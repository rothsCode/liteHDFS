package com.rothsCode.litehdfs;

import com.rothsCode.litehdfs.namenode.container.NameNodeContainer;
import lombok.extern.slf4j.Slf4j;

/**
 * nameNode启动类
 */
@Slf4j
public class NameNodeApplication {

  public static void main(String[] args) {
    NameNodeContainer nameNodeContainer = new NameNodeContainer();
    nameNodeContainer.init();
    nameNodeContainer.start();
    Runtime.getRuntime()
        .addShutdownHook(new Thread(nameNodeContainer::shutDown, "nameNodeContainer"));
    log.info("nameNodeApplication started");
  }

}
