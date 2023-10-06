package com.rothsCode.backup;

import lombok.Builder;
import lombok.Data;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/12/24 16:02
 */
@Data
@Builder
public class BackNodeInfo {

  private String hostName;
  private int port;
}
