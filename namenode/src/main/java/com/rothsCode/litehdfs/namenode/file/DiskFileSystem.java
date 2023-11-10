package com.rothsCode.litehdfs.namenode.file;

import com.alibaba.fastjson.JSONObject;
import com.rothsCode.litehdfs.namenode.config.NameNodeConfig;
import com.rothsCode.litehdfs.namenode.filetree.FileDirectoryTree;
import com.rothsCode.litehdfs.namenode.vo.PathOperateLog;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;


/**
 * @author rothsCode
 * @Description: 磁盘文件处理类
 * @date 2021/11/29 11:29
 */
@Slf4j
public class DiskFileSystem {

  private final static String CURRENT_FILE_NAME = "current.txt";//当前版本
  private final static String PREVIOUS_FILE_NAME = "previous.txt";//上一个版本
  private final static String TEMP_FILE_NAME = "temp.txt";//中间临时版本
  private final static String EDIT_LOG_NAME = "editLog";//临时修改日志
  private final static String PREVIOUS_FILE_PRE = "previousHistory";
  private final static String CURRENT_FILE_DIR = "current\\";
  private final static String FILE_IMAGE_DIR = "\\namenode\\fileImage\\";
  private NameNodeConfig nameNodeConfig;
  private FileDirectoryTree fileDirectoryTree;
  private RandomAccessFile tempFileAppender;
  private String treeDir;
  private File tempFile;
  private AtomicBoolean isBuildStatus = new AtomicBoolean(false);//是否正在重启构建

  public DiskFileSystem(NameNodeConfig nameNodeConfig, FileDirectoryTree fileDirectoryTree) {
    this.fileDirectoryTree = fileDirectoryTree;
    this.nameNodeConfig = nameNodeConfig;
    treeDir = nameNodeConfig.getFileTreePath() + FILE_IMAGE_DIR;
    File fileDir = new File(treeDir);
    if (!fileDir.exists()) {
      fileDir.mkdirs();
    }
    File previousDir = new File(treeDir + PREVIOUS_FILE_PRE);
    if (!previousDir.exists()) {
      previousDir.mkdirs();
    }
    File currentDir = new File(treeDir + CURRENT_FILE_DIR);
    if (!currentDir.exists()) {
      currentDir.mkdirs();
    }
  }


  /**
   * 先将更新文件写入temp文件,再將previous文件更名為previous_當前时间， 再将current 更名为previous 最后将temp文件更改为current
   * 如果写入失败则删除temp
   */
  public boolean storageFileTreeDiskFile() {
    if (!fileDirectoryTree.getUpdateFlag().get()) {
      return false;
    }
    // previous,current,temp 三个镜像文件
    try {
      tempFile = new File(treeDir + TEMP_FILE_NAME);
      if (!tempFile.exists()) {
        boolean createFlag = tempFile.createNewFile();
        if (!createFlag) {
          log.info("当前镜像文件目录生成失败");
        }
      }
      tempFileAppender = new RandomAccessFile(tempFile, "rw");
      tempFileAppender.seek(tempFileAppender.length());
      log.debug("镜像开始落盘");
      byte[] fsImage = fileDirectoryTree.getFsImage();
      tempFileAppender.write(fsImage);
      //previous->previous-time
      File previousFile = new File(treeDir + PREVIOUS_FILE_NAME);
      if (previousFile.exists()) {
        File previousHistoryFile = new File(
            treeDir + PREVIOUS_FILE_PRE + "\\" + +System.currentTimeMillis() + "_"
                + PREVIOUS_FILE_NAME);
        FileUtils.moveFile(previousFile, previousHistoryFile);
      }
      //current->previous
      File currentFile = new File(treeDir + CURRENT_FILE_DIR + "\\" + CURRENT_FILE_NAME);
      if (currentFile.exists()) {
        File newPreviousFile = new File(treeDir + PREVIOUS_FILE_NAME);
        FileUtils.moveFile(currentFile, newPreviousFile);
      }
      //tem->current
      tempFileAppender.close();
      File newFile = new File(treeDir + CURRENT_FILE_DIR + "\\" + CURRENT_FILE_NAME);
      FileUtils.moveFile(tempFile, newFile);
      fileDirectoryTree.getUpdateFlag().set(false);
      log.debug("镜像落盘成功");
      return true;
    } catch (Exception e) {
      tempFile.delete();
      return false;
    } finally {
      if (tempFileAppender != null) {
        try {
          tempFileAppender.close();
        } catch (IOException e) {
        }
      }
    }

  }

