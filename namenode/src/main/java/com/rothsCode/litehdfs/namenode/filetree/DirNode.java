package com.rothsCode.litehdfs.namenode.filetree;

import com.rothsCode.litehdfs.common.netty.request.FileInfo;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/12/6 17:29
 */
@Data
@NoArgsConstructor
public class DirNode {

  private String path; //当前节点目录

  private boolean isFileNode;//节点类型 目录节点 文件节点

  private Map<String, DirNode> childNodes;//子目录节点

  private FileInfo fileInfo;

  public DirNode(String path) {
    this.isFileNode = false;
    this.path = path;
    childNodes = new ConcurrentHashMap<>();
  }

  public DirNode(String path, FileInfo fileInfo) {
    this.isFileNode = true;
    this.path = path;
    this.fileInfo = fileInfo;
  }


}
