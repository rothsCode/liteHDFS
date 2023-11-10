package com.rothsCode.litehdfs.datanode.handler;

import com.alibaba.fastjson.JSONObject;
import com.rothsCode.litehdfs.common.enums.FileTransferType;
import com.rothsCode.litehdfs.common.enums.PacketType;
import com.rothsCode.litehdfs.common.file.FileAppender;
import com.rothsCode.litehdfs.common.netty.client.DataNodeClient;
import com.rothsCode.litehdfs.common.netty.handler.AbstractDataHandler;
import com.rothsCode.litehdfs.common.netty.request.ClientTransferFileInfo;
import com.rothsCode.litehdfs.common.netty.request.CopyDataNodeRequest;
import com.rothsCode.litehdfs.common.netty.request.FileInfo;
import com.rothsCode.litehdfs.common.netty.request.NettyPacket;
import com.rothsCode.litehdfs.common.netty.request.RequestWrapper;
import com.rothsCode.litehdfs.common.netty.response.ServerResponse;
import com.rothsCode.litehdfs.common.netty.vo.DataNodeInfo;
import com.rothsCode.litehdfs.datanode.config.DataNodeConfig;
import com.rothsCode.litehdfs.datanode.vo.DataNodeStorageInfo;
import io.netty.channel.ChannelHandlerContext;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/11/5 16:12
 */
@Slf4j
public class DataNodeServerHandler extends AbstractDataHandler {

  private DataNodeConfig dataNodeConfig;

  private FileCallBackHandler fileCallBackHandler;

  private DataNodeStorageInfo storageInfo;

  //每个文件对应一个文件操作类
  private Map<String, FileAppender> transferFileMap = new ConcurrentHashMap<>();
  //件传输超时定时任务
  private Map<String, Long> transferFileTimeMap = new ConcurrentHashMap<>();

  /**
   * 复制节点客户端
   */
  private Map<String, List<DataNodeClient>> copyDataNodeClientMap = new ConcurrentHashMap<>();

  /**
   * 记录block写入响应情况，当复制节点都成功返回时才删除
   */
  private Map<String, AtomicInteger> blockHeadAckMap = new ConcurrentHashMap<>();

  private Map<String, AtomicInteger> blockTailAckMap = new ConcurrentHashMap<>();
  /**
   * 记录文件块与存储节点的关系
   */
  private Map<String, String> blkDataAddressMap = new ConcurrentHashMap<>();

  private String baseStoragePath;

  public DataNodeServerHandler(DataNodeStorageInfo storageInfo, DataNodeConfig dataNodeConfig,
      FileCallBackHandler fileCallBackHandler) {
    this.storageInfo = storageInfo;
    this.dataNodeConfig = dataNodeConfig;
    this.fileCallBackHandler = fileCallBackHandler;
    this.baseStoragePath = dataNodeConfig.getDataPath() + "\\" + dataNodeConfig.getServerPort();
  }

  // TODO 后续优化为时间轮算法
  public void handleTransferTimeOut(long timeOut) {
    for (Map.Entry<String, Long> entry : transferFileTimeMap.entrySet()) {
      String fileName = entry.getKey();
      Long transferTime = entry.getValue();
      if (System.currentTimeMillis() - transferTime >= timeOut) {
        log.info("{}:fileAppender is removed for timeOut", fileName);
        FileAppender fileAppender = transferFileMap.remove(fileName);
        if (fileAppender != null) {
          fileAppender.close();
        }
        transferFileTimeMap.remove(fileName);
      }

    }

  }

  @Override
  public boolean handleMsg(NettyPacket nettyPacket, ChannelHandlerContext ctx) {
    int packetValue = nettyPacket.getPackageType();
    PacketType packetType = PacketType.getEnum(packetValue);
    RequestWrapper requestWrapper = new RequestWrapper(nettyPacket.getSequence(), packetValue,
        ctx);
    byte[] body = nettyPacket.getBody();
    switch (packetType) {
      case CLIENT_CONNECT_DATA_NODE:
        clientConnectDataNode(body, requestWrapper);
        break;
      case TRANSFER_FILE:
        storageBlockChunk(requestWrapper, nettyPacket);
        break;
      case GET_FILE:
        getFile(requestWrapper, body);
        break;
      default:
        log.error("packetType illegal");
    }

    return true;
  }