  /**
   * 加载磁盘文件解析成文件目录树内存数据,namenode故障重启后调用
   */
  public void loadDiskParseData() {
    RandomAccessFile rds = null;
    try {
      isBuildStatus.set(true);
      //1 加载全量镜像文件  先看temp文件是否存在，再看current文件是否存在
      File dataFile = new File(treeDir + TEMP_FILE_NAME);
      if (!dataFile.exists()) {
        dataFile = new File(treeDir + CURRENT_FILE_DIR + CURRENT_FILE_NAME);
      }
      log.info("开始解析构造镜像文件");
      if (dataFile.exists()) {
        //1加载主镜像文件
        rds = new RandomAccessFile(dataFile, "r");
        if (rds.length() > 0) {
          byte[] dataByte = new byte[(int) rds.length()];
          rds.read(dataByte);
          //恢复数据
          fileDirectoryTree.recoverFs(dataByte);
        }
      }
      //2加载editLog文件
      File editParentFile = new File(treeDir + EDIT_LOG_NAME);
      if (editParentFile.exists() && editParentFile.isDirectory()) {
        File[] editFiles = editParentFile.listFiles();
        if (editFiles != null && editFiles.length > 0) {
          for (File editFile : editFiles) {
            RandomAccessFile editRds = new RandomAccessFile(editFile, "r");
            byte[] editByte = new byte[(int) editRds.length()];
            editRds.read(editByte);
            List<PathOperateLog> editLog = JSONObject
                .parseArray(new String(editByte), PathOperateLog.class);
            fileDirectoryTree.buildEditLog(editLog);
            editRds.close();
          }
        }
        //3重新构建后镜像再次落盘保存镜像
        boolean buildFlag = storageFileTreeDiskFile();
        //4镜像落盘成功后删除变更日志
        if (buildFlag) {
          FileUtils.deleteDirectory(editParentFile);
        }
      }

    } catch (Exception e) {
    } finally {
      isBuildStatus.set(false);
      try {
        if (rds != null) {
          rds.close();
        }
      } catch (IOException e) {
      }
    }
  }

  /**
   * 将目录操作日志刷入磁盘
   */
  public void savePathOperateLog(PathOperateLogBuffer PathOperateLogBuffer) {
    if (PathOperateLogBuffer.getIsNowFlush().get() || !PathOperateLogBuffer.getIsFLushData()) {
      return;
    }
    PathOperateLogBuffer.getIsNowFlush().compareAndSet(false, true);
    while (isBuildStatus.get()) {
      log.info("元数据正在重新构建---");
      try {
        //发生在nameNode重启过程中需要等待
        Thread.sleep(1000);
      } catch (InterruptedException e) {
      }
    }
    log.info("editLog开始落盘");
    long currentIndex = PathOperateLogBuffer.getCurrentFlushIndex();
    long startIndex = PathOperateLogBuffer.getStartFlushIndex();
    File editParentFile = new File(treeDir + EDIT_LOG_NAME);
    if (!editParentFile.exists()) {
      editParentFile.mkdirs();
    }
    File editFile = new File(
        treeDir + EDIT_LOG_NAME + "\\" + startIndex + "_" + currentIndex + ".txt");
    if (editFile.exists()) {
      return;
    }
    RandomAccessFile rds = null;
    try {
      boolean createFlag = editFile.createNewFile();
      if (!createFlag) {
        log.error("当前editLog文件生成失败");
      }
      rds = new RandomAccessFile(editFile, "rw");
      rds.seek(rds.length());
      byte[] bytes = JSONObject.toJSONString(PathOperateLogBuffer.getFlushLogList()).getBytes();
      //转byte数组
      rds.write(bytes);
    } catch (Exception e) {
      log.error("write editLog error:{}", e);
    } finally {
      //变更状态
      PathOperateLogBuffer.setLastFlushIndex(currentIndex);
      PathOperateLogBuffer.clearFlushList();
      PathOperateLogBuffer.setIsFLushData(false);
      if (rds != null) {
        try {
          rds.close();
        } catch (IOException e) {
        }
      }
    }
    log.info("当前日志段落盘成功" + startIndex + "_" + currentIndex);
    PathOperateLogBuffer.getIsNowFlush().set(false);
  }


}
