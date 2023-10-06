package com.rothsCode.datanode;

import com.alibaba.fastjson.JSONObject;
import com.rothsCode.net.IPUtil;
import com.rothsCode.net.NetClient;
import com.rothsCode.net.PacketType;
import com.rothsCode.net.request.DataNodeRequest;
import com.rothsCode.net.request.NettyPacket;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author rothsCode
 * @Description: 定时发生数据节点心跳信息
 * @date 2021/11/515:42
 */
@Slf4j
public class DataNodeHeartTask extends Thread {

  private NetClient netClient;
  private DataNodeConfig dataNodeConfig;

  public DataNodeHeartTask(NetClient netClient, DataNodeConfig dataNodeConfig) {
    this.netClient = netClient;
    this.dataNodeConfig = dataNodeConfig;
  }

  @SneakyThrows
  @Override
  public void run() {
    while (true) {
      String ip = IPUtil.getLocalIpByNetcard();
      DataNodeRequest dataNodeRequest = DataNodeRequest.builder().healthyStatus(1)
          .hostName(ip).port(dataNodeConfig.getPort()).build();
      NettyPacket nettyPacket = NettyPacket
          .buildPacket(JSONObject.toJSONString(dataNodeRequest).getBytes(),
              PacketType.HEART_BRET.value);
      System.out.println("dataNode发起心跳请求" + nettyPacket);
      netClient.send(nettyPacket);
      Thread.sleep(dataNodeConfig.getHeartBeatTime());
    }


  }
}
