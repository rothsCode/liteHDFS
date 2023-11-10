package com.rothsCode.litehdfs.common.netty.vo;

import java.util.Comparator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DataNodeInfo implements Comparator<DataNodeInfo> {

  private long nodeId;
  private String address;
  private String ip;
  private int port;
  private int httpPort;
  private long lastHeartTime;
  private Integer healthyStatus;
  private volatile long usedSpaceSize;
  private volatile long remainSpaceSize;
  /**
   * 节点类型 主节点   丛节点
   */
  private String nodeType;

  public synchronized void addFileSize(long fileSize) {
    usedSpaceSize += fileSize;
    remainSpaceSize -= fileSize;
  }

  @Override
  public int compare(DataNodeInfo o1, DataNodeInfo o2) {
    if (o1.getRemainSpaceSize() > o2.getRemainSpaceSize()) {
      return 1;
        } else if (o1.getRemainSpaceSize() < o2.getRemainSpaceSize()) {
            return -1;
        }
        return 0;
    }
}

