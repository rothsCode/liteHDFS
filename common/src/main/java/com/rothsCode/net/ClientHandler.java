package com.rothsCode.net;

import com.rothsCode.net.request.NettyPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/11/416:26
 */
public class ClientHandler extends SimpleChannelInboundHandler<NettyPacket> {

  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, NettyPacket nettyPacket)
      throws Exception {

  }
}
