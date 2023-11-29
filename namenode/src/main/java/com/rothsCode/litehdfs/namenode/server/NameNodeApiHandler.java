package com.rothsCode.litehdfs.namenode.server;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.rothsCode.litehdfs.common.enums.FlushDiskType;
import com.rothsCode.litehdfs.common.enums.PacketType;
import com.rothsCode.litehdfs.common.netty.handler.AbstractDataHandler;
import com.rothsCode.litehdfs.common.netty.request.ClientToNameNodeRequest;
import com.rothsCode.litehdfs.common.netty.request.DataNodeRequest;
import com.rothsCode.litehdfs.common.netty.request.FileInfo;
import com.rothsCode.litehdfs.common.netty.request.NettyPacket;
import com.rothsCode.litehdfs.common.netty.response.FileResponse;
import com.rothsCode.litehdfs.common.netty.response.ServerResponse;
import com.rothsCode.litehdfs.common.netty.thread.DefaultScheduler;
import com.rothsCode.litehdfs.common.netty.vo.DataNodeInfo;
import com.rothsCode.litehdfs.common.protoc.OperateLog;
import com.rothsCode.litehdfs.common.protoc.ProtoFileInfo;
import com.rothsCode.litehdfs.datanode.DataNodeInfoManager;
import com.rothsCode.litehdfs.namenode.config.NameNodeConfig;
import com.rothsCode.litehdfs.namenode.file.DefaultOperateLogStore;
import com.rothsCode.litehdfs.namenode.file.DiskFileSystem;
import com.rothsCode.litehdfs.namenode.file.OperateLogDoubleBuffer;
import com.rothsCode.litehdfs.namenode.filetree.DirNode;
import com.rothsCode.litehdfs.namenode.filetree.FileDirectoryTree;
import com.rothsCode.litehdfs.namenode.user.UserManager;
import com.rothsCode.litehdfs.namenode.vo.PathOperateType;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author rothsCode
 * @Description:请求处理类
 * @date 2021/11/8 15:29
 */
@Slf4j
public class NameNodeApiHandler extends AbstractDataHandler {

  private DataNodeInfoManager dataNodeManager;
  private FileDirectoryTree fileDirectoryTree;
  private Executor executor;
  private OperateLogDoubleBuffer operateLogDoubleBuffer;
  private DefaultScheduler defaultScheduler;
  private DiskFileSystem diskFileSystem;
  private UserManager userManager;
  private NameNodeConfig nameNodeConfig;
  private boolean bufferFlushDisk;
  private DefaultOperateLogStore defaultOperateLogStore;


  public NameNodeApiHandler(NameNodeConfig nameNodeConfig, UserManager userManager,
      DiskFileSystem diskFileSystem, DefaultScheduler defaultScheduler,
      DataNodeInfoManager dataNodeManager, FileDirectoryTree fileDirectoryTree) {
    this.nameNodeConfig = nameNodeConfig;
    this.userManager = userManager;
    this.diskFileSystem = diskFileSystem;
    this.dataNodeManager = dataNodeManager;
    this.fileDirectoryTree = fileDirectoryTree;
    this.defaultScheduler = defaultScheduler;
    executor = new ThreadPoolExecutor(0, 65536, 60, TimeUnit.SECONDS,
        new SynchronousQueue<>());

    //元数据变更日志异步落盘任务
    if (nameNodeConfig.getEditLogFlushDiskType().equals(FlushDiskType.ASYNC_BUFFER_FLUSH.value)) {
      this.operateLogDoubleBuffer = new OperateLogDoubleBuffer(nameNodeConfig, diskFileSystem,
          fileDirectoryTree.getMaxTxId());
      defaultScheduler.schedule("editLogAsyncFlushDiskTask", new EditLogRunnable(), 10,
          nameNodeConfig.getEditLogSyncInterval(),
          TimeUnit.SECONDS);
      log.info("editLogAsyncFlushDiskTask started");
      bufferFlushDisk = true;
    } else {
      defaultOperateLogStore = new DefaultOperateLogStore(nameNodeConfig,
          fileDirectoryTree.getMaxTxId());
      log.info("defaultOperateLogStore started");
    }
    //文件树镜像落盘任务,性能考虑落盘周期必须大于等于30分钟
    //Assert.isTrue(nameNodeConfig.getFsImageSyncInterval()>=30,"fileTreeScheduleTime must greater than 30 minute!");
    defaultScheduler
        .schedule("storageFileTreeTask", diskFileSystem::storageFsImage, 1,
            nameNodeConfig.getFsImageSyncInterval(),
            TimeUnit.MINUTES);
    log.info("storageFileTreeTask started");
  }


