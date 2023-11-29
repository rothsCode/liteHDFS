package com.rothsCode.litehdfs.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/11/11 14:13
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientConfig {

  private String nameNodeServerHost = "127.0.0.1";

  private int nameNodeServerPort = 9400;

  private String userName;

  private String password;
  /**
   * 存储目录
   */
  private String storageDirectory = "/usr/redis/";

  /**
   * 默认本地下载地址
   */
  private String localDownloadPath = "D:\\tmp\\down\\";

  private String authToken;

  private int retryTime = 5;

  /**
   * 切分文件块大小单位默认128M 128*1024*1024 单位byte
   */
  private long fileBlockSize = 1024 * 1024 * 24;

  /**
   * 对block 以packetSize为单位传输 单位byte 1024 * 1024
   */
  private long fileBlockPacketSize = 1024 * 1024;

  /**
   * 下载文件超时时间 单位秒 针对小文件数据
   */
  private int downFileTimeOutSeconds = 60;
}
