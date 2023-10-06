package com.rothsCode.namenode;

import com.alibaba.fastjson.JSONObject;
import com.rothsCode.net.request.FileInfo;
import com.rothsCode.vo.OperateLog;
import com.rothsCode.vo.OperateType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import lombok.Data;

/**
 * @author rothsCode
 * @Description:文件目录树 目录下存文件名索引，每个文件可能包含多个数据块，每个数据块对应多个数据节点
 * @date 2021/11/11 16:21
 */
@Data
public class FileDirectoryTree {

  public DirNode rootNode;
  private AtomicBoolean updateFlag = new AtomicBoolean(false);

  public FileDirectoryTree() {
    this.rootNode = new DirNode();
  }


  /**
   * 根据editLog更新树
   */
  public void buildEditLog(List<OperateLog> list) {
    if (list == null || list.size() == 0) {
      return;
    }
    for (OperateLog operateLog : list) {
      if (Objects.equals(OperateType.ADD.name(), operateLog.getOperateType())) {
        makeDir(operateLog.getContent());
      }
    }
    updateFlag.set(true);
  }


  /**
   * 创建文件目录
   *
   * @param path
   */
  public boolean makeDir(String path) {
    // admin/tem/pic
    String[] paths = path.split("/");
    if (paths.length == 0) {
      return false;
    }
    int pathLength = 0;
    boolean makeFlag = selectTailPath(paths, pathLength, rootNode);
    if (Objects.equals(makeFlag, true)) {
      updateFlag.getAndSet(true);
    }
    System.out.println("文件目录树创建文件目录:" + path);
    return makeFlag;
  }

  /**
   * 根据文件目录查询对应的文件节点
   */
  public DirNode findDirNodeByPath(String path) {
    List<DirNode> dirNodes = rootNode.getChildNode();
    if (dirNodes == null || dirNodes.size() == 0) {
      return null;
    }
    String[] paths = path.split("/");
    if (paths.length == 0) {
      return null;
    }
    int pathLength = 0;
    DirNode dirNode = findChildCode(dirNodes, paths, pathLength);
    return dirNode;

  }

  /**
   * 查询子目录
   *
   * @return
   */
  public List<String> getChildPaths(String path) {
    List<DirNode> dirNodes = rootNode.getChildNode();
    if (dirNodes == null || dirNodes.size() == 0) {
      return Collections.emptyList();
    }
    String[] paths = path.split("/");
    if (paths.length == 0) {
      return Collections.emptyList();
    }
    int pathLength = 0;
    List<String> childPaths = findChildPath(dirNodes, paths, pathLength);
    return childPaths;
  }

  private List<String> findChildPath(List<DirNode> dirNodes, String[] paths, int pathLength) {
    for (DirNode node : dirNodes) {
      if (!Objects.equals(node.path, paths[pathLength])) {
        continue;
      }
      pathLength++;
      if (pathLength == paths.length) {
        return node.getChildNode().stream().map(DirNode::getPath).collect(Collectors.toList());
      }
      return findChildPath(node.getChildNode(), paths, pathLength);
    }
    return Collections.emptyList();


  }


  private DirNode findChildCode(List<DirNode> dirNodes, String[] paths, int pathLength) {
    for (DirNode node : dirNodes) {
      if (!Objects.equals(node.path, paths[pathLength])) {
        continue;
      }
      pathLength++;
      if (pathLength == paths.length) {
        System.out.println("查询到子节点:" + JSONObject.toJSONString(node));
        return node;
      }
      return findChildCode(node.getChildNode(), paths, pathLength);
    }
    return null;
  }

  /**
   * 查询目录下的文件信息
   */
  public List<FileInfo> findFileInfoByPath(String path) {
    DirNode dirNode = findDirNodeByPath(path);
    if (dirNode == null) {
      return Collections.emptyList();
    }
    return dirNode.getFileInfos();
  }

  /**
   * 添加文件信息
   *
   * @param path
   * @param fileInfo
   */
  public boolean AddFileInfo(String path, FileInfo fileInfo) {
    DirNode dirNode = findDirNodeByPath(path);
    if (dirNode == null) {
      System.out.println("路径不正确");
      return false;
    }
    dirNode.getFileInfos().add(fileInfo);
    return true;
  }

  private boolean selectTailPath(String[] paths, int pathLength, DirNode parentDirNode) {
    List<DirNode> dirNodes = new ArrayList<>();
    dirNodes.addAll(parentDirNode.getChildNode());
    if (dirNodes == null || dirNodes.size() == 0) {
      DirNode dirNode = new DirNode();
      dirNode.setPath(paths[pathLength]);
      dirNode.setParentNode(parentDirNode);
      parentDirNode.getChildNode().add(dirNode);
      //最后一个文件目录返回
      pathLength++;
      if (pathLength == paths.length) {
        return true;
      }
      return selectTailPath(paths, pathLength, dirNode);
    } else {
      int nodeLength = 0;
      for (DirNode node : dirNodes) {
        nodeLength++;
        if (Objects.equals(node.path, paths[pathLength])) {
          break;
        }
        if (nodeLength == dirNodes.size()) {
          //最上层目录不存在开始构造
          DirNode dirNode = new DirNode();
          dirNode.setPath(paths[pathLength]);
          dirNode.setParentNode(parentDirNode);
          parentDirNode.getChildNode().add(dirNode);
          nodeLength++;
        }
      }
      pathLength++;
      if (pathLength == paths.length) {
        return true;
      }
      return selectTailPath(paths, pathLength, parentDirNode.getChildNode().get(nodeLength - 1));
    }

  }

  @Override
  public String toString() {
    return " ";
  }

}
