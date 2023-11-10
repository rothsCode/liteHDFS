package com.rothsCode.litehdfs.namenode.config;

import lombok.Data;

/**
 * @author rothsCode
 * @Description: 元数据配置
 * @date 2021/11/9 14:15
 */
@Data
public class NameNodeConfig {

  private int serverPort = 9400;
  //副本数量
  private int copySize = 3;
  //节点续期时间 默认 3s
  private int dataNodeRenewalTime = 3000;
  //文件树存储路径
  private String fileTreePath;
  //日志同步频率单位秒
  private int editLogSyncInterval = 1;
}
