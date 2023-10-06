package com.rothsCode.net.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author rothsCode
 * @Description:文件属性
 * @date 2021/11/28 15:03
 */
@Data
@AllArgsConstructor
@Builder
public class FileInfo {

  private long fileSize;
  private String fileType;
  private String fileName;
  private String absolutePath;//绝对路径
  private long createTime;
  private long updateTime;
  private String createUser;
  private String updateUser;
  private String hostName;

  public FileInfo() {

  }
}
