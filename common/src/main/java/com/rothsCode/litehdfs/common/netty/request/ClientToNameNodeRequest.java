package com.rothsCode.litehdfs.common.netty.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author rothsCode
 * @Description:客户端请求
 * @date 2021/11/11 15:24
 */
@Data
@Builder
@AllArgsConstructor
public class ClientToNameNodeRequest {

  private String path;

  private String fileName;

  /**
   * 第几个block
   */
  private long blockIndex;

  private long blockSize;

  private String fileType;

  private String userName;

  private String passWord;

  private String authToken;
}
