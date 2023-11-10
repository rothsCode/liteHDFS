package com.rothsCode.litehdfs.client;

import com.alibaba.fastjson.JSONObject;
import com.rothsCode.litehdfs.client.fileclient.DownFileDataNodeClient;
import com.rothsCode.litehdfs.client.fileclient.FileTransferDataNodeClient;
import com.rothsCode.litehdfs.client.netty.NameNodeClient;
import com.rothsCode.litehdfs.common.enums.DataNodeType;
import com.rothsCode.litehdfs.common.enums.PacketType;
import com.rothsCode.litehdfs.common.netty.LifeCycle;
import com.rothsCode.litehdfs.common.netty.client.DataNodeClient;
import com.rothsCode.litehdfs.common.netty.request.ClientToNameNodeRequest;
import com.rothsCode.litehdfs.common.netty.request.CopyDataNodeRequest;
import com.rothsCode.litehdfs.common.netty.request.FileInfo;
import com.rothsCode.litehdfs.common.netty.request.NettyPacket;
import com.rothsCode.litehdfs.common.netty.response.FileResponse;
import com.rothsCode.litehdfs.common.netty.response.ServerResponse;
import com.rothsCode.litehdfs.common.netty.vo.DataNodeInfo;
import java.io.File;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/11/11 13:57
 */
@Slf4j
public class DefaultFileSystem implements FileSystemClient, LifeCycle {

  private ClientConfig clientConfig;
  /**
   * nameNodeClient
   */
  private NameNodeClient nameNodeClient;


  public DefaultFileSystem(ClientConfig clientConfig) {
    this.clientConfig = clientConfig;
  }


  @Override
  public void init() {
    nameNodeClient = new NameNodeClient(clientConfig);
    nameNodeClient.init();
  }

  @Override
  public void start() {
    nameNodeClient.start();
  }

  @Override
  public void shutDown() {
    if (nameNodeClient != null) {
      nameNodeClient.shutDown();
    }
  }

  @Override
  public ServerResponse makeDir(String path) {
    ClientToNameNodeRequest request = ClientToNameNodeRequest.builder()
        .path(path).userName(clientConfig.getUserName()).authToken(clientConfig.getAuthToken())
        .build();
    NettyPacket nettyPacket = NettyPacket
        .buildPacket(JSONObject.toJSONString(request).getBytes(), PacketType.MKDIR.value);
    NettyPacket response = nameNodeClient.sendSync(nettyPacket);
    if (response == null) {
      return ServerResponse.failByMsg("response timeout");
    }
    ServerResponse serverResponse = JSONObject
        .parseObject(new String(response.getBody()), ServerResponse.class);
    return serverResponse;
  }


  /**
   * 上传先发创建文件请求到nameNode ，nameNode 分配存储文件节点的数据节点返回给客户端，并记录文件与数据节点的关联数据
   * 客户端获得dataNode节点位置后，再往对应dataNode 发送文件生成请求，dataNode收到请求后将文件存储在对应服务器磁盘上 上传流程
   * 1:对文件进行切分操作,切分为多个block client 发起文件上传请求，通过 RPC 与 NameNode 建立通讯，NameNode
   * 检查目标文件是否已存在，父目录是否存在，返回是否可以上传； 2:client 请求第一个 block 该传输到哪些 DataNode 服务器上； 3:NameNode
   * 根据配置文件中指定的备份数量及副本放置策略进行文件分配，返回可用的 DataNode 的地址，如：A，B，C； 4:client 请求3台 DataNode 中的一台A上传数据（本质上是一个
   * RPC 调用），A收到请求会同时调用调用B,调用C， B C 将ack结果告知A, 节点A将 pipeline ack 发送给client; 7:当一个 block
   * 传输完成之后，client 再次请求 NameNode 上传第二个 block 到服务器。
   *
   * @param file
   * @return
   */
  @Override
  public ServerResponse uploadFile(File file) {
    //1 对文件进行切分为多个block
    long fileLength = file.length();
    long blockSize = fileLength / clientConfig.getFileBlockSize() + 1;
    //2 对文件按切分后的大小顺序依次传输
    for (long blockIndex = 0; blockIndex < blockSize; blockIndex++) {
      long blockLength = clientConfig.getFileBlockSize();
      if (blockIndex == blockSize - 1) {
        if (blockSize == 1) {
          blockLength = fileLength;
        } else {
          blockLength = fileLength - clientConfig.getFileBlockSize() * blockIndex;
        }
      }
      //3 传输单个block文件部分
      ServerResponse blockResponse = uploadBlock(file, blockIndex, blockLength);
      if (!blockResponse.getSuccess()) {
        return blockResponse;
      }
    }
    return ServerResponse.success();
  }

