package com.rothsCode.datanode;

import com.rothsCode.net.DefaultScheduler;
import com.rothsCode.net.NetServer;
import java.util.Arrays;

/**
 * @author rothsCode
 * @Description:客户端往datanode写数据
 * @date 2021/11/5 16:03
 */
public class DataNodeServer {

  private DataNodeServerHandler dataNodeServerHandler;
  private NetServer netServer;
  private DataNodeConfig dataNodeConfig;
  private StorageManager storageManager;

  public DataNodeServer(DefaultScheduler defaultScheduler, DataNodeConfig dataNodeConfig,
      DataNodeServerHandler dataNodeServerHandler, StorageManager storageManager) {
    netServer = new NetServer(defaultScheduler);
    this.dataNodeConfig = dataNodeConfig;
    this.dataNodeServerHandler = dataNodeServerHandler;
    this.storageManager = storageManager;
  }

  public void startServer() {
    MultiChannel multiChannel = new MultiChannel(dataNodeConfig, storageManager);
    multiChannel.addHandler(dataNodeServerHandler);
    this.netServer.setBaseChannelInitial(multiChannel);
    this.netServer
        .bindMultiPort(Arrays.asList(dataNodeConfig.getPort(), dataNodeConfig.getHttpPort()));
  }
}
