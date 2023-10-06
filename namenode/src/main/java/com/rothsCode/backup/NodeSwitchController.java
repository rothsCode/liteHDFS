package com.rothsCode.backup;

import com.rothsCode.NameNodeConfig;
import lombok.Data;

/**
 * @author rothsCode
 * @Description:主备切换选举器
 * @date 2021/12/21 15:40
 */
@Data
public class NodeSwitchController {

  private static volatile boolean switchStatus = false;
  private NameNodeConfig nameNodeConfig;

  private NodeSwitchController() {

  }

  public static NodeSwitchController getInstance() {
    return singleInstance.singleInstance;
  }

  public void setNameNodeConfig(NameNodeConfig nameNodeConfig) {
    this.nameNodeConfig = nameNodeConfig;
  }

  public void switchNode() {

  }

  private static class singleInstance {

    private static final NodeSwitchController singleInstance = new NodeSwitchController();
  }


}
