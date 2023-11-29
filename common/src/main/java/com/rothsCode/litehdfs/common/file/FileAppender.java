package com.rothsCode.litehdfs.common.file;

import cn.hutool.core.lang.Assert;
import com.rothsCode.litehdfs.common.enums.FlushDiskType;
import com.rothsCode.litehdfs.common.netty.thread.ServiceThread;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

/**
 * @author rothsCode
 * @Description:文件追加工具类
 * @date 2021/11/16 14:54
 */
@Slf4j
@Getter
public class FileAppender {

  private RandomAccessFile rds;
  private FileChannel channel;
  private File destFile;
  private static ReentrantLock syncDiskLock = new ReentrantLock();
  private FlushDiskType flushDiskType = FlushDiskType.ASYNC_FLUSH;
  private int flushInterval = 10;
  private AsyncFlushFileService asyncFlushFileService;

  public FileAppender(String storagePath, String fileName) {
    initFile(storagePath, fileName);
  }

  public FileAppender(File file) {
    this.destFile = file;
    createFileChannel();
  }

  public FileAppender(String storagePath, String fileName, int flushInterval) {
    initFile(storagePath, fileName);
    if (flushInterval == 0) {
      flushDiskType = FlushDiskType.SYNC_FLUSH;
    } else {
      flushDiskType = FlushDiskType.ASYNC_FLUSH;
      this.flushInterval = flushInterval;
    }
  }

  private void initFile(String storagePath, String fileName) {
    int suffixLastIndex = fileName.lastIndexOf("/");
    if (suffixLastIndex > 0) {
      File fileDir = new File(storagePath + fileName.substring(0, suffixLastIndex));
      if (!fileDir.exists()) {
        fileDir.mkdirs();
      }
    }
    destFile = new File(storagePath + fileName);
    if (!destFile.exists()) {
      try {
        destFile.createNewFile();
      } catch (IOException e) {
        log.error("createNewFile error:{}", fileName);
      }
    }
    createFileChannel();
  }

  private void createFileChannel() {
    try {
      rds = new RandomAccessFile(destFile, "rw");
    } catch (FileNotFoundException e) {
      log.error("{}:file not find", destFile.getPath());
    }
    Assert.notNull(rds, "randomAccessFile create error");
    channel = rds.getChannel();
    //创建文件连接后开启刷盘任务
    if (FlushDiskType.ASYNC_FLUSH.equals(flushDiskType)) {
      asyncFlushFileService = new AsyncFlushFileService();
      asyncFlushFileService.start();
    }
  }

  /**
   * 异步追加写入数据
   *
   * @param body
   */
  public void append(byte[] body) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(body.length);
    byteBuffer.put(body);
    syncDisk(byteBuffer);
  }

  /**
   * @param byteBuffer
   */
  public void syncDisk(ByteBuffer byteBuffer) {
    syncDiskLock.lock();
    try {
      // 5、读写切换
      byteBuffer.flip();
      while (byteBuffer.hasRemaining()) {
        channel.write(byteBuffer);
      }
      if (FlushDiskType.SYNC_FLUSH.equals(flushDiskType)) {
        channel.force(false);
      }
    } catch (IOException e) {
      log.error("syncDisk error:{}", e);
    } finally {
      syncDiskLock.unlock();
    }

  }

  public void transferChannelAndClose(FileAppender targetFileAppender) {
    try {
      channel.transferTo(0, channel.size(), targetFileAppender.channel);
      this.close();
    } catch (IOException e) {
      log.error("readBodyError:{}", e);
    }
  }

  /**
   * 校验数据一致性
   */
  public boolean checkBlockCheckCode(String blockCheckCode) {
    String sourceBlockCheckCode = getFileCheckCode();
    if (sourceBlockCheckCode.equals(blockCheckCode)) {
      return true;
    } else {
      this.close();
      FileUtils.deleteQuietly(destFile);
      return false;
    }
  }

  /**
   * 获取整个文件校验码
   *
   * @return
   */
  public String getFileCheckCode() {
    ByteBuffer byteBuffer = readBody();
    byte[] bytes = byteBuffer.array();
    //TODO 读取不到数据
    if (bytes[0] == 0) {
      try (FileInputStream fileInputStream = new FileInputStream(destFile)) {
        return DigestUtils.sha512Hex(fileInputStream);
      } catch (Exception e) {
        return null;
      }
    }
    return DigestUtils.sha512Hex(bytes);
  }

  /**
   * 关闭资源 发送文件消息到nameNode
   */
  public void close() {
    try {
      if (asyncFlushFileService != null) {
        asyncFlushFileService.shutdown();
      }
      if (channel != null && channel.isOpen()) {
        channel.force(false);
        channel.close();
      }
      if (rds != null) {
        rds.close();
      }
    } catch (Exception e) {
      log.error("fileCloseError:{}", e);
    }
  }

  /**
   * 获取部分文件对应的校验码
   *
   * @return
   */
  public String getBlockFileCheckCode(int blockIndex, int blockSize, int bufferSize) {
    try {
      rds.seek(blockIndex * blockSize);
      byte[] blockByte = new byte[bufferSize];
      rds.read(blockByte);
      //恢复到文件初始读取位置
      rds.seek(blockIndex * blockSize);
      return DigestUtils.sha512Hex(blockByte);
    } catch (Exception e) {
      return null;
    }
  }

  public ByteBuffer readBody() {
    try {
      //读之前强制刷新磁盘
      channel.force(false);
      ByteBuffer buffer = ByteBuffer.allocate((int) destFile.length());
      channel.read(buffer);
      buffer.flip();
      return buffer;
    } catch (IOException e) {
      return null;
    }
  }

  public ByteBuffer readBody(int bufferSize) {
    try {
      //读之前强制刷新磁盘
      channel.force(false);
      ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
      channel.read(byteBuffer);
      byteBuffer.flip();
      return byteBuffer;
    } catch (IOException e) {
      return null;
    }
  }

  public void deleteFile() {
    FileUtils.deleteQuietly(destFile);
  }

  /**
   * 异步刷盘任务
   */
  class AsyncFlushFileService extends ServiceThread {

    @Override
    public String getServiceName() {
      return "asyncFlushFileThread";
    }

    @Override
    public void run() {
      while (!this.isStopped()) {
        this.waitForRunning(flushInterval);
        try {
          if (channel.isOpen() && channel.size() > 0) {
            channel.force(false);
          }
        } catch (IOException e) {
          log.error("{}:syncDisk error:{}", destFile.getName(), e);
        }
      }

    }
  }

}
