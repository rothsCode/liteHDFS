package com.rothsCode.litehdfs.namenode.vo;

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
public class PathOperateLog implements Comparator<PathOperateLog>, Serializable {

  private String operateType;//操作类型
  private String content;//操作内容
  private long id;//操作顺序id,用于顺序操作

  public PathOperateLog() {

  }

  @Override
  public int compare(PathOperateLog o1, PathOperateLog o2) {
    return (o1.getId() - o2.getId() > 0) ? 1 : -1;
  }
}
