package com.rothsCode.litehdfs.datanode.handler;

import com.rothsCode.litehdfs.common.netty.request.FileInfo;
import com.rothsCode.litehdfs.common.netty.response.ServerResponse;
import com.rothsCode.litehdfs.datanode.client.NameNodeClient;
import com.rothsCode.litehdfs.datanode.file.StorageManager;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/11/28 15:42
 */
public class FileCallBackHandler {

  private StorageManager storageManager;
  private NameNodeClient nameNodeClient;

  public FileCallBackHandler(StorageManager storageManager, NameNodeClient nameNodeClient) {
    this.storageManager = storageManager;
    this.nameNodeClient = nameNodeClient;
  }

  public ServerResponse fileComplete(FileInfo fileInfo) {
    storageManager.addFile(fileInfo);
    return nameNodeClient.sendFileInfoToNameNode(fileInfo);
  }
}
