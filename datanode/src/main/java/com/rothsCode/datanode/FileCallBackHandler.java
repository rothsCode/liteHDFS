package com.rothsCode.datanode;

import com.rothsCode.net.request.FileInfo;

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

  public void fileComplete(FileInfo fileInfo) {
    storageManager.addFile(fileInfo);
    nameNodeClient.sendFileInfoToNameNode(fileInfo);
  }
}
