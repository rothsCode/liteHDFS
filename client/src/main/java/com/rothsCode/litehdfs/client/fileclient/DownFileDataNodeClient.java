package com.rothsCode.litehdfs.client.fileclient;

import com.alibaba.fastjson.JSONObject;
import com.rothsCode.litehdfs.client.ClientConfig;
import com.rothsCode.litehdfs.common.enums.FileTransferType;
import com.rothsCode.litehdfs.common.enums.PacketType;
import com.rothsCode.litehdfs.common.file.FileAppender;
import com.rothsCode.litehdfs.common.netty.client.DataNodeClient;
import com.rothsCode.litehdfs.common.netty.listener.NettyPacketListener;
import com.rothsCode.litehdfs.common.netty.request.ClientTransferFileInfo;
import com.rothsCode.litehdfs.common.netty.request.NettyPacket;
import com.rothsCode.litehdfs.common.netty.request.RequestWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author rothsCode
 * @Description: 传输文件到dataNode
 * @date 2021/11/15 17:06
 */
@Slf4j
public class DownFileDataNodeClient {

  private Map<String, DataNodeClient> fileNameDataNodeClientMap = new ConcurrentHashMap<>();

  private Map<String, FileAppender> blockFileAppenderMap = new ConcurrentHashMap<>();
  // K:blockFileName V: downPath
  private Map<String, String> blockFileDownPathMap = new ConcurrentHashMap<>();

  private Map<String, String> blockFileNameMap = new ConcurrentHashMap<>();
  //文件块下载响应
  private Map<String, AtomicInteger> downFileCompleteMap = new ConcurrentHashMap<>();

  private Map<String, List<String>> fileNameToBlockNameMap = new ConcurrentHashMap<>();

  public void putDataNodeClient(String fileName, DataNodeClient dataNodeClient) {
    dataNodeClient.addPacketLister(new DownFileDataListener());
    fileNameDataNodeClientMap.put(fileName, dataNodeClient);
  }

  /**
   * 下载文件
   */
  public void downBlockFile(String fileName, String blockFileName, ClientConfig clientConfig,
      boolean lastBlock) {
    cacheFileNameForCallback(fileName, blockFileName, clientConfig.getLocalDownloadPath());
    //异步回调处理文件流
    ClientTransferFileInfo clientTransferFileInfo = ClientTransferFileInfo.builder()
        .fileName(blockFileName).build();
    NettyPacket getFilePacket = NettyPacket
        .buildPacket(JSONObject.toJSONString(clientTransferFileInfo).getBytes(),
            PacketType.GET_FILE.value);
    fileNameDataNodeClientMap.get(blockFileName).send(getFilePacket);
    //如果是最后一个block则做合并文件处理
    mergeBlockFile(fileName, clientConfig, lastBlock);
  }

  private void cacheFileNameForCallback(String fileName, String blockFileName,
      String localDownloadPath) {
    blockFileNameMap.put(blockFileName, fileName);
    //文件片下载地址
    blockFileDownPathMap.put(blockFileName, localDownloadPath);
    List<String> blockFileNames = fileNameToBlockNameMap.get(fileName);
    if (blockFileNames == null) {
      blockFileNames = new ArrayList<>();
    }
    blockFileNames.add(blockFileName);
    fileNameToBlockNameMap.put(fileName, blockFileNames);
    AtomicInteger completeBlockCount = downFileCompleteMap.get(fileName);
    if (completeBlockCount == null) {
      completeBlockCount = new AtomicInteger(0);
    }
    completeBlockCount.incrementAndGet();
    downFileCompleteMap.put(fileName, completeBlockCount);
  }

  private void mergeBlockFile(String fileName, ClientConfig clientConfig, boolean lastBlock) {
    List<String> blockFileNames;
    if (lastBlock) {
      log.debug("{}:down lastBlock", fileName);
      //分段下载完毕后再做文件合并
      int downFileTimeOutSecond = clientConfig.getDownFileTimeOutSeconds();
      do {
        try {
          downFileTimeOutSecond--;
          if (downFileTimeOutSecond < 0) {
            return;
          }
          Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
      } while (downFileCompleteMap.get(fileName).get() > 0);
      log.debug("{}:start mergeBlockFile", fileName);
      blockFileNames = fileNameToBlockNameMap.get(fileName);
      FileAppender targetFileAppender = new FileAppender(clientConfig.getLocalDownloadPath(),
          fileName);
      for (String blockName : blockFileNames) {
        //释放客户端
        fileNameDataNodeClientMap.get(blockName).shutDown();
        fileNameDataNodeClientMap.remove(blockName);
        FileAppender blockFileAppender = blockFileAppenderMap.remove(blockName);
        blockFileAppender.transferChannelAndClose(targetFileAppender);
        blockFileAppender.deleteFile();
      }
      log.debug("{}: mergeBlockFile success", targetFileAppender.getDestFile().getName());
      targetFileAppender.close();
    }
  }

  private class DownFileDataListener implements NettyPacketListener {

    @Override
    public void callBack(RequestWrapper requestWrapper) {
      //下载dataNode回调处理
      NettyPacket nettyPacket = requestWrapper.getNettyPacket();
      byte[] body = nettyPacket.getBody();
      if (body == null || body.length <= 0) {
        return;
      }
      ClientTransferFileInfo transferRequest = JSONObject
          .parseObject(new String(body), ClientTransferFileInfo.class);
      String blockFileName = transferRequest.getFileName();
      if (StringUtils.isEmpty(blockFileName)) {
        return;
      }
      switch (FileTransferType.valueOf(transferRequest.getTransferType())) {
        case HEAD:
          //生成写文件客户端
          String localDownloadPath = blockFileDownPathMap.get(blockFileName);
          String suffixBlockFileName = StringUtils.substringAfterLast(blockFileName, "/");
          FileAppender fileAppender = new FileAppender(localDownloadPath,
              suffixBlockFileName);
          blockFileAppenderMap.put(blockFileName, fileAppender);
          break;
        case BODY:
          log.debug("{}:is downing", blockFileName);
          blockFileAppenderMap.get(blockFileName).append(transferRequest.getBody());
          break;
        case TAIL:
          try {
            log.debug("{}: is reachTail", blockFileName);
            boolean checkFlag = blockFileAppenderMap.get(blockFileName)
                .checkBlockCheckCode(transferRequest.getBlockCheckCode());
            //文件校验成功后回写完成状态
            if (checkFlag) {
              AtomicInteger fileCompleteCount = downFileCompleteMap
                  .get(blockFileNameMap.get(transferRequest.getFileName()));
              fileCompleteCount.decrementAndGet();
              log.debug("{}: down success", blockFileName);
            } else {
              log.error("downFile checkBlockCode false:{}", transferRequest.toString());
            }
          } catch (Exception e) {
            log.error("blockCheck error:{}", e);
          }
          break;
        default:
          log.error("fileType error");
      }
    }

  }
}
