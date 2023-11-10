package com.rothsCode.litehdfs.namenode.server;

import com.rothsCode.litehdfs.common.netty.handler.BaseChannelInitial;
import com.rothsCode.litehdfs.common.netty.server.NetServer;
import com.rothsCode.litehdfs.namenode.config.NameNodeConfig;
import java.util.Collections;

/**
 * @author rothsCode
 * @Description:nameNodeServer端，接收客户端以及dataNode请求
 * @date 2021/11/8 10:05
 */
public class NameNodeServer {

  private NetServer netServer;
  private NameNodeApiHandler nameNodeApiHandler;
  private NameNodeConfig nameNodeConfig;

  public NameNodeServer(NameNodeApiHandler nameNodeApiHandler, NameNodeConfig nameNodeConfig) {
    this.netServer = new NetServer(null);
    this.nameNodeApiHandler = nameNodeApiHandler;
    this.nameNodeConfig = nameNodeConfig;
  }

  public void start() {
    BaseChannelInitial baseChannelInitial = new BaseChannelInitial();
    baseChannelInitial.addHandler(nameNodeApiHandler);
    this.netServer.setBaseChannelInitial(baseChannelInitial);
    netServer.start(Collections.singletonList(nameNodeConfig.getServerPort()));

  }

  public void shutDown() {
    netServer.shutDown();
  }


}
