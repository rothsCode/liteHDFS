package com.rothsCode.litehdfs.namenode.file;

import cn.hutool.core.io.FileUtil;
import com.rothsCode.litehdfs.common.file.FileAppender;
import com.rothsCode.litehdfs.common.protoc.OperateLog;
import com.rothsCode.litehdfs.namenode.config.NameNodeConfig;
import com.rothsCode.litehdfs.namenode.filetree.FileDirectoryTree;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
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

  private final static String CURRENT_FILE_NAME = "currentImage";//当前版本
  private final static String PREVIOUS_FILE_NAME = "previousImage";//上一个版本
  private final static String TEMP_FILE_NAME = "tempImage";//中间临时版本
  private final static String EDIT_LOG_PATH = "\\namenode\\editLog\\";//临时修改日志
  private final static String PREVIOUS_FILE_PRE = "previousHistory\\";
  private final static String FILE_IMAGE_DIR = "\\namenode\\fileImage\\";
  private NameNodeConfig nameNodeConfig;
  private FileDirectoryTree fileDirectoryTree;
  private String fileImagePath;
  private AtomicBoolean isBuildStatus = new AtomicBoolean(false);//是否正在重启构建

  public DiskFileSystem(NameNodeConfig nameNodeConfig, FileDirectoryTree fileDirectoryTree) {
    this.fileDirectoryTree = fileDirectoryTree;
    this.nameNodeConfig = nameNodeConfig;
    fileImagePath = nameNodeConfig.getNameNodePath() + FILE_IMAGE_DIR;
    File fileDir = new File(fileImagePath);
    if (!fileDir.exists()) {
      fileDir.mkdirs();
    }
    File previousDir = new File(fileImagePath + PREVIOUS_FILE_PRE);
    if (!previousDir.exists()) {
      previousDir.mkdirs();
    }
    File editParentFile = new File(nameNodeConfig.getNameNodePath() + EDIT_LOG_PATH);
    if (!editParentFile.exists()) {
      editParentFile.mkdirs();
    }
  }


  /**
   * 先将更新文件写入temp文件,再將previous文件更名為previous_當前时间， 再将current 更名为previous 最后将temp文件更改为current
   * 如果写入失败则删除temp
   */
  public boolean storageFsImage() {
    if (!fileDirectoryTree.getUpdateFlag().get()) {
      return false;
    }
    log.info("fsImage start storageFile");
    long startStorageTime = System.currentTimeMillis();
    // previous,current,temp 三个镜像文件
    FileAppender temFileAppender = null;
    try {
      // append tempImage
      File temFile = new File(fileImagePath + TEMP_FILE_NAME);
      if (temFile.exists()) {
        temFile.delete();
      }
      temFileAppender = new FileAppender(fileImagePath, TEMP_FILE_NAME);
      ByteBuffer fsImageBuffer = fileDirectoryTree.getFsImage();
      temFileAppender.syncDisk(fsImageBuffer);
      temFileAppender.close();
      //previous->previous-time
      File previousFile = new File(fileImagePath + PREVIOUS_FILE_NAME);
      if (nameNodeConfig.isStorageFsImageHistory() && previousFile.exists()) {
        String historyFileName = System.currentTimeMillis() + "_" + PREVIOUS_FILE_NAME;
        FileUtil.rename(previousFile, fileImagePath + PREVIOUS_FILE_PRE + historyFileName, true);
      }
      //current->previous
      File currentFile = new File(fileImagePath + CURRENT_FILE_NAME);
      if (currentFile.exists()) {
        FileUtil.rename(currentFile, fileImagePath + PREVIOUS_FILE_NAME, true);
      }
      //tem->current
      FileUtil.rename(temFile, fileImagePath + CURRENT_FILE_NAME, true);
      fileDirectoryTree.getUpdateFlag().set(false);
      long maxTxId = fileDirectoryTree.getMaxTxId();
      // 镜像存储成功后删除检查点之前的操作日志
      File editParentFile = new File(nameNodeConfig.getNameNodePath() + EDIT_LOG_PATH);
      if (editParentFile.exists() && editParentFile.isDirectory()) {
        File[] editFiles = editParentFile.listFiles();
        if (editFiles != null && editFiles.length > 0) {
          for (File editFile : editFiles) {
            String[] startAndEndTxId = editFile.getName().split("_");
            long endTxId = Long.parseLong(startAndEndTxId[1]);
            if (maxTxId >= endTxId) {
              FileUtils.deleteQuietly(editFile);
            }
          }
        }
      }
      long endStorageTime = System.currentTimeMillis();
      log.info("fsImage storageDisk success cost:{}ms", endStorageTime - startStorageTime);
      return true;
    } catch (Exception e) {
      log.error("storageFsImage error:{}", e);
      return false;
    } finally {
      if (temFileAppender != null) {
        //删除临时文件
        temFileAppender.deleteFile();
      }
    }

  }

  /**
   * 加载磁盘文件解析成文件目录树内存数据,namenode故障重启后调用
   */
  public void recoverFileImage() {
    isBuildStatus.set(true);
    log.info("start recoverFileImage");
    long startRecoverTime = System.currentTimeMillis();
    //1 加载全量镜像文件  先看temp文件是否存在，再看current文件是否存在
    File dataFile = new File(fileImagePath + TEMP_FILE_NAME);
    if (!dataFile.exists()) {
      dataFile = new File(fileImagePath + CURRENT_FILE_NAME);
    }
    long maxTxId = 0;
    if (dataFile.exists()) {
      //1加载主镜像文件
      long startBuildFileTreeTime = System.currentTimeMillis();
      FileAppender imageFileAppender = new FileAppender(dataFile);
      ByteBuffer imageBuffer = imageFileAppender.readBody();
      imageFileAppender.close();
      //恢复数据
      fileDirectoryTree.recoverFs(imageBuffer);
      log.info("buildFileTreeTime complete cost:{}ms",
          System.currentTimeMillis() - startBuildFileTreeTime);
      maxTxId = fileDirectoryTree.getMaxTxId();
    }
    //2加载包含txId检查点以及之后的editLog文件
    List<File> deleteFiles = new ArrayList<>();
    File editParentFile = new File(nameNodeConfig.getNameNodePath() + EDIT_LOG_PATH);
    if (editParentFile.exists() && editParentFile.isDirectory()) {
      File[] editFiles = editParentFile.listFiles();
      if (editFiles != null && editFiles.length > 0) {
        log.info("start recoverEditLog");
        for (File editFile : editFiles) {
          if (editFile.length() == 0) {
            continue;
          }
          String[] startAndEndTxId = editFile.getName().split("_");
          long startTxId = Long.parseLong(startAndEndTxId[0]);
          long endTxId = Long.parseLong(startAndEndTxId[1]);
          if (maxTxId > startTxId) {
            deleteFiles.add(editFile);
            if (maxTxId > endTxId) {
              continue;
            }
          }
          FileAppender fileAppender = new FileAppender(editFile);
          ByteBuffer byteBuffer = fileAppender.readBody();
          fileAppender.close();
          List<OperateLog> editLogs = new ArrayList<>();
          long fileMaxTxId = 0;
          while (byteBuffer.remaining() > 4) {
            try {
              int bodyLength = byteBuffer.getInt();
              byte[] bodyBytes = new byte[bodyLength];
              byteBuffer.get(bodyBytes);
              OperateLog operateLog = OperateLog.parseFrom(bodyBytes);
              editLogs.add(operateLog);
              if (operateLog.getTxId() > fileMaxTxId) {
                fileMaxTxId = operateLog.getTxId();
              }
            } catch (Exception e) {
              log.error("{}:editFile recoverEditLogError:{}", editFile.getName(), e);
              deleteFiles.remove(editFile);
              break;
            }
          }
          fileDirectoryTree.buildEditLog(editLogs);
          fileDirectoryTree.setMaxTxId(fileMaxTxId);
        }
      }
      //3重新构建后镜像再次落盘保存镜像
      boolean buildFlag = storageFsImage();
      //4镜像落盘成功后删除变更日志
      if (buildFlag) {
        deleteFiles.forEach(FileUtil::del);
      }
    }
    isBuildStatus.set(false);
    log.info("recoverImage complete cost:{}ms", System.currentTimeMillis() - startRecoverTime);
  }

  /**
   * 将目录操作日志刷入磁盘
   */
  public void batchFlushPathOperateLog(OperateLogDoubleBuffer operateLogDoubleBuffer) {
    synchronized (this) {
      if (operateLogDoubleBuffer.isNowFlush()
          || operateLogDoubleBuffer.getFlushBuffer().position() <= 0) {
        return;
      }
      operateLogDoubleBuffer.setIsNowFlush(true);
    }
    log.debug("operateLog start flushDisk");
    long endIndex = operateLogDoubleBuffer.getCurrentFlushIndex();
    long startIndex = operateLogDoubleBuffer.getStartFlushIndex();
    FileAppender editLogFileAppender = new FileAppender(
        nameNodeConfig.getNameNodePath() + EDIT_LOG_PATH,
        startIndex + "_" + endIndex);
    editLogFileAppender.syncDisk(operateLogDoubleBuffer.getFlushBuffer());
    editLogFileAppender.close();
    //变更状态
    operateLogDoubleBuffer.setLastFlushIndex(endIndex);
    operateLogDoubleBuffer.clearFlushBuffer();
    operateLogDoubleBuffer.setIsNowFlush(false);
    log.debug("operateLog flush success" + startIndex + "_" + endIndex);
  }


}
