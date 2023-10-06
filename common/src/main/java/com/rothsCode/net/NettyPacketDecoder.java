package com.rothsCode.net;

import com.rothsCode.net.request.NettyPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;

/**
 * @author rothsCode
 * @Description:网络包解码
 * @date 2021/11/5 16:28
 */
public class NettyPacketDecoder extends MessageToMessageDecoder<ByteBuf> {


  @Override
  protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf,
      List<Object> list) throws Exception {
    if (byteBuf != null) {
      NettyPacket nettyPacket = NettyPacket.decodePacket(byteBuf);
      list.add(nettyPacket);
    }

  }
}
