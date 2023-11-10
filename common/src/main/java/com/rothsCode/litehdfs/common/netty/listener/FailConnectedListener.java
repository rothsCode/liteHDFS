package com.rothsCode.litehdfs.common.netty.listener;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/11/510:26
 */
public interface FailConnectedListener {


  /**
   * 连接失败监听
   */
  void handleFailConnected();
}
