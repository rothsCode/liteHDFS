package com.rothsCode.net;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import java.util.ArrayList;
import java.util.List;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/11/5 16:25
 */
public class BaseChannelInitial extends ChannelInitializer<SocketChannel> {

  private List<AbstractDataHandler> dataHandlerList = new ArrayList<>();

  @Override
  protected void initChannel(SocketChannel socketChannel) throws Exception {
    socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(10 * 1024 * 1024, 0, 4, 0, 4))
        .addLast(new LengthFieldPrepender(4))
        .addLast(new NettyPacketDecoder())//反序列化
        .addLast(new NettyPacketEncoder());//序列化

    for (AbstractDataHandler handler : dataHandlerList) {
      socketChannel.pipeline().addLast(handler);
    }
  }

  public void addHandler(AbstractDataHandler handler) {
    dataHandlerList.add(handler);
  }

  public void clearList() {
    dataHandlerList.clear();
  }
}