  @Override
  public boolean handleMsg(NettyPacket nettyPacket, ChannelHandlerContext ctx) {
    int packetValue = nettyPacket.getPackageType();
    PacketType packetType = PacketType.getEnum(packetValue);
    RequestWrapper requestWrapper = new RequestWrapper(
        nettyPacket.getSequence(), packetValue, ctx);
    //处理注册请求
    byte[] body = nettyPacket.getBody();
    DataNodeRequest request = JSONObject.parseObject(new String(body), DataNodeRequest.class);
    switch (packetType) {
      case DATA_NODE_REGISTER:
        DataNodeInfo dataNodeInfo = DataNodeInfo.builder()
            .address(request.getAddress()).ip(request.getIp())
            .port(request.getPort()).httpPort(request.getHttpPort())
            .healthyStatus(request.getHealthyStatus())
            .remainSpaceSize(request.getRemainSpaceSize())
            .usedSpaceSize(request.getUsedSpaceSize()).build();
        dataNodeManager.register(dataNodeInfo);
        requestWrapper.sendResponse(ServerResponse.success());
        break;
      case HEART_BRET:
        //处理心跳请求
        dataNodeManager.heartBeat(request.getAddress());
        break;
      case MKDIR:
        //处理文件目录请求
        if (makeDir(nettyPacket, requestWrapper)) {
          return false;
        }
        break;
      case CREATE_FILE:
        //处理创建文件请求
        createFile(nettyPacket, requestWrapper);
        break;
      case REPLICA_RECEIVE:
        recordFileDataNodeRelation(nettyPacket, requestWrapper);
        break;
      case CLIENT_LIST_FILES:
        ClientToNameNodeRequest dirPathsRequest = JSONObject
            .parseObject(new String(nettyPacket.getBody()), ClientToNameNodeRequest.class);
        String parentPath = dirPathsRequest.getPath();
        checkToken(requestWrapper, dirPathsRequest);
        if (parentPath == null) {
          return false;
        }
        List<DirNode> childPaths = fileDirectoryTree.listFiles(parentPath);
        requestWrapper.sendResponse(ServerResponse.successByData(childPaths));
        break;
      case REPORT_STORAGE_INFO:
        if (request.getFileInfos().size() > 0) {
          for (ProtoFileInfo info : request.getFileInfos()) {
            dataNodeManager.addReplicateFile(request.getAddress(), info);
          }
        }
        break;
      case GET_DATA_NODE_FOR_FILE:
        ClientToNameNodeRequest downFileRequest = JSONObject
            .parseObject(new String(nettyPacket.getBody()), ClientToNameNodeRequest.class);
        String fileName = downFileRequest.getFileName();
        FileInfo fileInfo = fileDirectoryTree.getFileInfoByPath(fileName);
        if (fileInfo == null || CollectionUtil.isEmpty(fileInfo.getBlkDataNodes())) {
          requestWrapper.sendResponse(ServerResponse.failByMsg("file is not existed"));
        }
        requestWrapper.sendResponse(ServerResponse.successByData(fileInfo));
        break;
      case AUTHENTICATE:
        ClientToNameNodeRequest authRequest = JSONObject
            .parseObject(new String(nettyPacket.getBody()), ClientToNameNodeRequest.class);
        String token = userManager
            .login(authRequest.getUserName(), authRequest.getPassWord());
        if (token != null) {
          requestWrapper.sendResponse(ServerResponse.successByData(token));

        } else {
          requestWrapper.sendResponse(ServerResponse.failByMsg("token error"));
        }
        break;
      case CLIENT_LOGOUT:
        ClientToNameNodeRequest loginOutRequest = JSONObject
            .parseObject(new String(nettyPacket.getBody()), ClientToNameNodeRequest.class);
        boolean loginOutFlag = userManager.loginOut(loginOutRequest.getUserName());
        if (loginOutFlag) {
          requestWrapper.sendResponse(ServerResponse.success());
        } else {
          requestWrapper.sendResponse(ServerResponse.failByMsg("loginOut fair"));
        }
        break;
      default:
        log.error("packetType error");

    }
    return true;
  }

