package com.rothsCode.litehdfs.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 节点类型
 *
 * @author rothsCode
 */
@Getter
@AllArgsConstructor
public enum DataNodeType {

  MASTER("master", "主节点"),
  FOLLOWER("follower", "从节点"),
  ;

  public String value;
  private String description;

}