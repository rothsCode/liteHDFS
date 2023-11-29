package com.rothsCode.litehdfs.namenode.filetree;

import cn.hutool.core.collection.CollectionUtil;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rothsCode.litehdfs.common.netty.request.FileInfo;
import com.rothsCode.litehdfs.common.protoc.OperateLog;
import com.rothsCode.litehdfs.common.protoc.ProtoFileInfo;
import com.rothsCode.litehdfs.common.protoc.ProtoNode;
import com.rothsCode.litehdfs.namenode.vo.PathOperateType;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  private DirNode rootNode;


  /**
   * 日志恢复检查点
   */
  private long maxTxId;

  private AtomicBoolean updateFlag = new AtomicBoolean(false);

  private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);


  public FileDirectoryTree() {
    this.rootNode = new DirNode("/");
    rootNode.setChildNodes(new HashMap<>());
    int i = 0;
  }

  public long getMaxTxId() {
    return maxTxId;
  }

  public void setMaxTxId(long txId) {
    if (maxTxId < txId) {
      this.maxTxId = txId;
    }
  }

  /**
   * 根据editLog更新树
   */
  public void buildEditLog(List<OperateLog> list) {
    if (CollectionUtils.isEmpty(list)) {
      return;
    }
    for (OperateLog operateLog : list) {
      if (Objects.equals(PathOperateType.PATH.name(), operateLog.getOperateType())) {
        makeDir(operateLog.getPath());
      } else if (Objects.equals(PathOperateType.FILE.name(), operateLog.getOperateType())) {
        ProtoFileInfo dataNodeFileLog = operateLog.getFileInfo();
        FileInfo fileInfoLog = FileInfo.decoder(dataNodeFileLog);
        saveFileInfo(fileInfoLog);
      }
    }
    updateFlag.set(true);
  }

  /**
   * 新增或者更新文件
   *
   * @param fileInfoLog
   */
  public void saveFileInfo(FileInfo fileInfoLog) {
    FileInfo fileInfo = this.getFileInfoByPath(fileInfoLog.getParentFileName());
    if (fileInfo == null) {
      this.makeFileNode(fileInfoLog.getParentFileName(), fileInfoLog);
    } else {
      fileInfo.getBlkDataNodes().addAll(fileInfoLog.getBlkDataNodes());
    }
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
              childNode = DirNode.builder().path(p).fileInfo(fileInfo).build();
              ;
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
    log.debug("makeFileNode:{}", path);
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
  public ByteBuffer getFsImage() {
    try {
      readWriteLock.readLock().lock();
      //rootNode 转protoNode
      ProtoNode protoNode = coverProtoNode(rootNode);
      byte[] body = protoNode.toByteArray();
      ByteBuffer byteBuffer = ByteBuffer.allocate(body.length + 8);
      byteBuffer.putLong(maxTxId);
      byteBuffer.put(body);
      return byteBuffer;
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  private ProtoNode coverProtoNode(DirNode node) {
    ProtoNode.Builder protoNodeBuilder = ProtoNode.newBuilder();
    protoNodeBuilder.setPath(node.getPath())
        .setIsFileNode(node.isFileNode());
    if (node.getFileInfo() != null) {
      ProtoFileInfo protoFileInfo = node.getFileInfo().encoder();
      protoNodeBuilder.setFileInfo(protoFileInfo);
    }
    if (CollectionUtil.isNotEmpty(node.getChildNodes())) {
      Map<String, ProtoNode> childNodeMap = new HashMap<>();
      for (DirNode childNode : node.getChildNodes().values()) {
        ProtoNode childProtoNode = coverProtoNode(childNode);
        childNodeMap.put(childNode.getPath(), childProtoNode);
      }
      protoNodeBuilder.putAllChildNodes(childNodeMap);
    }
    return protoNodeBuilder.build();
  }

  @Override
  public void recoverFs(ByteBuffer byteBuffer) {
    try {
      readWriteLock.writeLock().lock();
      if (byteBuffer.remaining() <= 8) {
        log.error("fsImageBufferSize less than 8");
        return;
      }
      // txId
      maxTxId = byteBuffer.getLong();
      ProtoNode protoNode = ProtoNode.parseFrom(byteBuffer);
      byteBuffer.clear();
      //protoNode 转DirNode
      rootNode = coverDirNode(protoNode);
    } catch (InvalidProtocolBufferException e) {
      log.error("InvalidProtocolBufferException:{}", e);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  private DirNode coverDirNode(ProtoNode node) {
    DirNode dirNode = new DirNode(node.getPath());
    if (node.getFileInfo() != null) {
      ProtoFileInfo protoFileInfo = node.getFileInfo();
      FileInfo fileInfo = FileInfo.decoder(protoFileInfo);
      dirNode.setFileInfo(fileInfo);
    }

    if (CollectionUtil.isNotEmpty(node.getChildNodesMap())) {
      Map<String, DirNode> childNodeMap = new HashMap<>();
      for (ProtoNode childProtoNode : node.getChildNodesMap().values()) {
        DirNode childNode = coverDirNode(childProtoNode);
        childNodeMap.put(childNode.getPath(), childNode);
      }
      dirNode.setChildNodes(childNodeMap);
    }
    return dirNode;
  }
}
