package com.rothsCode.litehdfs.common.file;

import cn.hutool.core.lang.Assert;
import com.rothsCode.litehdfs.common.netty.request.FileInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author rothsCode
 * @Description:文件追加工具类
 * @date 2021/11/16 14:54
 */
@Slf4j
public class FileAppender {

  private RandomAccessFile rds;
  private FileChannel channel;
  private File destFile;

  public FileAppender(String storagePath, String fileName) {
    int suffixLastIndex = fileName.lastIndexOf("/");
    if (suffixLastIndex > 0) {
      File fileDir = new File(storagePath + fileName.substring(0, suffixLastIndex));
      if (!fileDir.exists()) {
        fileDir.mkdirs();
      }
    }
    destFile = new File(storagePath + fileName);
    if (!destFile.exists()) {
      try {
        destFile.createNewFile();
      } catch (IOException e) {
        log.error("createNewFile error:{}", fileName);
      }
    }
    createFileChannel();
  }

  public FileAppender(File file) {
    this.destFile = file;
    createFileChannel();
  }

  private void createFileChannel() {
    try {
      rds = new RandomAccessFile(destFile, "rw");
    } catch (FileNotFoundException e) {
      log.error("{}:file not find", destFile.getPath());
    }
    Assert.notNull(rds, "randomAccessFile create error");
    channel = rds.getChannel();
  }

  public FileChannel getChannel() {
    return channel;
  }

  public RandomAccessFile getRds() {
    return rds;
  }

  public File getDestFile() {
    return destFile;
  }

  /**
   * 追加写入数据
   *
   * @param body
   */
  public void append(byte[] body) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(body.length);
    byteBuffer.put(body);
    // 5、读写切换
    byteBuffer.flip();
    while (byteBuffer.hasRemaining()) {
      try {
        channel.write(byteBuffer);
      } catch (IOException e) {
      }
    }
  }

  /**
   * 读取文件流写入目标文件 内存映射提升读写效率
   *
   * @param targetFileAppender
   */
  public void transferChannel(FileAppender targetFileAppender) {
    try {
      channel.transferTo(0, channel.size(), targetFileAppender.channel);
    } catch (IOException e) {
      log.error("readBodyError:{}", e);
    }

  }

  /**
   * 校验数据一致性
   */
  public boolean checkBlockCheckCode(String blockCheckCode) {
    String sourceBlockCheckCode = getFileCheckCode();
    if (sourceBlockCheckCode.equals(blockCheckCode)) {
      return true;
    } else {
      this.close();
      //FileUtils.deleteQuietly(destFile);
      return false;
    }
  }

  /**
   * 获取整个文件校验码
   *
   * @return
   */
  public String getFileCheckCode() {
    ByteBuffer byteBuffer = readBody();
    byte[] bytes = byteBuffer.array();
    if (bytes[0] == 0) {
      try (FileInputStream fileInputStream = new FileInputStream(destFile)) {
        return DigestUtils.sha512Hex(fileInputStream);
      } catch (Exception e) {
        return null;
      }
    }
    return DigestUtils.sha512Hex(bytes);
  }

  /**
   * 获取部分文件对应的校验码
   *
   * @return
   */
  public String getBlockFileCheckCode(int blockIndex, int blockSize, int bufferSize) {
    try {
      rds.seek(blockIndex * blockSize);
      byte[] blockByte = new byte[bufferSize];
      rds.read(blockByte);
      //恢复到文件初始读取位置
      rds.seek(blockIndex * blockSize);
      return DigestUtils.sha512Hex(blockByte);
    } catch (Exception e) {
      return null;
    }
  }

  public ByteBuffer readBody() {
    try {
      //读之前强制刷新磁盘
      channel.force(false);
      ByteBuffer buffer = ByteBuffer.allocate((int) destFile.length());
      channel.read(buffer);
      buffer.flip();
      return buffer;
    } catch (IOException e) {
      return null;
    }
  }

  public ByteBuffer readBody(int bufferSize) {
    try {
      //读之前强制刷新磁盘
      channel.force(false);
      ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
      channel.read(byteBuffer);
      byteBuffer.flip();
      return byteBuffer;
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * 关闭资源 发送文件消息到nameNode
   */
  public void close() {
    if (rds != null) {
      try {
        channel.close();
        rds.close();
      } catch (IOException e) {
      }
    }

  }

  public void complete(FileInfo fileInfo) {
    fileInfo.setAbsolutePath(destFile.getAbsolutePath());
  }
}
