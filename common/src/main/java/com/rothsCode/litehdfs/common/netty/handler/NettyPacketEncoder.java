package com.rothsCode.litehdfs.common.netty.handler;

import com.rothsCode.litehdfs.common.netty.request.NettyPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author rothsCode
 * @Description: 编码
 * @date 2021/11/5 18:21
 */
public class NettyPacketEncoder extends MessageToByteEncoder<NettyPacket> {

  @Override
  protected void encode(ChannelHandlerContext channelHandlerContext, NettyPacket nettyPacket,
      ByteBuf byteBuf) throws Exception {
    nettyPacket.writeBuffer(byteBuf);
  }
}
