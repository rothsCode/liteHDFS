package com.rothsCode.litehdfs.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 刷盘类型
 *
 * @author rothsCode
 */
@Getter
@AllArgsConstructor
public enum FlushDiskType {
  ASYNC_BUFFER_FLUSH("ASYNC_BUFFER_FLUSH", "异步缓冲刷盘"),
  ASYNC_FLUSH("ASYNC_FLUSH", "异步刷盘 默认10ms批量刷盘"),
  SYNC_FLUSH("SYNC_FLUSH", "完全同步刷盘"),
  ;

  public String value;
  private String description;

}