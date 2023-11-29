package com.rothsCode.litehdfs.datanode.file;

import cn.hutool.core.lang.Assert;
import com.rothsCode.litehdfs.datanode.handler.FileCallBackHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

/**
 * @author rothsCode
 * @Description:文件追加工具类
 * @date 2021/11/16 14:54
 */
@Slf4j
public class DataFileAppender {

  private RandomAccessFile rds;
  private File destFile;
  private FileCallBackHandler fileCallBackHandler;

  public DataFileAppender(FileCallBackHandler fileCallBackHandler, String storagePath,
      String fileName) {
    this.fileCallBackHandler = fileCallBackHandler;
    File fileDir = new File(storagePath + fileName.substring(0, fileName.lastIndexOf("/")));
    if (!fileDir.exists()) {
      fileDir.mkdirs();
    }
    destFile = new File(storagePath + fileName);
    if (destFile.exists()) {
      log.error("{}: is existed", fileName);
      return;
    }
    try {
      boolean createFlag = destFile.createNewFile();
      if (!createFlag) {
        log.error("createNewFile error:{}", fileName);
      }
      rds = new RandomAccessFile(destFile, "rw");
      rds.seek(rds.length());
    } catch (IOException e) {
    }

  }

  public void append(byte[] body) {
    Assert.notNull(rds, "file rds is null");
    try {
      rds.write(body);
    } catch (IOException e) {
    }
  }


  /**
   * 校验数据一致性
   */
  public boolean checkBlockCheckCode(String blockCheckCode) {
    try {
      String sourceBlockCheckCode = DigestUtils.sha512Hex(new FileInputStream(destFile));
      if (sourceBlockCheckCode.equals(blockCheckCode)) {
        return true;
      } else {
        FileUtils.forceDelete(destFile);
        return false;
      }
    } catch (IOException e) {
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
      }
    }

  }

}
