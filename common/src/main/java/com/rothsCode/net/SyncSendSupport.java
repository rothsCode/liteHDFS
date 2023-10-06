package com.rothsCode.net;

import com.rothsCode.net.request.NettyPacket;
import io.netty.channel.socket.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;

/**
 * @author rothsCode
 * @Description: 同步发送返回结果支持类
 * @date 2021/11/15 13:54
 */
public class SyncSendSupport {

  private Map<String, SyncFuture> sequenceResultMap = new ConcurrentHashMap<>();
  private SocketChannel socketChannel;
  private DefaultScheduler defaultScheduler;

  public SyncSendSupport(DefaultScheduler defaultScheduler) {
    this.defaultScheduler = defaultScheduler;
    //   defaultScheduler.schedule("超时检测", this::checkRequestTimeout,0,1,TimeUnit.SECONDS);
  }


  public void setChannel(SocketChannel socketChannel) {
    this.socketChannel = socketChannel;
  }

  @SneakyThrows
  public NettyPacket sendSync(NettyPacket request) {
    if (socketChannel == null || !socketChannel.isActive()) {
      System.out.println("连接丢失");
      return null;
    }
    SyncFuture syncFuture = new SyncFuture(request);
    sequenceResultMap.put(request.getSequence(), syncFuture);
    socketChannel.writeAndFlush(request);
    System.out.println("发送消息开始等待结果");
    return syncFuture.get(30, TimeUnit.SECONDS);
  }

  public boolean onResponse(NettyPacket response) {
    String sequence = response.getSequence();
    if (sequence == null) {
      return false;
    }
    SyncFuture syncFuture = sequenceResultMap.get(sequence);
    if (syncFuture == null) {
      return false;
    }
    syncFuture.setResponse(response);
    return true;
  }


}
