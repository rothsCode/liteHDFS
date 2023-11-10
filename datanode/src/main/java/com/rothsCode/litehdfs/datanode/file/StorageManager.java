package com.rothsCode.litehdfs.datanode.file;

import com.rothsCode.litehdfs.common.netty.request.FileInfo;
import com.rothsCode.litehdfs.datanode.vo.DataNodeStorageInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * @author rothsCode
 * @Description: 数据节点文件管理器
 * @date 2021/11/8 10:39
 */
public class StorageManager {

  private DataNodeStorageInfo storageInfo;

  public StorageManager() {
    this.storageInfo = new DataNodeStorageInfo();
  }

  public void setStorageInfo(DataNodeStorageInfo storageInfo) {
    this.storageInfo = storageInfo;
  }

  public void addFile(FileInfo fileInfo) {
    synchronized (this) {
      storageInfo.addFileInfo(fileInfo);
    }
  }

  /**
   * dataNode重启时扫描文件夹获取相对应的文件信息
   */
  public DataNodeStorageInfo scanFiles(String path) {
    Validate.isTrue(StringUtils.isNotBlank(path), "scanFiles path is null");
    File parentFile = new File(path);
    if (!parentFile.exists() || !parentFile.isDirectory()) {
      return null;
    }
    DataNodeStorageInfo storageInfo = new DataNodeStorageInfo();
    List<FileInfo> fileInfos = new ArrayList<>();
    long userSpace = 0;
    File[] childFiles = parentFile.listFiles();
    if (childFiles == null || childFiles.length == 0) {
      return storageInfo;
    }
    for (File file : childFiles) {
      if (file.isDirectory()) {
        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
          continue;
        }
        for (File childFile : files) {
          if (childFile.isFile()) {
            FileInfo fileInfo = FileInfo.builder().fileName(childFile.getName())
                .absolutePath(childFile.getAbsolutePath())
                .fileSize(childFile.length()).createTime(childFile.lastModified()).build();
            fileInfos.add(fileInfo);
            userSpace += file.getUsableSpace();
          }
        }
      }
    }
    storageInfo.setFileInfos(fileInfos);
    storageInfo.setUsedSpaceSize(userSpace);
    return storageInfo;

  }


}