  private void clientConnectDataNode(byte[] body, RequestWrapper requestWrapper) {
    CopyDataNodeRequest copyDataNodeRequest = JSONObject
        .parseObject(new String(body), CopyDataNodeRequest.class);
    Validate.notEmpty(copyDataNodeRequest.getCopyDataNodes(), "copyDataNodeInfo not existed");
    //获取对应的存储节点
    StringBuffer dataAddressBuffer = new StringBuffer();
    dataAddressBuffer.append(copyDataNodeRequest.getParentFileName()).append(">");
    int dataSize = copyDataNodeRequest.getCopyDataNodes().size();
    for (int i = 0; i < dataSize; i++) {
      DataNodeInfo dataNodeInfo = copyDataNodeRequest.getCopyDataNodes().get(i);
      if (i < dataSize - 1) {
        dataAddressBuffer.append(dataNodeInfo.getAddress()).append(",");
      } else {
        dataAddressBuffer.append(dataNodeInfo.getAddress());
      }
    }
    //移除主节点
    copyDataNodeRequest.getCopyDataNodes().remove(0);
    List<DataNodeClient> dataNodeClients = new ArrayList<>();
    for (DataNodeInfo copyDataNodeInfo : copyDataNodeRequest.getCopyDataNodes()) {
      //与复制节点依次建立连接
      DataNodeClient dataNodeClient = new DataNodeClient(copyDataNodeInfo);
      dataNodeClients.add(dataNodeClient);
    }
    //主节点保存ack信息
    AtomicInteger headAckCount = new AtomicInteger(copyDataNodeRequest.getCopyDataNodes().size());
    blockHeadAckMap.put(copyDataNodeRequest.getFileName(),
        headAckCount);
    AtomicInteger tailAckCount = new AtomicInteger(copyDataNodeRequest.getCopyDataNodes().size());
    blockTailAckMap.put(copyDataNodeRequest.getFileName(),
        tailAckCount);
    copyDataNodeClientMap.put(copyDataNodeRequest.getFileName(), dataNodeClients);
    //存储节点
    blkDataAddressMap.put(copyDataNodeRequest.getFileName(), dataAddressBuffer.toString());
    //响应
    requestWrapper.sendResponse(ServerResponse.success());
  }

  private void getFile(RequestWrapper requestWrapper, byte[] body) {
    //head
    ClientTransferFileInfo getFileRequest = JSONObject
        .parseObject(new String(body), ClientTransferFileInfo.class);
    String fileName = getFileRequest.getFileName();
    ClientTransferFileInfo headFile = ClientTransferFileInfo.builder()
        .fileName(fileName)
        .transferType(FileTransferType.HEAD.name()).build();
    requestWrapper.sendResponse(headFile);
    //body传输
    FileAppender fileAppender = new FileAppender(baseStoragePath, fileName);
    sendFileBodyData(requestWrapper, fileName, fileAppender);
    //tail
    ClientTransferFileInfo tailFileInfo = ClientTransferFileInfo.builder()
        .fileName(fileName).transferType(FileTransferType.TAIL.name())
        .blockCheckCode(fileAppender.getFileCheckCode()).build();
    requestWrapper.sendResponse(tailFileInfo);

  }

  private void sendFileBodyData(RequestWrapper requestWrapper, String fileName,
      FileAppender fileAppender) {
    int bufferSize = dataNodeConfig.getSendFileBufferSize();
    try {
      if (bufferSize > fileAppender.getChannel().size()) {
        bufferSize = (int) fileAppender.getChannel().size();
      }
      ByteBuffer buff = ByteBuffer.allocate(bufferSize);
      while (fileAppender.getChannel().read(buff) > 0) {
        ClientTransferFileInfo bodyFileInfo = ClientTransferFileInfo.builder()
            .fileName(fileName).transferType(FileTransferType.BODY.name()).body(buff.array())
            .build();
        requestWrapper.sendResponse(bodyFileInfo);
        buff.clear();
      }
    } catch (IOException e) {
    }
  }


