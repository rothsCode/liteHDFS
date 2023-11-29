package com.rothsCode.litehdfs.namenode.filetree;

import com.rothsCode.litehdfs.common.netty.request.FileInfo;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/12/6 17:29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirNode {

  private String path; //当前节点目录

  private boolean isFileNode;//节点类型 目录节点 文件节点

  private Map<String, DirNode> childNodes;//子目录节点

  private FileInfo fileInfo;

  public DirNode(String path) {
    childNodes = new HashMap<>();
    this.isFileNode = false;
    this.path = path;
  }



}
