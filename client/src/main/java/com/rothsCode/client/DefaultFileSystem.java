package com.rothsCode.client;

import com.alibaba.fastjson.JSONObject;
import com.rothsCode.net.DataNodeInfo;
import com.rothsCode.net.DefaultScheduler;
import com.rothsCode.net.FileResponse;
import com.rothsCode.net.NetClient;
import com.rothsCode.net.PacketType;
import com.rothsCode.net.request.ClientToNameNodeRequest;
import com.rothsCode.net.request.NettyPacket;
import com.rothsCode.net.request.RequestWrapper;
import com.rothsCode.net.response.ServerResponse;
import java.io.File;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/11/11 13:57
 */
@Slf4j
public class DefaultFileSystem implements FileSystem {

    private NetClient nameNodeClient;
    private ClientConfig clientConfig;
    private DefaultScheduler defaultScheduler;

    public DefaultFileSystem(ClientConfig clientConfig) {
        this.defaultScheduler = new DefaultScheduler("FSClient_Scheduler");
        this.clientConfig = clientConfig;
        nameNodeClient = new NetClient("FileClient_", clientConfig.getServerHostName(),
            clientConfig.getServerPort(), clientConfig.getRetryTime(), defaultScheduler);
    }

    public void start() {
        //连接成功后回调
        nameNodeClient.addPacketLister(this::callBack);
        //连接成功后处理流程
        nameNodeClient.addConnectedLister(connected -> {
            if (connected) {
                //发送用户登录请求
                defaultScheduler.scheduleOnce("登录认证任务",
                    () -> login(clientConfig.getUserName(), clientConfig.getPassword()));
                //登录成功后需要唤醒主线程
                synchronized (this) {
                    notifyAll();
                }
            }
        });
        nameNodeClient.addFailConnectedLister(() -> {
            System.out.println("客户端与nameNode断开,可能宕机--");
        });
        nameNodeClient.startConnect();
        synchronized (this) {
            try {
                wait();
                System.out.println("客户端启动成功---");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void shutDown() {
        if (nameNodeClient != null) {
            nameNodeClient.shutDown();
        }
    }


    @Override
    public boolean makeDir(String path) {
        ClientToNameNodeRequest request = ClientToNameNodeRequest.builder()
            .path(path).userName(clientConfig.getUserName()).authToken(clientConfig.getAuthToken())
            .build();
        NettyPacket nettyPacket = NettyPacket
            .buildPacket(JSONObject.toJSONString(request).getBytes(), PacketType.MKDIR.value);
        NettyPacket response = nameNodeClient.sendSync(nettyPacket);
        if (response == null) {
            return false;
        }
        log.info("创建文件目录返回结果:{}" + JSONObject.toJSONString(response));
        ServerResponse serverResponse = JSONObject
            .parseObject(new String(response.getBody()), ServerResponse.class);
        if (serverResponse.getSuccess()) {
            return true;
        }
        log.error("创建文件目录报错:{}", serverResponse.getErrorMsg());
        return false;
    }

    /**
     * 上传先发创建文件请求到nameNode ，nameNode 分配存储文件节点的数据节点返回给客户端，并记录文件与数据节点的关联数据
     * 客户端获得dataNode节点位置后，再往对应dataNode 发送文件生成请求，dataNode收到请求后将文件存储在对应服务器磁盘上
     *
     * @param path
     * @param file
     * @return
     */
    @Override
    public boolean uploadFile(String path, File file) {
        ClientToNameNodeRequest request = ClientToNameNodeRequest.builder()
            .userName(clientConfig.getUserName()).authToken(clientConfig.getAuthToken()).path(path)
            .fileName(file.getName()).fileSize(file.length()).userId(11).build();
        NettyPacket nettyPacket = NettyPacket
            .buildPacket(JSONObject.toJSONString(request).getBytes(), PacketType.CREATE_FILE.value);
        NettyPacket response = nameNodeClient.sendSync(nettyPacket);
        if (response == null) {
            return false;
        }
        log.info("上传文件返回结果:" + JSONObject.toJSONString(response));
        if (response.getPackageType() != PacketType.CREATE_FILE.value) {
            log.error("请求类型不一致:", response.getPackageType());
            return false;
        }
        ServerResponse serverResponse = JSONObject
            .parseObject(new String(response.getBody()), ServerResponse.class);
        if (!serverResponse.getSuccess()) {
            log.error("未找到可用的数据节点:", serverResponse.getErrorMsg());
            return false;
        }
        //可用的数据节点
        FileResponse fileResponse = JSONObject
            .parseObject(JSONObject.toJSONString(serverResponse.getData()), FileResponse.class);
        List<DataNodeInfo> dataNodeInfos = fileResponse.getDataNodeInfos();
        String fileName = fileResponse.getFileName();
        //往dataNode发送创建文件请求
        for (int i = 0; i < dataNodeInfos.size(); i++) {
            DataNodeInfo dataNodeInfo = dataNodeInfos.get(i);
            NetClient dataNodeClient = new NetClient("clientDataNodeClient_",
                dataNodeInfo.getHostName(), dataNodeInfo.getPort(), clientConfig.getRetryTime(),
                defaultScheduler);
            FileTransferClient fileTransferClient = new FileTransferClient(dataNodeClient);
            fileTransferClient.startDataNodeClient();
            boolean success = fileTransferClient.transferFile(fileName, file);
            fileTransferClient.shutDown();
            if (!success) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean removeFile(String filePath) {
        return false;
    }

    @Override
    public void downFile(String fileName, String destPath) {
        //先发送到namenode获取datanode
        ClientToNameNodeRequest request = ClientToNameNodeRequest.builder()
            .userName(clientConfig.getUserName()).authToken(clientConfig.getAuthToken())
            .fileName(fileName).build();
        NettyPacket nettyPacket = NettyPacket
            .buildPacket(JSONObject.toJSONString(request).getBytes(),
                PacketType.GET_DATA_NODE_FOR_FILE.value);
        NettyPacket response = nameNodeClient.sendSync(nettyPacket);
        if (response == null) {
            return;
        }
        ServerResponse serverResponse = JSONObject
            .parseObject(new String(response.getBody()), ServerResponse.class);
        if (serverResponse.getSuccess() && serverResponse.getData() != null) {
            DataNodeInfo dataNodeInfo = JSONObject
                .parseObject(JSONObject.toJSONString(serverResponse.getData()), DataNodeInfo.class);
            //发生请求到dataNode
            NetClient dataNodeClient = new NetClient("downFileClient_", dataNodeInfo.getHostName(),
                dataNodeInfo.getPort(), clientConfig.getRetryTime(), defaultScheduler);
            FileTransferClient fileTransferClient = new FileTransferClient(dataNodeClient);
            fileTransferClient.startDataNodeClient();
            fileTransferClient.downFile(fileName, destPath);
        }


    }

    @Override
    public List<String> getChildDirs(String filePath) {
        ClientToNameNodeRequest request = ClientToNameNodeRequest.builder()
            .userName(clientConfig.getUserName()).authToken(clientConfig.getAuthToken())
            .path(filePath).build();
        NettyPacket nettyPacket = NettyPacket
            .buildPacket(JSONObject.toJSONString(request).getBytes(),
                PacketType.CLIENT_LIST_FILES.value);
        NettyPacket response = nameNodeClient.sendSync(nettyPacket);
        if (response == null) {
            return Collections.emptyList();
        }
        if (response.getPackageType() != PacketType.CLIENT_LIST_FILES.value) {
            log.error("请求类型不一致:", response.getPackageType());
            return Collections.emptyList();
        }
        ServerResponse serverResponse = JSONObject
            .parseObject(new String(response.getBody()), ServerResponse.class);
        if (serverResponse.getSuccess() && serverResponse.getData() != null) {
            return (List<String>) serverResponse.getData();
        }
        return Collections.emptyList();
    }

    @Override
    public boolean login(String userName, String password) {
        ClientToNameNodeRequest request = ClientToNameNodeRequest.builder()
            .userName(userName).passWord(password).build();
        NettyPacket nettyPacket = NettyPacket
            .buildPacket(JSONObject.toJSONString(request).getBytes(),
                PacketType.AUTHENTICATE.value);
        NettyPacket response = nameNodeClient.sendSync(nettyPacket);
        if (response == null) {
            this.shutDown();
            return false;
        }
        ServerResponse serverResponse = JSONObject
            .parseObject(new String(response.getBody()), ServerResponse.class);
        if (serverResponse.getSuccess()) {
            String authToken = (String) serverResponse.getData();
            clientConfig.setAuthToken(authToken);
            System.out.println("客户端成功连接认证");
            return true;
        }
        this.shutDown();
        return false;
    }

    /**
     * 客户端连接后回调信息
     */
    private void callBack(RequestWrapper requestWrapper) {

    }

}
