package com.rothsCode.litehdfs.common.netty.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author rothsCode
 * @Description:文件属性
 * @date 2021/11/28 15:03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileInfo {

  private long fileSize;
  private String fileType;
  private String fileName;
  private String parentFileName;
  private String absolutePath;
  private long createTime;
  private long updateTime;
  private String createUser;
  private String updateUser;
  private String hostName;

  /**
   * 文件对应的存储节点信息 /abc/dgg/feg.txt blk1:dn1,dn2,dn3 blk2:dn2,dn5,dn4
   */
  private String blkDataNode;

}