  private void storageBlockChunk(RequestWrapper requestWrapper, NettyPacket nettyPacket) {
    ClientTransferFileInfo transferRequest = JSONObject
        .parseObject(new String(nettyPacket.getBody()), ClientTransferFileInfo.class);
    switch (FileTransferType.valueOf(transferRequest.getTransferType())) {
      case HEAD:
        handleFileHead(requestWrapper, transferRequest);
        break;
      case BODY:
        handleFileBody(requestWrapper, transferRequest);
        break;
      case TAIL:
        handleFileTail(requestWrapper, transferRequest);
        break;
      default:
        log.error("transferType illegal");
    }
    //将数据发送至副节点复制
    //followerNode skip reportNameNode
    if (blockTailAckMap.size() > 0) {
      sendDataToFollower(requestWrapper, nettyPacket, transferRequest);
    }
  }

  private void handleFileTail(RequestWrapper requestWrapper,
      ClientTransferFileInfo transferRequest) {
    String fileName = transferRequest.getFileName();
    //移除文件超时任务
    transferFileTimeMap.remove(fileName);
    //移除文件写入客户端
    FileAppender fileAppender = transferFileMap.remove(fileName);
    if (fileAppender == null) {
      requestWrapper
          .sendResponse(
              ServerResponse
                  .failByMsg("fileAppender tail is removed when handleFileTail:" + fileName));
      return;
    }
    try {
      // 校验文件数据一致性
      boolean checkFlag = fileAppender.checkBlockCheckCode(transferRequest.getBlockCheckCode());
      if (!checkFlag) {
        requestWrapper
            .sendResponse(ServerResponse.failByMsg("checkCrc32 false:" + fileName));
        return;
      }
      //followerNode skip reportNameNode
      if (blockTailAckMap.size() == 0) {
        requestWrapper
            .sendResponse(ServerResponse.success());
        log.debug("followerNode send tailAck:{}", transferRequest.getFileName());
        return;
      }
    } catch (Exception e) {
      log.error("fileAppender complete  error:{}", e);
    } finally {
      fileAppender.close();
    }
  }

  private void handleFileBody(RequestWrapper requestWrapper,
      ClientTransferFileInfo transferRequest) {
    String fileName = transferRequest.getFileName();
    FileAppender fileAppender = transferFileMap.get(fileName);
    if (fileAppender == null) {
      log.warn("{}:fileAppender is removed when append fileBody", fileName);
      fileAppender = new FileAppender(baseStoragePath, fileName);
      log.info("create fileAppender:{} when handleFileBody", fileName);
      transferFileMap.put(fileName, fileAppender);
      transferFileTimeMap.put(fileName, System.currentTimeMillis());
    }
    try {
      fileAppender.append(transferRequest.getBody());
      requestWrapper.sendResponse(ServerResponse.success());
    } catch (Exception e) {
      log.error("append fileBody error:{}", e);
    }
  }

  private void handleFileHead(RequestWrapper requestWrapper,
      ClientTransferFileInfo transferRequest) {
    String fileName = transferRequest.getFileName();
    FileAppender fileAppender = new FileAppender(
        dataNodeConfig.getDataPath() + "\\" + dataNodeConfig.getServerPort(), fileName);
    log.debug("create fileAppender:{}", fileName);
    transferFileMap.put(fileName, fileAppender);
    transferFileTimeMap.put(fileName, System.currentTimeMillis());
    //followerNode
    if (blockHeadAckMap.size() == 0) {
      log.debug("followNode send headAck:{}", fileName);
      requestWrapper.sendResponse(ServerResponse.success());
    }
  }

  private void sendDataToFollower(RequestWrapper requestWrapper, NettyPacket nettyPacket,
      ClientTransferFileInfo transferRequest) {
    List<DataNodeClient> copyDataNodeClients = copyDataNodeClientMap
        .get(transferRequest.getFileName());
    if (CollectionUtils.isNotEmpty(copyDataNodeClients)) {
      for (DataNodeClient dataNodeClient : copyDataNodeClients) {
        CompletableFuture<NettyPacket> nettyResponseFuture = dataNodeClient.asyncSend(nettyPacket);
        if (nettyResponseFuture == null) {
          log.error("{}:sendDataToFollower asyncSend error", transferRequest.getFileName());
          continue;
        }
        nettyResponseFuture.whenCompleteAsync((response, throwable) -> {
          handleAckResponse(response, throwable, transferRequest, requestWrapper);
        });
      }
    }
  }

