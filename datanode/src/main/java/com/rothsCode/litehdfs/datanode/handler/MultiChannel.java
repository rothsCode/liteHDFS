package com.rothsCode.litehdfs.datanode.handler;

import com.rothsCode.litehdfs.common.netty.handler.BaseChannelInitial;
import com.rothsCode.litehdfs.datanode.config.DataNodeConfig;
import com.rothsCode.litehdfs.datanode.file.StorageManager;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * @author rothsCode
 * @Description: 多端口绑定，一个专门下载文件的端口，一个各组件通信的端口
 * @date 2021/11/8 10:37
 */
public class MultiChannel extends BaseChannelInitial {

  private DataNodeConfig dataNodeConfig;
  private StorageManager storageManager;

  public MultiChannel(DataNodeConfig dataNodeConfig, StorageManager storageManager) {
    this.dataNodeConfig = dataNodeConfig;
    this.storageManager = storageManager;
  }

  @Override
  protected void initChannel(SocketChannel socketChannel) throws Exception {
    int port = socketChannel.localAddress().getPort();
    if (port == dataNodeConfig.getServerPort()) {
      super.initChannel(socketChannel);
    } else {
      socketChannel.pipeline().addLast(new HttpServerCodec())
          .addLast(new HttpObjectAggregator(65536))
          .addLast(new FileRequestHandler());
    }
  }
}
