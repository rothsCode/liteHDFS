package com.rothsCode.litehdfs.common.netty.listener;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/11/417:57
 */
public interface ConnectedListener {


  /**
   * 网络连接状态监听类
   */
  void connectedStatus(boolean connected);
}
