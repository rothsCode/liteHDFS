package com.rothsCode.namenode;

import com.rothsCode.vo.OperateLog;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;

/**
 * @author rothsCode
 * @Description: 操作记录
 * @date 2021/11/29 11:11
 */
@Data
public class OperateLogBuffer {

  private static final long exchangeThreshold = 10;  //内存队列交换阀值
  private volatile List<OperateLog> operateLogList = new ArrayList<>();  //写入内存buffer
  private List<OperateLog> flushLogList = new ArrayList<>();    //即将刷盘buffer
  private AtomicLong currentLogIndex = new AtomicLong(0); //当前刷盘时的日志id
  private volatile long lastFlushIndex = 0;//上一次刷盘的日志末尾ID
  private volatile long currentFlushIndex = 0;//当前刷盘的末尾日志ID
  private AtomicBoolean isNowFlush = new AtomicBoolean(false);//是否正在刷盘
  private AtomicBoolean isAdd = new AtomicBoolean(true);//是否可以写入内存
  private volatile boolean isFLushData = false; //是否存在可刷入的数据

  public void addOperateLog(OperateLog operateLog) throws InterruptedException {
    synchronized (this) {
      while (!isAdd.get()) {
        wait(10);
      }
      operateLog.setId(currentLogIndex.addAndGet(1));
      operateLogList.add(operateLog);
      System.out.println("操作日志加入到内存中:" + currentLogIndex.get());
      if (operateLogList.size() >= exchangeThreshold) {
        isAdd.set(false);
        //正在刷盘则不可进行内存转移
        while (isNowFlush.get() || isFLushData) {
          wait(10);
        }
        exchangeList();
      }

    }
  }

  public boolean getIsFLushData() {
    return isFLushData;
  }

  public void setIsFLushData(boolean flag) {
    isFLushData = flag;
  }


  public void clearFlushList() {
    flushLogList.clear();
  }

  public long getCurrentFlushIndex() {
    return currentFlushIndex;
  }

  public long getStartFlushIndex() {
    return lastFlushIndex + 1;
  }

  public void setLastFlushIndex(long index) {
    lastFlushIndex = index;
  }

  /**
   * 内存转移
   */
  public void exchangeList() throws InterruptedException {
    synchronized (this) {
      flushLogList.addAll(operateLogList);
      currentFlushIndex = currentLogIndex.get();
      operateLogList.clear();
      isFLushData = true;
      isAdd.set(true);
      notifyAll();
    }
  }
}
