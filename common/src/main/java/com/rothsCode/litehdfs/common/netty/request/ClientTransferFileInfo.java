package com.rothsCode.litehdfs.common.netty.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author rothsCode
 * @Description:客户端往dataNode传输文件参数
 * @date 2021/11/15 16:52
 */
@Data
@Builder
@AllArgsConstructor
public class ClientTransferFileInfo {

  /**
   * 传输类型，文件头，文件内容，文件尾
   */
  private String transferType;

  private long fileSize;

  private String fileType;

  private String fileName;

  private byte[] body;
  /**
   * 校验数据一致性码
   */
  private String blockCheckCode;
  /**
   * 传输渠道  客户端   主数据节点
   */
  private String transferChannel;

}
