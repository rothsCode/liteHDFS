package com.rothsCode.litehdfs.datanode.client;


import com.alibaba.fastjson.JSONObject;
import com.rothsCode.litehdfs.common.enums.PacketType;
import com.rothsCode.litehdfs.common.netty.LifeCycle;
import com.rothsCode.litehdfs.common.netty.NetClient;
import com.rothsCode.litehdfs.common.netty.request.DataNodeRequest;
import com.rothsCode.litehdfs.common.netty.request.FileInfo;
import com.rothsCode.litehdfs.common.netty.request.NettyPacket;
import com.rothsCode.litehdfs.common.netty.response.ServerResponse;
import com.rothsCode.litehdfs.common.netty.thread.DefaultScheduler;
import com.rothsCode.litehdfs.common.util.IPUtil;
import com.rothsCode.litehdfs.datanode.config.DataNodeConfig;
import com.rothsCode.litehdfs.datanode.vo.DataNodeStorageInfo;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @author rothsCode
 * @Description: dataNode -> NameNode
 * @date 2021/11/417:08
 */
@Slf4j
public class NameNodeClient implements LifeCycle {

  private NetClient netClient;
  private DataNodeConfig dataNodeConfig;
  private DataNodeStorageInfo dataNodeStorageInfo;


  public NameNodeClient(DataNodeStorageInfo dataNodeStorageInfo, DataNodeConfig dataNodeConfig,
      DefaultScheduler defaultScheduler) {
    this.dataNodeStorageInfo = dataNodeStorageInfo;
    this.dataNodeConfig = dataNodeConfig;
    this.netClient = new NetClient("nameNodeClient_",
        dataNodeConfig.getNameNodeServerAddress().split(":")[0],
        Integer.parseInt(dataNodeConfig.getNameNodeServerAddress().split(":")[1]),
        dataNodeConfig.getRetryTime(), defaultScheduler);
  }

  @Override
  public void init() {

  }

  /**
   * 启动nameNode客户端
   */
  public void start() {
    //启动连接后进行注册拉取操作
    netClient.addConnectedLister(connected -> {
      if (connected) {
        //连接成功
        registerNameNode();
                //启动心跳任务
                new DataNodeHeartTask(netClient, dataNodeConfig).start();
                //发送全量信息
                sendAllFileInfo(dataNodeStorageInfo.getFileInfos());
            } else {
                log.error("nameNodeClient connect fair");
      }
    });
    netClient.addFailConnectedLister(() -> {
      log.error("nameNodeClient connect fair");
    });
    netClient.startConnect();


  }

  public void shutDown() {
    netClient.shutDown();
  }


  private void registerNameNode() {
    DataNodeRequest dataNodeRequest = DataNodeRequest.builder().healthyStatus(1)
        .ip(IPUtil.getLocalIpByNetcard()).port(dataNodeConfig.getServerPort())
        .httpPort(dataNodeConfig.getHttpPort()).build();
    dataNodeRequest.setAddress(dataNodeRequest.getIp() + ":" + dataNodeRequest.getPort());
    NettyPacket nettyPacket = NettyPacket
        .buildPacket(JSONObject.toJSONString(dataNodeRequest).getBytes(),
            PacketType.DATA_NODE_REGISTER.value);
    netClient.send(nettyPacket);

  }

    /**
     * 向nameNode发送文件存储信息
     */
    public ServerResponse sendFileInfoToNameNode(FileInfo fileInfo) {
      NettyPacket nettyPacket = NettyPacket
          .buildPacket(JSONObject.toJSONString(fileInfo).getBytes(),
              PacketType.REPLICA_RECEIVE.value);
      NettyPacket response = netClient.sendSync(nettyPacket);
      if (response.getPackageType() != PacketType.REPLICA_RECEIVE.value) {
        return ServerResponse.failByMsg("packetType error");
      }
      ServerResponse serverResponse = JSONObject
          .parseObject(new String(response.getBody()), ServerResponse.class);
      return serverResponse;

    }

    /**
     * 启动完成后全量上报存储信息(自身节点启动后或者nameNode重启)
     */
    public void sendAllFileInfo(List<FileInfo> infoList) {
      DataNodeRequest dataNodeRequest = DataNodeRequest.builder().healthyStatus(1)
          .ip(IPUtil.getLocalIP()).fileInfos(infoList).build();
      dataNodeRequest.setAddress(dataNodeRequest.getIp() + ":" + dataNodeRequest.getPort());
      NettyPacket nettyPacket = NettyPacket
          .buildPacket(JSONObject.toJSONString(dataNodeRequest).getBytes(),
              PacketType.REPORT_STORAGE_INFO.value);
      netClient.send(nettyPacket);
    }

}
