package com.rothsCode.litehdfs;

import com.rothsCode.litehdfs.datanode.config.DataNodeConfigLoader;
import com.rothsCode.litehdfs.datanode.container.DataNodeContainer;

/**
 * dataNode启动类
 */
public class DataNodeApplication {

  public static void main(String[] args) {
    DataNodeConfigLoader.getInstance().loadConfig(args);
    DataNodeContainer dataNodeContainer = new DataNodeContainer();
    dataNodeContainer.init();
    dataNodeContainer.start();
    Runtime.getRuntime()
        .addShutdownHook(new Thread(dataNodeContainer::shutDown, "dataNodeContainer"));

  }


}
