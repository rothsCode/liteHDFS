package com.rothsCode.litehdfs.namenode.container;

import com.rothsCode.litehdfs.common.netty.LifeCycle;
import com.rothsCode.litehdfs.common.netty.thread.DefaultScheduler;
import com.rothsCode.litehdfs.datanode.DataNodeInfoManager;
import com.rothsCode.litehdfs.namenode.config.NameNodeConfig;
import com.rothsCode.litehdfs.namenode.config.NameNodeConfigLoader;
import com.rothsCode.litehdfs.namenode.file.DiskFileSystem;
import com.rothsCode.litehdfs.namenode.filetree.FileDirectoryTree;
import com.rothsCode.litehdfs.namenode.server.NameNodeApiHandler;
import com.rothsCode.litehdfs.namenode.server.NameNodeServer;
import com.rothsCode.litehdfs.namenode.user.UserManager;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;

/**
 * @author roths
 * @Description:
 * @date 2023/10/10 17:04
 */
@Slf4j
public class NameNodeContainer implements LifeCycle {

  private DataNodeInfoManager dataNodeManager;

  private NameNodeApiHandler nameNodeApiHandler;

  private NameNodeServer nameNodeServer;

  private AtomicBoolean startStatus = new AtomicBoolean(false);

  @Override
  public void init() {
    NameNodeConfig nameNodeConfig = NameNodeConfigLoader.getInstance().loadConfig();
    //dataNode管理器
    dataNodeManager = new DataNodeInfoManager(nameNodeConfig);
    //文件树
    FileDirectoryTree fileDirectoryTree = new FileDirectoryTree();
    DiskFileSystem diskFileSystem = new DiskFileSystem(nameNodeConfig, fileDirectoryTree);
    //初始化镜像
    diskFileSystem.loadDiskParseData();
    DefaultScheduler defaultScheduler = new DefaultScheduler("nameNodeBackupsScheduler");
    //用户管理器
    UserManager userManager = new UserManager(defaultScheduler);
    nameNodeApiHandler = new NameNodeApiHandler(nameNodeConfig, userManager, diskFileSystem,
        defaultScheduler, dataNodeManager, fileDirectoryTree);
    nameNodeServer = new NameNodeServer(nameNodeApiHandler, nameNodeConfig);
    log.info("nameNodeContainer init");
  }

  @Override
  public void start() {
    if (startStatus.compareAndSet(false, true)) {
      nameNodeServer.start();
      log.info("nameNodeContainer start");
    }
  }

  @Override
  public void shutDown() {
    if (startStatus.compareAndSet(true, false)) {
      nameNodeServer.shutDown();
    }
  }
}
