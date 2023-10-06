package com.rothsCode.client;

import com.alibaba.fastjson.JSONObject;
import com.rothsCode.net.FileTransferType;
import com.rothsCode.net.NetClient;
import com.rothsCode.net.NettyPacketListener;
import com.rothsCode.net.PacketType;
import com.rothsCode.net.request.ClientTransferFileInfo;
import com.rothsCode.net.request.NettyPacket;
import com.rothsCode.net.request.RequestWrapper;
import com.rothsCode.net.response.ServerResponse;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

/**
 * @author rothsCode
 * @Description: 文件客户端
 * @date 2021/11/15 17:06
 */
@Slf4j
public class FileTransferClient {

  private NetClient netClient;
  private Map<String, ClientFileAppender> transferFileMap = new ConcurrentHashMap<>();
  private Map<String, String> downFileMap = new ConcurrentHashMap<>();

  public FileTransferClient(NetClient netClient) {
    this.netClient = netClient;
  }

  public void startDataNodeClient() {
    //连接成功后回调
    netClient.addPacketLister(new ClientDataNodeListener());
    //连接成功后处理流程
    netClient.addConnectedLister(connected -> {
      if (connected) {
        System.out.println("客户端成功连接dataNode");
        //连接成功后需要唤醒主线程
        synchronized (this) {
          notifyAll();
        }
      }
    });
    netClient.addFailConnectedLister(() -> {
      System.out.println("客户端与dataNode断开,可能宕机--");
    });
    netClient.startConnect();
    synchronized (this) {
      try {
        wait();
        System.out.println("dataNode客户端启动成功---");
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

  }

  /**
   * 往dataNode传输文件数据
   *
   * @param fileName
   * @param file
   */
  public boolean transferFile(String fileName, File file) {
    if (!file.exists()) {
      return false;
    }
    //先发送头信息
    ClientTransferFileInfo clientTransferFileInfo = ClientTransferFileInfo.builder()
        .fileSize(file.length())
        .fileType(fileName.substring(fileName.lastIndexOf(".") + 1))
        .fileName(fileName).transferType(FileTransferType.HEAD.name()).build();
    NettyPacket headPacket = NettyPacket
        .buildPacket(JSONObject.toJSONString(clientTransferFileInfo).getBytes(),
            PacketType.TRANSFER_FILE.value);
    NettyPacket headResponse = netClient.sendSync(headPacket);
    if (headResponse == null) {
      return false;
    }
    ServerResponse serverHeadResponse = JSONObject
        .parseObject(new String(headResponse.getBody()), ServerResponse.class);
    if (!serverHeadResponse.getSuccess()) {
      log.info("发送文件头失败:{}", fileName);
      return false;
    }
    log.info("发送文件头:{}", fileName);
    // 从本地获取文件流数据再发送文件数据
    handleFile(fileName, file);
    //发生文件尾标识文件传输完毕
    ClientTransferFileInfo tailFileInfo = null;
    try {
      tailFileInfo = ClientTransferFileInfo.builder()
          .fileName(fileName).transferType(FileTransferType.TAIL.name())
          .crc32(FileUtils.checksumCRC32(file)).build();
    } catch (IOException e) {
      e.printStackTrace();
    }
    NettyPacket tailPacket = NettyPacket
        .buildPacket(JSONObject.toJSONString(tailFileInfo).getBytes(),
            PacketType.TRANSFER_FILE.value);
    NettyPacket tailResponse = netClient.sendSync(tailPacket);
    if (tailResponse == null) {
      return false;
    }
    ServerResponse serverResponse = JSONObject
        .parseObject(new String(tailResponse.getBody()), ServerResponse.class);
    log.info("发送文件尾--发生完毕:{}", fileName);
    //发送成功后关闭客户端
    return serverResponse.getSuccess();
  }

  private void handleFile(String fileName, File file) {
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
        ClientTransferFileInfo bodyFileInfo = ClientTransferFileInfo.builder().fileSize(hasRead)
            .fileName(fileName).transferType(FileTransferType.BODY.name()).body(bodyByte).build();
        NettyPacket bodyPacket = NettyPacket
            .buildPacket(JSONObject.toJSONString(bodyFileInfo).getBytes(),
                PacketType.TRANSFER_FILE.value);
        netClient.sendSync(bodyPacket);
        log.info("发送文件内容:{}", fileName);
        readLength += hasRead;
        double progress = (double) readLength / file.length();
        log.info("发送文件内容进度:{}", progress);
        //TODO进度监控回调
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (rds != null) {
          rds.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 关闭客户端回收资源
   */
  public void shutDown() {
    netClient.shutDown();
  }

  /**
   * 下载文件
   */
  public void downFile(String fileName, String destPath) {
    ClientTransferFileInfo clientTransferFileInfo = ClientTransferFileInfo.builder()
        .fileName(fileName).build();
    NettyPacket headPacket = NettyPacket
        .buildPacket(JSONObject.toJSONString(clientTransferFileInfo).getBytes(),
            PacketType.GET_FILE.value);
    downFileMap.put(fileName, destPath);
    netClient.send(headPacket);
  }

  private class ClientDataNodeListener implements NettyPacketListener {

    @Override
    public void callBack(RequestWrapper requestWrapper) {
      //下载dataNode回调处理
      NettyPacket nettyPacket = requestWrapper.getNettyPacket();
      PacketType packageType = PacketType.getEnum(nettyPacket.getPackageType());
      byte[] body = nettyPacket.getBody();
      if (body == null || body.length <= 0) {
        return;
      }
      ClientTransferFileInfo transferRequest = JSONObject
          .parseObject(new String(body), ClientTransferFileInfo.class);
      switch (packageType) {
        case GET_FILE:
          if (FileTransferType.HEAD.name().equals(transferRequest.getTransferType())) {
            //生成写文件客户端
            String fileName = transferRequest.getFileName();
            String destPath = downFileMap.get(fileName);
            if (destPath == null) {
              break;
            }
            ClientFileAppender fileAppender = new ClientFileAppender(destPath, fileName);
            transferFileMap.put(fileName, fileAppender);

          } else if (FileTransferType.BODY.name().equals(transferRequest.getTransferType())) {
            String fileName = transferRequest.getFileName();
            ClientFileAppender fileAppender = transferFileMap.get(fileName);
            if (fileAppender == null) {
              break;
            }
            try {
              fileAppender.append(transferRequest.getBody());
            } catch (Exception e) {
            }

          } else if (FileTransferType.TAIL.name().equals(transferRequest.getTransferType())) {
            String fileName = transferRequest.getFileName();
            ClientFileAppender fileAppender = transferFileMap.remove(fileName);
            if (fileAppender == null) {
              break;
            }
            try {
              boolean checkFlag = fileAppender.checkCrc32(transferRequest.getCrc32());
              if (checkFlag) {
                downFileMap.remove(fileName);
              }

            } catch (Exception e) {
              log.error(e.getMessage());
            } finally {
              fileAppender.close();

            }
          }
          break;

        default:
          System.out.println("消息类型异常");
      }
    }

  }
}
