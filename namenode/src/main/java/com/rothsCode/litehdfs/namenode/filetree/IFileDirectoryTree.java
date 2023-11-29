package com.rothsCode.litehdfs.namenode.filetree;

import com.rothsCode.litehdfs.common.netty.request.FileInfo;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author roths
 * @Description:
 * @date 2023/10/31 15:11
 */
public interface IFileDirectoryTree {


  /**
   * 创建目录
   *
   * @param path
   * @return
   */
  boolean makeDir(String path);

  boolean deleteDir(String path);

  /**
   * 创建文件节点
   */
  boolean makeFileNode(String path, FileInfo fileInfo);

  boolean deleteFileNode(String path);

  List<DirNode> listFiles(String path);

  FileInfo getFileInfoByPath(String path);

  ByteBuffer getFsImage();

  void recoverFs(ByteBuffer byteBuffer);
}
