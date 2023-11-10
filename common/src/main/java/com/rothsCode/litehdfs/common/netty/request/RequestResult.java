package com.rothsCode.litehdfs.common.netty.request;

import com.alibaba.fastjson.JSONObject;
import com.rothsCode.litehdfs.common.netty.exception.RequestTimeOutException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author rothsCode
 * @Description: 请求结果处理类
 * @date 2021/11/15 14:20
 */
@Slf4j
public class RequestResult {

  private final long sendTime;
  private NettyPacket request;
  private NettyPacket response;
  private volatile boolean getResult = false;
  private boolean timeOutFlag = false;

  public RequestResult(NettyPacket request) {
    this.sendTime = System.currentTimeMillis();
    this.request = request;
  }

  public boolean isTimeOut() {
    if (this.timeOutFlag) {
      return true;
    }
    if (System.currentTimeMillis() - sendTime > 5000) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * 3秒钟超时机制
   *
   * @return
   */

  public NettyPacket getResult() {
    waitResult();
    return response;
  }

  public void setResult(NettyPacket response) {
    synchronized (this) {
      this.response = response;
      this.getResult = true;
      log.error("开始获取返回值");
      notifyAll();
    }

  }

  public void waitResult() {
    synchronized (this) {
      try {
        while (!getResult && !timeOutFlag) {
          try {
            wait(100);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        if (timeOutFlag) {
          throw new RequestTimeOutException(JSONObject.toJSONString(request) + ":请求返回结果超时");
        }
        log.error("最终返回值");
      } catch (Exception e) {
        log.error("等待消息被打断");
      }
    }

  }

  public void markTimeout() {
    if (this.timeOutFlag) {
      return;
    }
    timeOutFlag = true;
    synchronized (this) {
      notifyAll();
    }
  }
}
