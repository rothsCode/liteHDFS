package com.rothsCode.litehdfs.common.netty.request;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/11/15 10:32
 */
@Data
@Builder
@AllArgsConstructor
public class RequestWrapper {

  private String sequence;
  private int packetType;
  private ChannelHandlerContext ctx;
  private NettyPacket nettyPacket;

  public RequestWrapper(String sequence, int packetType, ChannelHandlerContext ctx) {
    this.sequence = sequence;
    this.packetType = packetType;
    this.ctx = ctx;
  }

  public void sendResponse(Object response) {
    byte[] body = response == null ? new byte[0] : JSONObject.toJSONString(response).getBytes();
    NettyPacket nettyPacket = NettyPacket.buildPacket(body, packetType);
    nettyPacket.setSequence(sequence);
    ctx.writeAndFlush(nettyPacket);
  }


}
