package com.rothsCode.litehdfs.namenode.file;

import com.rothsCode.litehdfs.common.file.FileAppender;
import com.rothsCode.litehdfs.common.protoc.OperateLog;
import com.rothsCode.litehdfs.common.util.ThreadFactoryImpl;
import com.rothsCode.litehdfs.namenode.config.NameNodeConfig;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

/**
 * @author roths
 * @Description: 元數據持久化服務
 * @date 2023/11/13 17:19
 */
@Slf4j
public class DefaultOperateLogStore {

  //修改日志
  private final static String EDIT_LOG_PATH = "\\namenode\\editLog\\";
  /**
   * 操作日志数据间隔
   */
  private static long txIdInterval;

  /**
   * 文件写入器数
   */
  private static long fileAppenderNums;

  private static long fileAppenderNumsMask;

  private static long fileAppenderAdjustNums;
  private static String baseStoragePath;
  private static ScheduledExecutorService fillOperateLogFileAppenderService = Executors
      .newSingleThreadScheduledExecutor(new ThreadFactoryImpl("fillOperateLogFileAppenderTask"));
  /**
   * 文件预热,提前分配一组txId区间命名的文件写入器
   */
  private final List<FileAppender> fileAppenderList = new ArrayList<>();
  /**
   * 待删除的文件连接
   */
  private final List<FileAppender> removeFileAppenderList = new CopyOnWriteArrayList<>();
  /**
   * 当前刷盘时的日志id
   */
  private AtomicLong currentLogIndex = new AtomicLong(0);
  private volatile long fileAppenderEndIndex;
  private volatile long fileAppenderStartIndex;
  private NameNodeConfig nameNodeConfig;


  public DefaultOperateLogStore(NameNodeConfig nameNodeConfig, long maxTxId) {
    this.nameNodeConfig = nameNodeConfig;
    currentLogIndex.set(maxTxId);
    txIdInterval = nameNodeConfig.getTxIdInterval();
    fileAppenderNums = nameNodeConfig.getFileAppenderNums();
    fileAppenderNumsMask = fileAppenderNums - 1;
    fileAppenderAdjustNums = (long) (fileAppenderNums * nameNodeConfig
        .getFileAppenderAdjustThreshold());
    baseStoragePath = nameNodeConfig.getNameNodePath() + EDIT_LOG_PATH;
    // init fill FileAppender
    fileAppenderStartIndex = (int) ((currentLogIndex.get() / txIdInterval) * txIdInterval) + 1;
    for (int i = 1; i <= fileAppenderNums; i++) {
      long startTxId = fileAppenderStartIndex + (i - 1) * txIdInterval;
      long endTxId = fileAppenderStartIndex + i * txIdInterval - 1;
      String fileName = startTxId + "_" + endTxId;
      FileAppender fileAppender = new FileAppender(baseStoragePath, fileName,
          nameNodeConfig.getChannelFlushInterval());
      fileAppenderList.add(fileAppender);
      if (i == fileAppenderNums) {
        fileAppenderEndIndex = endTxId;
      }
    }
    //文件维护队列定时任务
    fillOperateLogFileAppenderService
        .scheduleAtFixedRate(this::fillFileAppenderList, 10,
            nameNodeConfig.getFileAppenderScheduleTime(),
            TimeUnit.SECONDS);

  }

  /**
   * 更新维护写入器队列  1 2 3 4 5 6 7 0
   */
  private void fillFileAppenderList() {
    //关闭上一次待删除的文件连接，避免过早关闭无法写入
    removeFileAppenderList.forEach(FileAppender::close);
    removeFileAppenderList.clear();
    long endAppenderPos =
        (fileAppenderEndIndex - 1 - fileAppenderStartIndex) / txIdInterval & fileAppenderNumsMask;
    // write read diff 差异槽数
    long diffCircleNums =
        (fileAppenderEndIndex - currentLogIndex.get()) / txIdInterval;
    //不考虑越界
    long fillFileNums = fileAppenderNums - diffCircleNums - 1;
    if (fillFileNums < fileAppenderAdjustNums) {
      return;
    }
    log.debug("fillFileAppenderList fillFileNums:{}", fillFileNums);
    long fillStartPos = (endAppenderPos + 1) & fileAppenderNumsMask;
    log.debug("fillFileAppenderList endAppenderPos:{},fillStartPos:{}", endAppenderPos,
        fillStartPos);
    for (int i = 1; i <= fillFileNums; i++) {
      long startTxId = fileAppenderEndIndex + (i - 1) * txIdInterval + 1;
      long endTxId = fileAppenderEndIndex + i * txIdInterval;
      String fileName = startTxId + "_" + endTxId;
      FileAppender currentCircleAppender = new FileAppender(baseStoragePath, fileName,
          nameNodeConfig.getChannelFlushInterval());
      int fillAppenderPos = (int) ((fillStartPos + i - 1) & fileAppenderNumsMask);
      //replace oldFileAppender;
      //获取旧的文件流
      removeFileAppenderList.add(fileAppenderList.get(fillAppenderPos));
      fileAppenderList.set(fillAppenderPos, currentCircleAppender);
      if (i == fillFileNums) {
        //prevent over
        fileAppenderEndIndex = endTxId;
      }
    }
    log.debug("fillFileAppenderList end");
  }

  public long syncOperateLogDisk(OperateLog pathOperateLog) {
    // just spin check writeStatus  and prevent over
    long txId;
    int sleepTime = 10;
    int retry = 0;
    while (currentLogIndex.get() >= fileAppenderEndIndex) {
      //fast fair
      if (retry > 4) {
        return 0;
      }
      try {
        sleepTime = (int) (sleepTime * (1L << retry));
        Thread.sleep(sleepTime);
        if (retry >= 4) {
          log.error("syncOperateLogDisk block not write");
        }
        retry++;
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    //get fileIndex
    txId = currentLogIndex.incrementAndGet();
    int fileIndex = (int) ((txId - fileAppenderStartIndex) / txIdInterval & fileAppenderNumsMask);
    pathOperateLog = OperateLog.newBuilder(pathOperateLog).setTxId(txId).build();
    byte[] body = pathOperateLog.toByteArray();
    ByteBuffer byteBuffer = ByteBuffer.allocate(body.length + 4);
    byteBuffer.putInt(body.length);
    byteBuffer.put(body);
    fileAppenderList.get(fileIndex).syncDisk(byteBuffer);
    return txId;
  }

}
