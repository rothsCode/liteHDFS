package com.rothsCode.vo;

import java.io.Serializable;
import java.util.Comparator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author rothsCode
 * @Description: 文件目录操作日志
 * @date 2021/11/29 10:09
 */
@Data
@Builder
@AllArgsConstructor
public class OperateLog implements Comparator<OperateLog>, Serializable {

  private String operateType;//操作类型
  private String content;//操作类型
  private long id;//操作顺序id,用于顺序操作

  @Override
  public int compare(OperateLog o1, OperateLog o2) {
    return (o1.getId() - o2.getId() > 0) ? 1 : -1;
  }
}
