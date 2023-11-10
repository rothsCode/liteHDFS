package com.rothsCode.litehdfs.common.netty.handler;

import com.rothsCode.litehdfs.common.netty.request.NettyPacket;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.SocketChannel;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * @author rothsCode
 * @Description: netty 发送结果支持类
 * @date 2021/11/15 13:54
 */
@Slf4j
public class NettySendClient {

  private static final long FUTURE_TIME_OUT_SECONDS = 1200;
  private Map<String, CompletableFuture<NettyPacket>> sequenceResultMap = new ConcurrentHashMap<>();
  private SocketChannel socketChannel;

  public void setChannel(SocketChannel socketChannel) {
    this.socketChannel = socketChannel;
  }


  public NettyPacket sendSync(NettyPacket request) {
    if (socketChannel == null || !socketChannel.isActive()) {
      return null;
    }
    CompletableFuture<NettyPacket> nettyResponseFuture = new CompletableFuture();
    sequenceResultMap.put(request.getSequence(), nettyResponseFuture);
    socketChannel.writeAndFlush(request);
    try {
      return nettyResponseFuture.get(FUTURE_TIME_OUT_SECONDS, TimeUnit.SECONDS);
    } catch (Exception e) {
      log.error("sendSync error:{}", e);
    }
    return null;
  }

  /**
   * 异步发送future模式
   *
   * @param request
   * @return NettyResponseFuture
   */
  public CompletableFuture<NettyPacket> asyncSend(NettyPacket request) {
    if (socketChannel == null || !socketChannel.isActive()) {
      return null;
    }
    CompletableFuture<NettyPacket> nettyResponseFuture = new CompletableFuture();
    sequenceResultMap.put(request.getSequence(), nettyResponseFuture);
    socketChannel.writeAndFlush(request);
    return nettyResponseFuture;
  }

  /**
   * 异步调用 lister回调模式
   *
   * @param request
   * @return
   */
  public void asyncSend(NettyPacket request, ChannelFutureListener channelFutureListener) {
    if (socketChannel == null || !socketChannel.isActive()) {
      return;
    }
    socketChannel.writeAndFlush(request).addListener(channelFutureListener);
  }


  public boolean onResponse(NettyPacket response) {
    String sequence = response.getSequence();
    if (sequence == null) {
      return false;
    }
    CompletableFuture nettyResponseFuture = sequenceResultMap.get(sequence);
    if (nettyResponseFuture == null) {
      return false;
    }
    nettyResponseFuture.complete(response);
    return true;
  }


}