  /**
   * 发送单个文件块
   *
   * @param file
   * @return
   */
  private ServerResponse uploadBlock(File file, long blockIndex, long blockLength) {
    //1向nameNode请求获取对应的dataNode
    ServerResponse nameNodeServerResponse = getDataInfoFromNameNode(file, blockIndex, blockLength);
    if (!nameNodeServerResponse.getSuccess()) {
      return nameNodeServerResponse;
    }
    // 2.1校验可用的数据节点
    FileResponse fileResponse = JSONObject
        .parseObject(JSONObject.toJSONString(nameNodeServerResponse.getData()), FileResponse.class);
    List<DataNodeInfo> dataNodeInfos = fileResponse.getDataNodeInfos();
    if (CollectionUtils.isEmpty(dataNodeInfos)) {
      return ServerResponse.failByMsg("dataNodeInfos is empty");
    }
    String fileName = fileResponse.getFileName();
    DataNodeInfo dataNodeInfo = dataNodeInfos.get(0);
    // 2.2client与dataNode建立传输连接
    DataNodeClient dataNodeClient = new DataNodeClient(dataNodeInfo);
    CopyDataNodeRequest copyDataNodeRequest = new CopyDataNodeRequest();
    copyDataNodeRequest.setFileName(fileName);
    copyDataNodeRequest.setParentFileName(fileResponse.getParentFileName());
    copyDataNodeRequest.setNodeType(DataNodeType.MASTER.getValue());
    copyDataNodeRequest.setCopyDataNodes(dataNodeInfos);
    NettyPacket connectPacket = NettyPacket
        .buildPacket(JSONObject.toJSONString(copyDataNodeRequest).getBytes(),
            PacketType.CLIENT_CONNECT_DATA_NODE.value);
    NettyPacket connectResponse = dataNodeClient.sendSync(connectPacket);
    if (connectResponse == null) {
      return ServerResponse.failByMsg("clientConnectDataNodeResponse timeout");
    }
    //3建立连接后开始向dataNode传输文件
    FileTransferDataNodeClient fileTransferDataNodeClient = new FileTransferDataNodeClient(
        dataNodeClient, clientConfig);
    ServerResponse transferBlockResponse = fileTransferDataNodeClient
        .transferFile(fileName, file, blockIndex, blockLength);
    fileTransferDataNodeClient.shutDown();
    return transferBlockResponse;
  }

  private ServerResponse getDataInfoFromNameNode(File file, long blockIndex, long blockLength) {
    ClientToNameNodeRequest request = ClientToNameNodeRequest.builder()
        .userName(clientConfig.getUserName()).authToken(clientConfig.getAuthToken())
        .path(clientConfig.getStorageDirectory())
        .fileName(file.getName()).blockSize(blockLength).blockIndex(blockIndex).build();
    NettyPacket nettyPacket = NettyPacket
        .buildPacket(JSONObject.toJSONString(request).getBytes(), PacketType.CREATE_FILE.value);
    NettyPacket response = nameNodeClient.sendSync(nettyPacket);
    if (response == null) {
      return ServerResponse.failByMsg("uploadBlock response timeout");
    }
    if (response.getPackageType() != PacketType.CREATE_FILE.value) {
      return ServerResponse.failByMsg("uploadBlock packageType error");
    }
    ServerResponse serverResponse = JSONObject
        .parseObject(new String(response.getBody()), ServerResponse.class);
    return serverResponse;
  }

  @Override
  public ServerResponse deleteFile(String filePath) {
    return null;
  }

