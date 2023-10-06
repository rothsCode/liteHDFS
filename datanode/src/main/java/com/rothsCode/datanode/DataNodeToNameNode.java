package com.rothsCode.datanode;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author rothsCode
 * @Description:节点向nameNode的注册以及心跳
 * @date 2021/10/2815:58
 */
public class DataNodeToNameNode {

  /**
   * 注册
   *
   * @return
   */
  public Boolean register() throws UnknownHostException {
    InetAddress addr = InetAddress.getLocalHost();
    String hostName = addr.getHostName();
    String address = addr.getHostAddress();
    //发送
    return true;
  }

  /**
   * 心跳
   *
   * @return
   */
  public Boolean heartBeat() {

    return true;
  }
}
