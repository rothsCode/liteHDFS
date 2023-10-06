package com.rothsCode.datanode;

import com.rothsCode.net.request.FileInfo;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

/**
 * @author rothsCode
 * @Description:文件追加工具类
 * @date 2021/11/16 14:54
 */
@Slf4j
public class FileAppender {

  private RandomAccessFile rds;
  private File destFile;
  private FileCallBackHandler fileCallBackHandler;

  public FileAppender(FileCallBackHandler fileCallBackHandler, String storagePath,
      String fileName) {
    this.fileCallBackHandler = fileCallBackHandler;
    File fileDir = new File(storagePath + fileName.substring(0, fileName.lastIndexOf("\\")));
    if (!fileDir.exists()) {
      fileDir.mkdirs();
    }
    destFile = new File(storagePath + fileName);
    if (!destFile.exists()) {
      try {
        boolean createFlag = destFile.createNewFile();
        if (!createFlag) {
          log.error("文件目录生成失败:{}", fileName);
        }
        rds = new RandomAccessFile(destFile, "rw");
        rds.seek(rds.length());

      } catch (IOException e) {
        e.printStackTrace();
      }
    }


  }

  public void append(byte[] body) {
    try {
      rds.write(body);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 校验数据一致性
   */
  public boolean checkCrc32(long crc32) {
    try {
      long checkCrc32 = FileUtils.checksumCRC32(destFile);
      if (checkCrc32 == crc32) {
        return true;
      } else {
        log.error("校验失败删除生成的文件:{}", destFile);
        FileUtils.forceDelete(destFile);
        return false;
      }
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * 关闭资源 发送文件消息到nameNode
   */
  public void close() {
    if (rds != null) {
      try {
        rds.close();
      } catch (IOException e) {
        log.error("关闭fileAppender失败:{}", e.getMessage());
        e.printStackTrace();
      }
    }

  }

  public void complete(FileInfo fileInfo) {
    fileInfo.setAbsolutePath(destFile.getAbsolutePath());
    fileCallBackHandler.fileComplete(fileInfo);
  }
}
