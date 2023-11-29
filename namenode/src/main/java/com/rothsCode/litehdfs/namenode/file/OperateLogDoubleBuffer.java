package com.rothsCode.litehdfs.namenode.file;

import com.rothsCode.litehdfs.common.protoc.OperateLog;
import com.rothsCode.litehdfs.namenode.config.NameNodeConfig;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author rothsCode
 * @Description: 操作日志双缓存
 * @date 2021/11/29 11:11
 */
@Data
@Slf4j
public class OperateLogDoubleBuffer {

  private static final double EXCHANGE_THRESHOLD = 0.7;  //内存交换阀值
  private ByteBuffer writeBuffer;  //写入内存buffer
  private ByteBuffer flushBuffer;    //即将刷盘buffer
  private AtomicLong currentLogIndex = new AtomicLong(0); //当前刷盘时的日志id
  private volatile long lastFlushIndex = 0;//上一次刷盘的日志末尾ID
  private volatile boolean isNowFlush = false;//是否正在刷盘
  private volatile boolean isWrite = true;//是否可以写入内存
  private volatile boolean isShouldExchangeBuffer = false; //是否需要交换缓存
  private volatile boolean isNowExchangeBuffer = false;//是否正在交换buffer
  private DiskFileSystem diskFileSystem;
  private int exchange_threshold_nums;

  public OperateLogDoubleBuffer(NameNodeConfig nameNodeConfig, DiskFileSystem diskFileSystem,
      long maxTxId) {
    //init buffer
    currentLogIndex.set(maxTxId);
    lastFlushIndex = maxTxId;
    writeBuffer = ByteBuffer.allocate(nameNodeConfig.getEditLogBufferSize());
    flushBuffer = ByteBuffer.allocate(nameNodeConfig.getEditLogBufferSize());
    this.diskFileSystem = diskFileSystem;
    exchange_threshold_nums = (int) (writeBuffer.capacity() * EXCHANGE_THRESHOLD);
  }


  public long addOperateLog(OperateLog operateLog) {
    checkWriteStatus();
    long txId = currentLogIndex.incrementAndGet();
    operateLog = OperateLog.newBuilder(operateLog).setTxId(txId).build();
    byte[] body = operateLog.toByteArray();
    //多线程写入会造成数据位置错乱
    synchronized (this) {
      writeBuffer.putInt(body.length);
      writeBuffer.put(body);
    }
    if (!isShouldExchangeBuffer
        && writeBuffer.position() >= exchange_threshold_nums) {
      isShouldExchangeBuffer = true;
      log.debug("canExchangeBuffer writeBufferPosition:{}", writeBuffer.position());
    }
    //没到交换时间超过阈值并且其他线程还未进行刷盘进行交换,正在交换不可写 ，正在刷盘不可交换
    if (isShouldExchangeBuffer && !isNowExchangeBuffer) {
      boolean exchangeFlag = exchangeDoubleBuffer();
      //刷盘
      if (exchangeFlag) {
        log.debug("batchFlushPathOperateLog start");
        diskFileSystem.batchFlushPathOperateLog(this);
      }
    }
    return operateLog.getTxId();
  }

  private void checkWriteStatus() {
    if (!isWrite) {
      synchronized (this) {
        log.debug("block when exchangeBuffer");
        //block when exchangeBuffer is not write
        while (!isWrite) {
          try {
            wait(10);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
      }
    }
  }

  public void clearFlushBuffer() {
    flushBuffer.clear();
  }

  public long getCurrentFlushIndex() {
    return currentLogIndex.get();
  }

  public long getStartFlushIndex() {
    return lastFlushIndex + 1;
  }

  public void setLastFlushIndex(long index) {
    lastFlushIndex = index;
  }

  public void setIsNowFlush(boolean isNowFlush) {
    this.isNowFlush = isNowFlush;
  }

  /**
   * 交换后触发异步刷盘
   */
  public boolean exchangeDoubleBuffer() {
    synchronized (this) {
      if (isNowExchangeBuffer || writeBuffer.position() <= 0) {
        return false;
      }
      isNowExchangeBuffer = true;
      isWrite = false;
    }
    //正在刷盘不可交换
    while (isNowFlush) {
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    log.debug("operateLogList over start exchangeList");
    ByteBuffer temBuffer = writeBuffer;
    writeBuffer = flushBuffer;
    flushBuffer = temBuffer;
    writeBuffer.clear();
    isWrite = true;
    isNowExchangeBuffer = false;
    isShouldExchangeBuffer = false;
    log.debug("exchangeBuffer complete");
    return true;
  }
}
