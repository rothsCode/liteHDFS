package com.rothsCode.namenode;

import com.alibaba.fastjson.JSONObject;
import com.rothsCode.NameNodeConfig;
import com.rothsCode.vo.OperateLog;
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
  private final static String CURRENT_FILE_DIR = "current";
  private NameNodeConfig nameNodeConfig;
  private FileDirectoryTree fileDirectoryTree;
  private RandomAccessFile tempFileAppender;
  private String treeDir;
  private File tempFile;
  private AtomicBoolean isBuildStatus = new AtomicBoolean(false);//是否正在重启构建

  public DiskFileSystem(NameNodeConfig nameNodeConfig, FileDirectoryTree fileDirectoryTree)
      throws IOException {
    this.fileDirectoryTree = fileDirectoryTree;
    this.nameNodeConfig = nameNodeConfig;
    treeDir = nameNodeConfig.getStorageFileTreeDir() + "\\namenode\\fileImage\\";
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
   * 先将更新文件写入temp文件,再將previous文件更名為previous_當前时间，再将current 更名为previous 最后将temp文件更改为current
   * 如果写入失败则删除temp
   */
  public boolean loadFileTreeDiskFile() {
    if (!fileDirectoryTree.getUpdateFlag().get()) {
      System.out.println("当前镜像未发生变化");
      return false;
    }
    //规划 previous,current,temp 三个镜像文件
    try {
      tempFile = new File(treeDir + TEMP_FILE_NAME);
      if (!tempFile.exists()) {
        boolean createFlag = tempFile.createNewFile();
        if (!createFlag) {
          log.error("当前镜像文件目录生成失败");
        }
      }
      tempFileAppender = new RandomAccessFile(tempFile, "rw");
      tempFileAppender.seek(tempFileAppender.length());

      System.out.println("镜像开始落盘");
      byte[] fsImage = JSONObject.toJSONString(fileDirectoryTree.getRootNode()).getBytes();
      tempFileAppender.write(fsImage);
      File previousFile = new File(treeDir + PREVIOUS_FILE_NAME);
      if (previousFile.exists()) {
        File previousHistoryFile = new File(
            treeDir + PREVIOUS_FILE_PRE + "\\" + +System.currentTimeMillis() + "_"
                + PREVIOUS_FILE_NAME);
        FileUtils.moveFile(previousFile, previousHistoryFile);
      }
      File currentFile = new File(treeDir + CURRENT_FILE_DIR + "\\" + CURRENT_FILE_NAME);
      if (currentFile.exists()) {
        File newPreviousFile = new File(treeDir + PREVIOUS_FILE_NAME);
        FileUtils.moveFile(currentFile, newPreviousFile);
      }
      tempFileAppender.close();
      File newFile = new File(treeDir + CURRENT_FILE_DIR + "\\" + CURRENT_FILE_NAME);
      FileUtils.moveFile(tempFile, newFile);
      fileDirectoryTree.getUpdateFlag().set(false);
      System.out.println("镜像落盘成功");
      return true;
    } catch (Exception e) {
      tempFile.delete();
      return false;
    } finally {
      if (tempFileAppender != null) {
        try {
          tempFileAppender.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

  }

  /**
   * 加载磁盘文件解析成文件目录树内存数据,namenode故障重启后调用
   */
  public FileDirectoryTree loadDiskParseData() {
    RandomAccessFile rds = null;
    try {
      isBuildStatus.set(true);
      //1镜像文件
      File currentFile = new File(treeDir + CURRENT_FILE_NAME);
      System.out.println("开始解析构造镜像文件");
      if (currentFile.exists()) {
        //加载主镜像文件
        rds = new RandomAccessFile(currentFile, "r");
        if (rds.length() > 0) {
          byte[] dataByte = new byte[(int) rds.length()];
          rds.read(dataByte);
          FileDirectoryTree diskTree = new FileDirectoryTree();
          String dataStr = new String(dataByte);
          DirNode rootNode = JSONObject.parseObject(dataStr, DirNode.class);
          diskTree.setRootNode(rootNode);
          if (rootNode != null) {
            fileDirectoryTree = diskTree;
          }
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
            List<OperateLog> editLog = JSONObject
                .parseArray(new String(editByte), OperateLog.class);
            fileDirectoryTree.buildEditLog(editLog);
            editRds.close();
          }
        }
        //重新构建后镜像再次落盘
        boolean buildFlag = loadFileTreeDiskFile();
        //成功后更改父类名备份
        if (buildFlag) {
          FileUtils.deleteDirectory(editParentFile);
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      isBuildStatus.set(false);
      try {
        if (rds != null) {
          rds.close();
        }
      } catch (IOException e) {
        log.error("rds关闭失败");
        e.printStackTrace();
      }
    }

    return fileDirectoryTree;


  }

  /**
   * 将操作日志刷入磁盘
   */
  public void loadOperateLog(OperateLogBuffer operateLogBuffer) throws IOException {
    if (operateLogBuffer.getIsNowFlush().get() || !operateLogBuffer.getIsFLushData()) {
      return;
    }
    operateLogBuffer.getIsNowFlush().compareAndSet(false, true);
    while (isBuildStatus.get()) {
      log.info("元数据正在重新构建---");
      try {
        Thread.sleep(1000);//发生在nameNode重启过程中需要等待
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    System.out.println("editLog开始落盘");
    long currentIndex = operateLogBuffer.getCurrentFlushIndex();
    long startIndex = operateLogBuffer.getStartFlushIndex();
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
      byte[] bytes = JSONObject.toJSONString(operateLogBuffer.getFlushLogList()).getBytes();
      //转byte数组
      rds.write(bytes);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      //变更状态
      operateLogBuffer.setLastFlushIndex(currentIndex);
      operateLogBuffer.clearFlushList();
      operateLogBuffer.setIsFLushData(false);
      if (rds != null) {
        rds.close();
      }
    }
    System.out.println("当前日志段落盘成功" + startIndex + "_" + currentIndex);
    operateLogBuffer.getIsNowFlush().set(false);
  }


}
