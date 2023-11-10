package com.rothsCode.litehdfs.common.netty.response;

import com.rothsCode.litehdfs.common.netty.vo.DataNodeInfo;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/11/15 11:33
 */

@Data
@Builder
@AllArgsConstructor
public class FileResponse {

  /**
   * block fileName
   */
  private String fileName;

  private String parentFileName;

  private List<DataNodeInfo> dataNodeInfos;

}