  /**
   * 仅处理 tail类型响应
   *
   * @param response
   * @param throwable
   * @param transferRequest
   * @param requestWrapper
   */
  private void handleAckResponse(NettyPacket response, Throwable throwable,
      ClientTransferFileInfo transferRequest,
      RequestWrapper requestWrapper) {
    if (throwable != null) {
      log.error("handleAckResponse error", throwable);
    }
    if (response == null) {
      requestWrapper.sendResponse(ServerResponse.failByMsg("copyDataNode no response"));
      return;
    }
    ServerResponse serverResponse = JSONObject
        .parseObject(new String(response.getBody()), ServerResponse.class);
    if (!serverResponse.getSuccess()) {
      requestWrapper.sendResponse(ServerResponse.failByMsg(serverResponse.getErrorMsg()));
      return;
    }
    switch (FileTransferType.valueOf(transferRequest.getTransferType())) {
      case HEAD:
        AtomicInteger headAckCount = blockHeadAckMap.get(transferRequest.getFileName());
        if (headAckCount != null && headAckCount.get() > 0) {
          if (headAckCount.decrementAndGet() == 0) {
            blockHeadAckMap.remove(transferRequest.getFileName());
            //全部相应后则返回存储成功的消息
            requestWrapper.sendResponse(ServerResponse.success());
            log.debug("{}:blockHeadAck complete", transferRequest.getFileName());
          }
        }
        break;
      case BODY:
        break;
      case TAIL:
        AtomicInteger tailAckCount = blockTailAckMap.get(transferRequest.getFileName());
        if (tailAckCount != null && tailAckCount.get() > 0) {
          //全部相应后则返回存储成功的消息
          if (tailAckCount.decrementAndGet() == 0) {
            blockTailAckMap.remove(transferRequest.getFileName());
            //文件存储成功后向nameNode 上报成功信息,保存对应的元数据，然后给客户端返回存储成功信息
            String blkDataAddressInfo = blkDataAddressMap.remove(transferRequest.getFileName());
            if (StringUtils.isEmpty(blkDataAddressInfo)) {
              requestWrapper.sendResponse(ServerResponse.failByMsg("blkDataAddressInfo is empty"));
              return;
            }
            String[] blkDataAddressInfoRelation = blkDataAddressInfo.split(">");
            //落盘后更新data节点文件索引以及向nameNode发送存储成功的消息
            FileInfo fileInfo = FileInfo.builder()
                .createTime(System.currentTimeMillis()).fileName(transferRequest.getFileName())
                .parentFileName(blkDataAddressInfoRelation[0])
                .blkDataNode(transferRequest.getFileName() + ">" + blkDataAddressInfoRelation[1])
                .build();
            ServerResponse nameNodeAckResponse = fileCallBackHandler.fileComplete(fileInfo);
            if (!nameNodeAckResponse.getSuccess()) {
              requestWrapper
                  .sendResponse(ServerResponse.failByMsg(nameNodeAckResponse.getErrorMsg()));
              return;
            }
            requestWrapper.sendResponse(ServerResponse.success());
            log.debug("{}:transferFile success", transferRequest.getFileName());
          }
        }
        break;
      default:
        log.error("transferType illegal");

    }
  }

  @Override
  public Executor getExecutor() {
    return null;
  }

  private void readFile(RequestWrapper requestWrapper, String fileName, File file) {
    if (!file.exists()) {
      return;
    }
    RandomAccessFile rds = null;
    try {
      int hasRead = 0;
      int readLength = 0;
      rds = new RandomAccessFile(file, "r");
      int remainFileLength = (int) rds.length();
      while (remainFileLength > 0) {
        long bodyLength = Math.min(1024 * 1024, remainFileLength);
        byte[] bodyByte = new byte[(int) bodyLength];
        hasRead = rds.read(bodyByte);
        remainFileLength = (remainFileLength - hasRead);
        ClientTransferFileInfo bodyFileInfo = ClientTransferFileInfo.builder()
            .fileSize(hasRead)
            .fileName(fileName).transferType(FileTransferType.BODY.name()).body(bodyByte)
            .build();
        requestWrapper.sendResponse(bodyFileInfo);
        readLength += hasRead;
        double progress = (double) readLength / file.length();
      }
    } catch (IOException e) {
    } finally {
      try {
        if (rds != null) {
          rds.close();
        }
      } catch (IOException e) {
      }
    }
  }

}
