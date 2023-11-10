package com.rothsCode.litehdfs.client.netty;

import com.rothsCode.litehdfs.client.ClientConfig;
import com.rothsCode.litehdfs.common.netty.LifeCycle;
import com.rothsCode.litehdfs.common.netty.NetClient;
import com.rothsCode.litehdfs.common.netty.client.ISendClient;
import com.rothsCode.litehdfs.common.netty.listener.NettyPacketListener;
import com.rothsCode.litehdfs.common.netty.request.NettyPacket;
import com.rothsCode.litehdfs.common.netty.thread.DefaultScheduler;
import io.netty.channel.ChannelFutureListener;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

/**
 * @author roths
 * @Description:
 * @date 2023/10/27 15:26
 */
@Slf4j
public class NameNodeClient implements LifeCycle, ISendClient {

  private NetClient netClient;

  private ClientConfig clientConfig;

  private DefaultScheduler defaultScheduler;

  public NameNodeClient(ClientConfig clientConfig) {
    this.clientConfig = clientConfig;
  }

  @Override
  public void init() {
    this.defaultScheduler = new DefaultScheduler("FSClient_Scheduler");
    netClient = new NetClient("FileClient_", clientConfig.getNameNodeServerHost(),
        clientConfig.getNameNodeServerPort(), clientConfig.getRetryTime(), defaultScheduler);
  }

  @Override
  public void start() {
    netClient.addConnectedLister(connected -> {
      if (connected) {
        //登录成功后需要唤醒主线程
        synchronized (this) {
          notifyAll();
        }
      }
    });
    netClient.addFailConnectedLister(() -> {
      log.error("客户端与nameNode断开,可能宕机--");
    });
    netClient.startConnect();
    synchronized (this) {
      try {
        wait();
        log.error("客户端启动成功---");
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void shutDown() {
    netClient.shutDown();
  }


  @Override
  public void send(NettyPacket nettyPacket) {
    netClient.send(nettyPacket);
  }

  public NettyPacket sendSync(NettyPacket request) {
    return netClient.sendSync(request);
  }

  @Override
  public CompletableFuture<NettyPacket> asyncSend(NettyPacket request) {
    return netClient.asyncSend(request);
  }

  @Override
  public void asyncSend(NettyPacket request, ChannelFutureListener channelFutureListener) {
    netClient.asyncSend(request, channelFutureListener);
  }

  @Override
  public void addPacketLister(NettyPacketListener nettyPacketListener) {

  }
}
