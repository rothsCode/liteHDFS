package com.rothsCode.namenode;

import com.rothsCode.net.request.FileInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/12/6 17:29
 */
@Data
@AllArgsConstructor
public class DirNode {

  public String path; //当前节点目录
  private String type;//节点类型
  private DirNode parentNode = null;//父节点目录,当为空则顶点目录
  private List<DirNode> childNode = new ArrayList<>();
  private Map<String, String> arr = new HashMap<>();//目录属性
  private List<FileInfo> fileInfos = new ArrayList<>();//目录下对应的文件名

  public DirNode() {

  }

  @Override
  public String toString() {
    return " ";
  }
}
