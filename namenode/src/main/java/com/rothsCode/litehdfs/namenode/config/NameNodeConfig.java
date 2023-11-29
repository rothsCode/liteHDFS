package com.rothsCode.litehdfs.namenode.config;

import com.rothsCode.litehdfs.common.enums.FlushDiskType;
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
  //nameNode主路徑
  private String nameNodePath;
  //日志同步模式 定时刷盘(ASYNC_FLUSH 异步)  每次刷盘 (SYNC_FLUSH 同步)默认异步
  private String editLogFlushDiskType = FlushDiskType.ASYNC_FLUSH.value;
  //日志缓存大小默认 64M  1024*1024*64
  private int editLogBufferSize = 1024 * 1024 * 8;
  //缓冲日志同步频率单位秒
  private int editLogSyncInterval = 5;
  //文件镜像频率单位分钟
  private int fsImageSyncInterval = 2;
  //写入器同步频率单位毫秒
  private int channelFlushInterval = 10;
  //操作日志文件数据间隔
  private long txIdInterval = 100000;
  //文件写入器数量
  private int fileAppenderNums = 8;
  //文件写入器百分阈值
  private double fileAppenderAdjustThreshold = 0.5;
  //文件写入器调整周期单位秒
  private long fileAppenderScheduleTime = 5;
  //是否保存镜像历史版本
  private boolean storageFsImageHistory;
}
