package com.rothsCode.litehdfs.datanode.vo;

import com.rothsCode.litehdfs.common.netty.request.FileInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author rothsCode
 * @Description: datanode自身运行的参数信息
 * @date 2021/10/2816:13
 */
@Builder
@Data
@AllArgsConstructor
public class DataNodeStorageInfo {

  private long healthyStatus;
  private long usedSpaceSize;
  private long remainSpaceSize;
  private List<FileInfo> fileInfos;


  public DataNodeStorageInfo() {
    fileInfos = new ArrayList<>();
  }

  public void addFileInfo(FileInfo fileInfo) {
    fileInfos.add(fileInfo);
    usedSpaceSize += fileInfo.getFileSize();
  }

  public List<FileInfo> getFileInfos() {
    return fileInfos;
  }

  public String getAbsolutePath(String fileName) {
    Optional<FileInfo> optionalFileInfo = fileInfos.stream()
        .filter(f -> Objects.equals(fileName, f.getFileName())).findFirst();
    return optionalFileInfo.map(FileInfo::getAbsolutePath).orElse(null);
  }


}