  public ServerResponse downFile(String fileName, String localDownloadPath) {
    //先发送到nameNode获取文件块对应的dataNode地址
    ClientToNameNodeRequest request = ClientToNameNodeRequest.builder()
        .userName(clientConfig.getUserName()).authToken(clientConfig.getAuthToken())
        .fileName(fileName).build();
    NettyPacket nettyPacket = NettyPacket
        .buildPacket(JSONObject.toJSONString(request).getBytes(),
            PacketType.GET_DATA_NODE_FOR_FILE.value);
    NettyPacket response = nameNodeClient.sendSync(nettyPacket);
    if (response == null) {
      return ServerResponse.failByMsg("downFileResponse timeout");
    }
    ServerResponse serverResponse = JSONObject
        .parseObject(new String(response.getBody()), ServerResponse.class);
    if (!serverResponse.getSuccess() || serverResponse.getData() == null) {
      return serverResponse;
    }
    FileInfo fileInfo = JSONObject
        .parseObject(JSONObject.toJSONString(serverResponse.getData()), FileInfo.class);
    //发生请求到dataNode
    if (StringUtils.isEmpty(fileInfo.getBlkDataNode())) {
      return ServerResponse.failByMsg("blkDataNode is empty");
    }
    //blk1>dn1,dn2;blk2>dn4,dn5
    String[] blkDataNodes = fileInfo.getBlkDataNode().split(";");
    if (blkDataNodes.length == 0) {
      return ServerResponse.failByMsg("blkDataNode is empty");
    }

    //获取到地址后发生请求获取文件块
    DownFileDataNodeClient downFileDataNodeClient = new DownFileDataNodeClient();
    int blkLength = blkDataNodes.length;
    for (int i = 0; i < blkLength; i++) {
      // 简单取第一个地址 TODO 感知网络最近节点
      String[] fileDataAddress = blkDataNodes[i].split(">");
      String blockFileName = fileDataAddress[0];
      String[] dataNodeAddress = fileDataAddress[1].split(",");
      String[] hostAndPort = dataNodeAddress[1].split(":");
      DataNodeInfo dataNodeInfo = new DataNodeInfo();
      dataNodeInfo.setIp(hostAndPort[0]);
      dataNodeInfo.setPort(Integer.parseInt(hostAndPort[1]));
      DataNodeClient blockDataNodeClient = new DataNodeClient(dataNodeInfo);
      downFileDataNodeClient.putDataNodeClient(blockFileName, blockDataNodeClient);
      try {
        downFileDataNodeClient
            .downBlockFile(fileName, blockFileName, localDownloadPath, blkLength - i == 1);
      } catch (Exception e) {
        log.error("downFile error:{}", e);
        return ServerResponse.failByMsg(e.getMessage());
      }
    }
    return ServerResponse.success();
  }

  @Override
  public ServerResponse getChildDirs(String filePath) {
    ClientToNameNodeRequest request = ClientToNameNodeRequest.builder()
        .userName(clientConfig.getUserName()).authToken(clientConfig.getAuthToken())
        .path(filePath).build();
    NettyPacket nettyPacket = NettyPacket
        .buildPacket(JSONObject.toJSONString(request).getBytes(),
            PacketType.CLIENT_LIST_FILES.value);
    NettyPacket response = nameNodeClient.sendSync(nettyPacket);
    if (response == null) {
      return ServerResponse.failByMsg("timeout");
    }
    if (response.getPackageType() != PacketType.CLIENT_LIST_FILES.value) {
      return ServerResponse.failByMsg("packetType error");
    }
    ServerResponse serverResponse = JSONObject
        .parseObject(new String(response.getBody()), ServerResponse.class);
    return serverResponse;
  }

  @Override
  public ServerResponse login(String userName, String password) {
//    ClientToNameNodeRequest request = ClientToNameNodeRequest.builder()
//        .userName(userName).passWord(password).build();
//    NettyPacket nettyPacket = NettyPacket
//        .buildPacket(JSONObject.toJSONString(request).getBytes(),
//            PacketType.AUTHENTICATE.value);
//    NettyPacket response = nameNodeClient.sendSync(nettyPacket);
//    if (response == null) {
//      this.shutDown();
//      return false;
//    }
//    ServerResponse serverResponse = JSONObject
//        .parseObject(new String(response.getBody()), ServerResponse.class);
//    if (serverResponse.getSuccess()) {
//      String authToken = (String) serverResponse.getData();
//      clientConfig.setAuthToken(authToken);
//      log.info("{}:客户端成功连接认证", userName);
//      return true;
//    }
//    this.shutDown();
    return null;
  }

//  /**
//   * 客户端连接后回调信息
//   */
//  private void callBack(RequestWrapper requestWrapper) {
//
//  }
}
