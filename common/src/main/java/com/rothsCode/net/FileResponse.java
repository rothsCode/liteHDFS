package com.rothsCode.net;

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

  private String fileName;
  private List<DataNodeInfo> dataNodeInfos;

}
