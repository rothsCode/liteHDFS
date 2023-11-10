package com.rothsCode.litehdfs;

import com.rothsCode.litehdfs.namenode.container.NameNodeContainer;

/**
 * nameNode启动类
 */
public class NameNodeApplication {

  public static void main(String[] args) {
    NameNodeContainer nameNodeContainer = new NameNodeContainer();
    nameNodeContainer.init();
    nameNodeContainer.start();
    Runtime.getRuntime()
        .addShutdownHook(new Thread(nameNodeContainer::shutDown, "nameNodeContainer"));

  }

}
