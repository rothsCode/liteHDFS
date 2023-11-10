package com.rothsCode.litehdfs.common.netty.client;

import com.rothsCode.litehdfs.common.netty.LifeCycle;
import com.rothsCode.litehdfs.common.netty.NetClient;
import com.rothsCode.litehdfs.common.netty.listener.NettyPacketListener;
import com.rothsCode.litehdfs.common.netty.request.NettyPacket;
import com.rothsCode.litehdfs.common.netty.thread.DefaultScheduler;
import com.rothsCode.litehdfs.common.netty.vo.DataNodeInfo;
import io.netty.channel.ChannelFutureListener;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

/**
 * @author roths
 * @Description:
 * @date 2023/10/27 15:26
 */
@Slf4j
public class DataNodeClient implements LifeCycle, ISendClient {

  private NetClient netClient;

  private DataNodeInfo dataNodeInfo;

  private DefaultScheduler defaultScheduler;

  public DataNodeClient(DataNodeInfo dataNodeInfo) {
    this.dataNodeInfo = dataNodeInfo;
    init();
    start();
  }

  @Override
  public void init() {
    this.defaultScheduler = new DefaultScheduler("FSDataNodeClient_Scheduler");
    netClient = new NetClient("dataClient_", dataNodeInfo.getIp(),
        dataNodeInfo.getPort(), 3, defaultScheduler);
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
      log.error("dataNodeClient fair connected");
    });
    netClient.startConnect();
    synchronized (this) {
      try {
        wait();
        log.info("dataNodeClient started---");
      } catch (InterruptedException e) {
        //ignore
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
    netClient.addPacketLister(nettyPacketListener);
  }
}
