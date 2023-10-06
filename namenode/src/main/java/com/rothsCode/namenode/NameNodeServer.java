package com.rothsCode.namenode;

import com.rothsCode.NameNodeConfig;
import com.rothsCode.net.BaseChannelInitial;
import com.rothsCode.net.NetServer;
import java.util.Collections;

/**
 * @author rothsCode
 * @Description:nameNodeServer端，接收客户端以及dataNode请求
 * @date 2021/11/8 10:05
 */
public class NameNodeServer {

  private com.rothsCode.net.NetServer netServer;
  private NameNodeApiHandler nameNodeApiHandler;
  private NameNodeConfig nameNodeConfig;

  public NameNodeServer(NameNodeApiHandler nameNodeApiHandler, NameNodeConfig nameNodeConfig) {
    this.netServer = new NetServer(null);
    this.nameNodeApiHandler = nameNodeApiHandler;
    this.nameNodeConfig = nameNodeConfig;
  }

  public void start() {
    com.rothsCode.net.BaseChannelInitial baseChannelInitial = new BaseChannelInitial();
    baseChannelInitial.addHandler(nameNodeApiHandler);
    this.netServer.setBaseChannelInitial(baseChannelInitial);
    netServer.start(Collections.singletonList(nameNodeConfig.getServerPort()));

  }

  public void shutDown() {
    netServer.shutDown();
  }


}
