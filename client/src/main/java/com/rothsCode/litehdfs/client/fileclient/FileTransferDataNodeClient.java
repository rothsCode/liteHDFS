package com.rothsCode.litehdfs.client.fileclient;

import com.alibaba.fastjson.JSONObject;
import com.rothsCode.litehdfs.client.ClientConfig;
import com.rothsCode.litehdfs.common.enums.FileTransferType;
import com.rothsCode.litehdfs.common.enums.PacketType;
import com.rothsCode.litehdfs.common.file.FileAppender;
import com.rothsCode.litehdfs.common.netty.client.DataNodeClient;
import com.rothsCode.litehdfs.common.netty.request.ClientTransferFileInfo;
import com.rothsCode.litehdfs.common.netty.request.NettyPacket;
import com.rothsCode.litehdfs.common.netty.response.ServerResponse;
import java.io.File;
import java.nio.ByteBuffer;

/**
 * @author roths
 * @Description:
 * @date 2023/11/8 11:20
 */
public class FileTransferDataNodeClient {

  private DataNodeClient dataNodeClient;

  private ClientConfig clientConfig;

  public FileTransferDataNodeClient(DataNodeClient dataNodeClient, ClientConfig clientConfig) {
    this.dataNodeClient = dataNodeClient;
    this.clientConfig = clientConfig;
  }

  /**
   * 往dataNode传输文件数据
   *
   * @param fileName
   * @param file
   */
  public ServerResponse transferFile(String fileName, File file, long blockIndex,
      long blockLength) {
    //先发送头信息
    ClientTransferFileInfo clientTransferFileInfo = ClientTransferFileInfo.builder()
        .fileSize(file.length())
        .fileName(fileName).transferType(FileTransferType.HEAD.name()).build();
    NettyPacket headPacket = NettyPacket
        .buildPacket(JSONObject.toJSONString(clientTransferFileInfo).getBytes(),
            PacketType.TRANSFER_FILE.value);
    NettyPacket headResponse = dataNodeClient.sendSync(headPacket);
    if (headResponse == null) {
      return ServerResponse.failByMsg("send head timeOut");
    }
    ServerResponse serverHeadResponse = JSONObject
        .parseObject(new String(headResponse.getBody()), ServerResponse.class);
    if (!serverHeadResponse.getSuccess()) {
      return serverHeadResponse;
    }
    // 从本地获取文件流数据再发送文件数据
    String blockCheckCode = sendBlockFileBody(fileName, file, blockIndex, blockLength);
    //发生文件尾标识文件传输完毕
    ClientTransferFileInfo tailFileInfo;
    tailFileInfo = ClientTransferFileInfo.builder()
        .fileName(fileName).transferType(FileTransferType.TAIL.name())
        .blockCheckCode(blockCheckCode).build();
    NettyPacket tailPacket = NettyPacket
        .buildPacket(JSONObject.toJSONString(tailFileInfo).getBytes(),
            PacketType.TRANSFER_FILE.value);
    NettyPacket tailResponse = dataNodeClient.sendSync(tailPacket);
    if (tailResponse == null) {
      return ServerResponse.failByMsg("send tail timeOut");
    }
    ServerResponse serverResponse = JSONObject
        .parseObject(new String(tailResponse.getBody()), ServerResponse.class);
    return serverResponse;
  }

  private String sendBlockFileBody(String fileName, File file, long blockIndex, long blockLength) {
    FileAppender blockFileBodyAppender = new FileAppender(file);
    //获取文件校验码
    String blockCheckCode = blockFileBodyAppender
        .getBlockFileCheckCode((int) blockIndex, (int) clientConfig.getFileBlockSize(),
            (int) blockLength);
    long remainFileLength = blockLength;
    while (remainFileLength > 0) {
      long bodyLength = Math.min(clientConfig.getFileBlockPacketSize(), remainFileLength);
      ByteBuffer buffer = blockFileBodyAppender.readBody((int) bodyLength);
      remainFileLength = (remainFileLength - bodyLength);
      //文件流传输
      ClientTransferFileInfo bodyFileInfo = ClientTransferFileInfo.builder().fileSize(bodyLength)
          .fileName(fileName).transferType(FileTransferType.BODY.name()).body(buffer.array())
          .build();
      NettyPacket bodyPacket = NettyPacket
          .buildPacket(JSONObject.toJSONString(bodyFileInfo).getBytes(),
              PacketType.TRANSFER_FILE.value);
      dataNodeClient.send(bodyPacket);
    }
    return blockCheckCode;
  }

  public void shutDown() {
    dataNodeClient.shutDown();
  }

}
