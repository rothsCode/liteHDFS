package com.rothsCode.litehdfs.datanode.client;

import com.alibaba.fastjson.JSONObject;
import com.rothsCode.litehdfs.common.enums.PacketType;
import com.rothsCode.litehdfs.common.netty.NetClient;
import com.rothsCode.litehdfs.common.netty.request.DataNodeRequest;
import com.rothsCode.litehdfs.common.netty.request.NettyPacket;
import com.rothsCode.litehdfs.common.util.IPUtil;
import com.rothsCode.litehdfs.datanode.config.DataNodeConfig;
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
      DataNodeRequest dataNodeRequest = DataNodeRequest.builder().healthyStatus(1)
          .ip(IPUtil.getLocalIpByNetcard()).port(dataNodeConfig.getServerPort()).build();
      dataNodeRequest.setAddress(dataNodeRequest.getIp() + ":" + dataNodeRequest.getPort());
      NettyPacket nettyPacket = NettyPacket
          .buildPacket(JSONObject.toJSONString(dataNodeRequest).getBytes(),
              PacketType.HEART_BRET.value);
      netClient.send(nettyPacket);
      Thread.sleep(dataNodeConfig.getHeartBeatTime());
    }

  }
}
