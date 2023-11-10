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
  private String storageDirectory = "/usr/redis";

  private String authToken;

  private int retryTime = 5;

  /**
   * 切分文件块大小单位默认128M 128*1024*1024 单位kb
   */
  private long fileBlockSize = 1024;

  /**
   * 对block 以packetSize为单位传输 单位byte
   */
  private long fileBlockPacketSize = 1024 * 1024;

  /**
   * 下载文件超时时间 单位秒
   */
  private long downFileTimeOutSeconds = 60;
}
