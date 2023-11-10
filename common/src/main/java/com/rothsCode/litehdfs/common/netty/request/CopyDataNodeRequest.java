package com.rothsCode.litehdfs.common.netty.request;

import com.rothsCode.litehdfs.common.netty.vo.DataNodeInfo;
import java.util.List;
import lombok.Data;

/**
 * @author rothsCode
 * @Description:客户端往dataNode传输文件参数
 * @date 2021/11/15 16:52
 */
@Data
public class CopyDataNodeRequest {

  private String fileName;

  private String parentFileName;

  private String nodeType;

  /**
   * 复制节点
   */
  private List<DataNodeInfo> copyDataNodes;


}
