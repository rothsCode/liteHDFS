package com.rothsCode.litehdfs.datanode.container;

import com.rothsCode.litehdfs.common.netty.LifeCycle;
import com.rothsCode.litehdfs.common.netty.thread.DefaultScheduler;
import com.rothsCode.litehdfs.datanode.client.NameNodeClient;
import com.rothsCode.litehdfs.datanode.config.DataNodeConfig;
import com.rothsCode.litehdfs.datanode.config.DataNodeConfigLoader;
import com.rothsCode.litehdfs.datanode.file.StorageManager;
import com.rothsCode.litehdfs.datanode.handler.DataNodeServerHandler;
import com.rothsCode.litehdfs.datanode.handler.FileCallBackHandler;
import com.rothsCode.litehdfs.datanode.server.DataNodeServer;
import com.rothsCode.litehdfs.datanode.vo.DataNodeStorageInfo;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author roths
 * @Description: dataNode 启动容器
 * @date 2023/10/19 15:37
 */
public class DataNodeContainer implements LifeCycle {

  private NameNodeClient nameNodeClient;
  private AtomicBoolean startStatus = new AtomicBoolean(false);
  private DataNodeServer dataNodeServer;
  private StorageManager storageManager;
  private DataNodeServerHandler dataNodeServerHandler;
  private DefaultScheduler defaultScheduler;
  private FileCallBackHandler fileCallBackHandler;

  @Override
  public void init() {
    DataNodeConfig dataNodeConfig = DataNodeConfigLoader.getInstance().getDataNodeConfig();
    this.defaultScheduler = new DefaultScheduler("FS_dataNodeScheduler");
    this.storageManager = new StorageManager();
    DataNodeStorageInfo dataNodeStorageInfo = storageManager
        .scanFiles(dataNodeConfig.getDataPath());
    if (dataNodeStorageInfo != null) {
      storageManager.setStorageInfo(dataNodeStorageInfo);
    }
    this.nameNodeClient = new NameNodeClient(dataNodeStorageInfo, dataNodeConfig,
        defaultScheduler);
    fileCallBackHandler = new FileCallBackHandler(storageManager, nameNodeClient);
    dataNodeServerHandler = new DataNodeServerHandler(dataNodeStorageInfo, dataNodeConfig,
        fileCallBackHandler);
    dataNodeServer = new DataNodeServer(defaultScheduler, dataNodeConfig, dataNodeServerHandler,
        storageManager);
  }

  @Override
  public void start() {
    if (startStatus.compareAndSet(false, true)) {
      this.nameNodeClient.start();
      this.dataNodeServer.startServer();
    }
  }

  @Override
  public void shutDown() {
    if (startStatus.compareAndSet(true, false)) {
      this.nameNodeClient.shutDown();
      this.dataNodeServer.shutDownServer();
    }
  }
}
