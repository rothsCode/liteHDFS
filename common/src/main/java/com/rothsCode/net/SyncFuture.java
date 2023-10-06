package com.rothsCode.net;

import com.rothsCode.net.request.NettyPacket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


/**
 * @author rothsCode
 * @Description: 异步获取消息
 * @date 2021/11/26 15:06
 */
public class SyncFuture implements Future<NettyPacket> {

  private CountDownLatch latch = new CountDownLatch(1);
  private NettyPacket response;
  private NettyPacket request;

  public SyncFuture(NettyPacket request) {
    this.request = request;
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return false;
  }

  @Override
  public boolean isCancelled() {
    return false;
  }

  @Override
  public boolean isDone() {
    if (response != null) {
      return true;
    }
    return false;
  }

  @Override
  public NettyPacket get() throws InterruptedException {
    latch.await();
    return this.response;
  }

  @Override
  public NettyPacket get(long timeout, TimeUnit unit) throws InterruptedException {
    latch.await(timeout, unit);
    return this.response;
  }

  public void setResponse(NettyPacket response) {
    this.response = response;
    latch.countDown();
  }
}