  private void recordFileDataNodeRelation(NettyPacket nettyPacket,
      RequestWrapper requestWrapper) {
    FileInfo fileInfo = JSONObject.parseObject(new String(nettyPacket.getBody()), FileInfo.class);
    ProtoFileInfo protoFileInfo = fileInfo.encoder();
    //先预写日志再更新内存数据
    OperateLog operateLog = OperateLog.newBuilder()
        .setOperateType(PathOperateType.FILE.name())
        .setFileInfo(protoFileInfo)
        .build();
    boolean writeLogFlag = writePathOperateLog(operateLog);
    if (!writeLogFlag) {
      requestWrapper.sendResponse(ServerResponse.failByMsg("writeLog busy"));
      return;
    }
    fileDirectoryTree.saveFileInfo(fileInfo);
    requestWrapper.sendResponse(ServerResponse.success());
  }

  private void createFile(NettyPacket nettyPacket, RequestWrapper requestWrapper) {
    ClientToNameNodeRequest createFileRequest = JSONObject
        .parseObject(new String(nettyPacket.getBody()), ClientToNameNodeRequest.class);
    String realFileName =
        createFileRequest.getPath() + "BLK" + createFileRequest.getBlockIndex() + "-"
            + createFileRequest.getFileName();
    //返回最优的数据节点
    List<DataNodeInfo> dataNodeInfoList = dataNodeManager.chooseBestDataNode();
    FileResponse fileResponse = FileResponse.builder().fileName(realFileName)
        .parentFileName(createFileRequest.getPath() + createFileRequest.getFileName())
        .dataNodeInfos(dataNodeInfoList).build();
    requestWrapper.sendResponse(ServerResponse.successByData(fileResponse));
  }

  private boolean makeDir(NettyPacket nettyPacket, RequestWrapper requestWrapper) {
    ClientToNameNodeRequest clientToNameNodeRequest = JSONObject
        .parseObject(new String(nettyPacket.getBody()), ClientToNameNodeRequest.class);
    String dirPath = clientToNameNodeRequest.getPath();
    checkToken(requestWrapper, clientToNameNodeRequest);
    if (StringUtils.isEmpty(dirPath)) {
      return false;
    }

    //WAL预写日志 记录操作日志备份磁盘 判断是同步刷盘还是异步刷盘
    OperateLog pathOperateLog = OperateLog.newBuilder()
        .setOperateType(PathOperateType.PATH.name())
        .setPath(dirPath)
        .build();
    boolean writeLogFlag = writePathOperateLog(pathOperateLog);
    if (!writeLogFlag) {
      requestWrapper.sendResponse(ServerResponse.failByMsg("writeLog busy"));
      return false;
    }
    boolean mkrFlag = fileDirectoryTree.makeDir(dirPath);
    requestWrapper.sendResponse(ServerResponse.responseStatus(mkrFlag));
    return false;
  }

  private boolean writePathOperateLog(OperateLog pathOperateLog) {
    long txId;
    if (bufferFlushDisk) {
      txId = operateLogDoubleBuffer.addOperateLog(pathOperateLog);
    } else {
      txId = defaultOperateLogStore.syncOperateLogDisk(pathOperateLog);
    }
    if (txId == 0) {
      return false;
    }
    fileDirectoryTree.setMaxTxId(txId);
    return true;
  }

  private void checkToken(RequestWrapper requestWrapper,
      ClientToNameNodeRequest clientToNameNodeRequest) {
//        Boolean checkFlag =  userManager.checkToken(clientToNameNodeRequest.getUserName(),clientToNameNodeRequest.getAuthToken());
//        if(!checkFlag){
//            requestWrapper.sendResponse(ServerResponse.failByMsg("权限认证不通过"));
//        }
  }

  @Override
  public Executor getExecutor() {
    return executor;
  }

  public class EditLogRunnable implements Runnable {

    @Override
    public void run() {
        try {
          //定时交换队列
          operateLogDoubleBuffer.exchangeDoubleBuffer();
          diskFileSystem.batchFlushPathOperateLog(operateLogDoubleBuffer);
        } catch (Exception e) {
          log.error("batchSavePathOperateLog error:{}", e);
        }
    }
  }
}
