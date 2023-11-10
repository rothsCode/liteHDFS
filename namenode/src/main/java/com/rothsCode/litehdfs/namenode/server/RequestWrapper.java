package com.rothsCode.litehdfs.namenode.server;

import com.alibaba.fastjson.JSONObject;
import com.rothsCode.litehdfs.common.netty.request.NettyPacket;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/11/15 10:32
 */
@Data
public class RequestWrapper {

  private String sequence;
  private int packetType;
  private ChannelHandlerContext ctx;

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
