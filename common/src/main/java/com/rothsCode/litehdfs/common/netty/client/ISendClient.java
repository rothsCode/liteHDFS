package com.rothsCode.litehdfs.common.netty.client;

import com.rothsCode.litehdfs.common.netty.listener.NettyPacketListener;
import com.rothsCode.litehdfs.common.netty.request.NettyPacket;
import io.netty.channel.ChannelFutureListener;
import java.util.concurrent.CompletableFuture;

/**
 * @author roths
 * @Description: 发送请求api
 * @date 2023/10/27 15:58
 */
public interface ISendClient {

  /**
   * oneWay模式
   *
   * @param nettyPacket
   */
  void send(NettyPacket nettyPacket);

  /**
   * 同步获取返回结果
   *
   * @param request
   */
  NettyPacket sendSync(NettyPacket request);

  /**
   * 异步获取返回结果
   *
   * @param request
   */
  CompletableFuture<NettyPacket> asyncSend(NettyPacket request);

  /**
   * 异步调用回调模式
   *
   * @param request
   * @param channelFutureListener
   */
  void asyncSend(NettyPacket request, ChannelFutureListener channelFutureListener);

  /**
   * 添加回调类
   *
   * @param nettyPacketListener
   */
  void addPacketLister(NettyPacketListener nettyPacketListener);
}