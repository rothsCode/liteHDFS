package com.rothsCode.net.request;

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

  private String transferType;//传输类型，文件头，文件内容，文件尾
  private long fileSize;
  private String fileType;
  private String fileName;
  private byte[] body;
  private long crc32;//校验数据一致性

}
