package com.rothsCode.litehdfs.client;

/**
 * @author rothsCode
 * @Description文件上传下载客户端
 * @date 2021/11/11 11:43
 */
public class FileClientFactory {

  public static DefaultFileSystem createFileSystemClient(ClientConfig clientConfig) {
    DefaultFileSystem defaultFileSystem = new DefaultFileSystem(clientConfig);
    defaultFileSystem.init();
    defaultFileSystem.start();
    return defaultFileSystem;
  }


}
