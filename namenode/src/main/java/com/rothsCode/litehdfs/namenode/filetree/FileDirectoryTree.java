package com.rothsCode.litehdfs.namenode.filetree;

import com.alibaba.fastjson.JSONObject;
import com.rothsCode.litehdfs.common.netty.request.FileInfo;
import com.rothsCode.litehdfs.namenode.vo.PathOperateLog;
import com.rothsCode.litehdfs.namenode.vo.PathOperateType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author rothsCode
 * @Description:文件目录树 目录下存文件名索引，每个文件可能包含多个数据块，每个数据块对应多个数据节点
 * @date 2021/11/11 16:21
 */
@Data
@Slf4j
public class FileDirectoryTree implements IFileDirectoryTree {

  public DirNode rootNode;

  private AtomicBoolean updateFlag = new AtomicBoolean(false);

  private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);

  public FileDirectoryTree() {
    this.rootNode = new DirNode("/");
  }


  /**
   * 根据editLog更新树
   */
  public void buildEditLog(List<PathOperateLog> list) {
    if (CollectionUtils.isEmpty(list)) {
      return;
    }
    for (PathOperateLog pathOperateLog : list) {
      if (Objects.equals(PathOperateType.ADD.name(), pathOperateLog.getOperateType())) {
        makeDir(pathOperateLog.getContent());
      } else if (Objects.equals(PathOperateType.FILE.name(), pathOperateLog.getOperateType())) {
        FileInfo dataNodeFileLog = JSONObject
            .parseObject(pathOperateLog.getContent(), FileInfo.class);
        FileInfo fileInfo = this.getFileInfoByPath(dataNodeFileLog.getParentFileName());
        if (fileInfo != null) {
          fileInfo
              .setBlkDataNode(fileInfo.getBlkDataNode() + ";" + dataNodeFileLog.getBlkDataNode());
        } else {
          this.makeFileNode(dataNodeFileLog.getParentFileName(), dataNodeFileLog);
        }
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
    if (StringUtils.isEmpty(path)) {
      return false;
    }
    String[] paths = path.split("/");
    if (paths.length == 0) {
      return false;
    }
    try {
      readWriteLock.writeLock().lock();
      // abc/dd/dfg
      DirNode currentNode = rootNode;
      for (String p : paths) {
        if (StringUtils.isNotBlank(p)) {
          DirNode childNode = currentNode.getChildNodes().get(p);
          if (childNode == null) {
            childNode = new DirNode(p);
            currentNode.getChildNodes().put(p, childNode);
          }
          currentNode = childNode;
        }
      }
    } finally {
      readWriteLock.writeLock().unlock();
    }
    updateFlag.set(true);
    log.debug("文件目录树创建文件目录:{}", path);
    return true;
  }


  @Override
  public boolean deleteDir(String path) {
    if (StringUtils.isEmpty(path)) {
      return false;
    }
    String[] paths = path.split("/");
    int pathLength = paths.length;
    if (paths.length == 0) {
      return false;
    }
    try {
      // abc/dd/dfg
      DirNode currentNode = rootNode;
      readWriteLock.writeLock().lock();
      for (int i = 0; i < pathLength; i++) {
        String p = paths[i];
        if (StringUtils.isNotBlank(p)) {
          DirNode childNode = currentNode.getChildNodes().get(p);
          if (childNode == null) {
            return false;
          }
          if (i == pathLength - 1) {
            currentNode.getChildNodes().remove(p);
            return true;
          }
          currentNode = childNode;
        }
      }
    } finally {
      readWriteLock.writeLock().unlock();
    }
    updateFlag.set(true);
    log.debug("文件目录树删除文件目录:{}" + path);
    return false;
  }

  @Override
  public boolean makeFileNode(String path, FileInfo fileInfo) {
    if (StringUtils.isEmpty(path)) {
      return false;
    }
    String[] paths = path.split("/");
    int pathLength = paths.length;
    if (pathLength == 0) {
      return false;
    }
    try {
      // abc/dd/dfg
      DirNode currentNode = rootNode;
      readWriteLock.writeLock().lock();
      for (int i = 0; i < pathLength; i++) {
        String p = paths[i];
        if (StringUtils.isNotBlank(p)) {
          DirNode childNode = currentNode.getChildNodes().get(p);
          if (childNode == null) {
            if (i < pathLength - 1) {
              //dirNode
              childNode = new DirNode(p);
            } else {
              //fileNode
              childNode = new DirNode(p, fileInfo);
            }
            currentNode.getChildNodes().put(p, childNode);
          }
          currentNode = childNode;
        }
      }
    } finally {
      readWriteLock.writeLock().unlock();
    }
    updateFlag.set(true);
    log.debug("文件目录树创建文件目录:{}", path);
    return true;
  }

  @Override
  public boolean deleteFileNode(String path) {
    return false;
  }

  @Override
  public List<DirNode> listFiles(String path) {
    if (StringUtils.isEmpty(path)) {
      return Collections.emptyList();
    }
    String[] paths = path.split("/");
    int pathLength = paths.length;
    if (pathLength == 0) {
      return Collections.emptyList();
    }
    try {
      readWriteLock.readLock().lock();
      // abc/dd/dfg
      DirNode currentNode = rootNode;
      for (int i = 0; i < pathLength; i++) {
        String p = paths[i];
        if (StringUtils.isNotBlank(p)) {
          DirNode childNode = currentNode.getChildNodes().get(p);
          if (childNode == null) {
            return Collections.emptyList();
          }
          currentNode = childNode;
        }
      }
      return new ArrayList<>(currentNode.getChildNodes().values());
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public FileInfo getFileInfoByPath(String path) {
    if (StringUtils.isEmpty(path)) {
      return null;
    }
    String[] paths = path.split("/");
    int pathLength = paths.length;
    if (pathLength == 0) {
      return null;
    }
    try {
      readWriteLock.readLock().lock();
      // abc/dd/dfg
      DirNode currentNode = rootNode;
      for (int i = 0; i < pathLength; i++) {
        String p = paths[i];
        if (StringUtils.isNotBlank(p) && !currentNode.isFileNode()) {
          DirNode childNode = currentNode.getChildNodes().get(p);
          if (childNode == null) {
            return null;
          }
          currentNode = childNode;
        }
      }
      return currentNode.getFileInfo();
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public byte[] getFsImage() {
    try {
      readWriteLock.readLock().lock();
      return JSONObject.toJSONString(rootNode).getBytes();
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public void recoverFs(byte[] fsImage) {
    try {
      readWriteLock.writeLock().lock();
      rootNode = JSONObject.parseObject(fsImage, DirNode.class);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }
}
