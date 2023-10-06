package com.rothsCode.datanode;


import com.alibaba.fastjson.JSONObject;
import com.rothsCode.net.DefaultScheduler;
import com.rothsCode.net.NetClient;
import com.rothsCode.net.PacketType;
import com.rothsCode.net.request.DataNodeRequest;
import com.rothsCode.net.request.FileInfo;
import com.rothsCode.net.request.NettyPacket;
import com.rothsCode.net.request.RequestWrapper;
import com.rothsCode.net.response.ServerResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @author rothsCode
 * @Description: dataNode -> NameNode
 * @date 2021/11/417:08
 */
@Slf4j
public class NameNodeClient {

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

    /**
     * 启动nameNode客户端
     */
    public void start() {
        //  netClient.addPacketLister(this::callBack);
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
                log.error("连接失败准备切换");
            }
        });
        netClient.addFailConnectedLister(() -> {
            System.out.println("检测到连接失败");
        });
        netClient.startConnect();


    }

    private void registerNameNode() {
        DataNodeRequest dataNodeRequest = DataNodeRequest.builder().healthyStatus(1)
            .hostName(dataNodeConfig.getHostName()).port(dataNodeConfig.getPort())
            .httpPort(dataNodeConfig.getHttpPort()).build();
        System.out.println("dataNode发起注册请求" + JSONObject.toJSONString(dataNodeRequest));
        NettyPacket nettyPacket = NettyPacket
            .buildPacket(JSONObject.toJSONString(dataNodeRequest).getBytes(),
                PacketType.DATA_NODE_REGISTER.value);
        netClient.send(nettyPacket);

    }

    /**
     * 向nameNode发送文件存储信息
     */
    public boolean sendFileInfoToNameNode(FileInfo fileInfo) {
        NettyPacket nettyPacket = NettyPacket
            .buildPacket(JSONObject.toJSONString(fileInfo).getBytes(),
                PacketType.REPLICA_RECEIVE.value);
        NettyPacket response = netClient.sendSync(nettyPacket);
        if (response.getPackageType() != PacketType.REPLICA_RECEIVE.value) {
            log.error("请求类型不一致:", response.getPackageType());
            return false;
        }
        ServerResponse serverResponse = JSONObject
            .parseObject(new String(response.getBody()), ServerResponse.class);
        if (serverResponse.getSuccess()) {
            return true;
        }
        log.error("发送文件信息请求处理失败:{}", serverResponse.getErrorMsg());
        return false;

    }

    /**
     * 启动完成后全量上报存储信息(自身节点启动后或者nameNode重启)
     */
    public void sendAllFileInfo(List<FileInfo> infoList) {
        DataNodeRequest dataNodeRequest = DataNodeRequest.builder().healthyStatus(1)
            .hostName(dataNodeConfig.getHostName()).fileInfos(infoList).build();
        NettyPacket nettyPacket = NettyPacket
            .buildPacket(JSONObject.toJSONString(dataNodeRequest).getBytes(),
                PacketType.REPORT_STORAGE_INFO.value);
        netClient.send(nettyPacket);
    }


    /**
     * 针对nameNode消息监听处理类
     *
     * @param requestWrapper
     */
    private void callBack(RequestWrapper requestWrapper) {

    }


}
