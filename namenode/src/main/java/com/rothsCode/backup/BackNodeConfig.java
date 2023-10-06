package com.rothsCode.backup;

import lombok.Data;

/**
 * @author rothsCode
 * @Description: 备用节点配置
 * @date 2021/12/24 15:08
 */
@Data
public class BackNodeConfig {

  private long pullIntervalSecond = 10;//定时拉取日志间隔时间
  private long startPullSecond = 5;//启动后首次拉取时间
  private int retryTime = 3;//重试时间
  private String backNodeServer = "0.0.0.0:9100"; //服务端节点
  private String activeNodeServer = "0.0.0.0:9000";//主服务端节点

}
