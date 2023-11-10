package com.rothsCode.litehdfs.namenode.server;

import com.alibaba.fastjson.JSONObject;
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
import com.rothsCode.litehdfs.datanode.DataNodeInfoManager;
import com.rothsCode.litehdfs.namenode.config.NameNodeConfig;
import com.rothsCode.litehdfs.namenode.file.DiskFileSystem;
import com.rothsCode.litehdfs.namenode.file.PathOperateLogBuffer;
import com.rothsCode.litehdfs.namenode.filetree.DirNode;
import com.rothsCode.litehdfs.namenode.filetree.FileDirectoryTree;
import com.rothsCode.litehdfs.namenode.user.UserManager;
import com.rothsCode.litehdfs.namenode.vo.PathOperateLog;
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
  private PathOperateLogBuffer PathOperateLogBuffer;
  private DefaultScheduler defaultScheduler;
  private DiskFileSystem diskFileSystem;
  private UserManager userManager;
  private NameNodeConfig nameNodeConfig;


  public NameNodeApiHandler(NameNodeConfig nameNodeConfig, UserManager userManager,
      DiskFileSystem diskFileSystem, DefaultScheduler defaultScheduler,
      DataNodeInfoManager dataNodeManager, FileDirectoryTree fileDirectoryTree) {
    this.nameNodeConfig = nameNodeConfig;
    this.userManager = userManager;
    this.diskFileSystem = diskFileSystem;
    this.PathOperateLogBuffer = new PathOperateLogBuffer();
    this.dataNodeManager = dataNodeManager;
    this.fileDirectoryTree = fileDirectoryTree;
    this.defaultScheduler = defaultScheduler;
    executor = new ThreadPoolExecutor(0, 65536, 60, TimeUnit.SECONDS,
        new SynchronousQueue<>());

    //元数据变更日志落盘任务
    if (nameNodeConfig.getEditLogSyncInterval() > 0) {
      defaultScheduler.schedule("editLogSyncTask", new EditLogRunnable(), 10,
          nameNodeConfig.getEditLogSyncInterval(),
          TimeUnit.SECONDS);
    }
    //文件树镜像落盘任务
    defaultScheduler
        .schedule("storageFileTreeTask", diskFileSystem::storageFileTreeDiskFile, 60, 120,
            TimeUnit.SECONDS);
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
          for (FileInfo info : request.getFileInfos()) {
            dataNodeManager.addReplicateFile(request.getAddress(), info);
          }
        }
        break;
      case GET_DATA_NODE_FOR_FILE:
        ClientToNameNodeRequest downFileRequest = JSONObject
            .parseObject(new String(nettyPacket.getBody()), ClientToNameNodeRequest.class);
        String fileName = downFileRequest.getFileName();
        FileInfo fileInfo = fileDirectoryTree.getFileInfoByPath(fileName);
        if (fileInfo == null || StringUtils.isEmpty(fileInfo.getBlkDataNode())) {
          requestWrapper.sendResponse(ServerResponse.failByMsg("file is empty"));
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
          requestWrapper.sendResponse(ServerResponse.failByMsg("登录错误"));
        }
        break;
      case CLIENT_LOGOUT:
        ClientToNameNodeRequest loginOutRequest = JSONObject
            .parseObject(new String(nettyPacket.getBody()), ClientToNameNodeRequest.class);
        boolean loginOutFlag = userManager.loginOut(loginOutRequest.getUserName());
        if (loginOutFlag) {
          requestWrapper.sendResponse(ServerResponse.success());
        } else {
          requestWrapper.sendResponse(ServerResponse.failByMsg("退出失败"));
        }
        break;
      default:
        log.error("消息类型异常");

    }
    return true;
  }

  private void recordFileDataNodeRelation(NettyPacket nettyPacket,
      RequestWrapper requestWrapper) {
    FileInfo dataNodeFileDto = JSONObject
        .parseObject(new String(nettyPacket.getBody()), FileInfo.class);
    //先预写日志再更新内存数据
    PathOperateLog pathOperateLog = new PathOperateLog();
    pathOperateLog.setOperateType(PathOperateType.FILE.name());
    pathOperateLog.setContent(JSONObject.toJSONString(dataNodeFileDto));
    try {
      PathOperateLogBuffer.addPathOperateLog(pathOperateLog);
    } catch (InterruptedException e) {
      requestWrapper.sendResponse(ServerResponse.fail());
      return;
    }
    FileInfo fileInfo = fileDirectoryTree.getFileInfoByPath(dataNodeFileDto.getParentFileName());
    if (fileInfo != null) {
      fileInfo.setBlkDataNode(fileInfo.getBlkDataNode() + ";" + dataNodeFileDto.getBlkDataNode());
    } else {
      fileDirectoryTree.makeFileNode(dataNodeFileDto.getParentFileName(), dataNodeFileDto);
    }
    requestWrapper.sendResponse(ServerResponse.success());
  }

  private void createFile(NettyPacket nettyPacket, RequestWrapper requestWrapper) {
    ClientToNameNodeRequest createFileRequest = JSONObject
        .parseObject(new String(nettyPacket.getBody()), ClientToNameNodeRequest.class);
    String realFileName =
        createFileRequest.getPath() + "/"
            + "BLK" + createFileRequest.getBlockIndex() + "-" + createFileRequest.getFileName();
    //返回最优的数据节点
    List<DataNodeInfo> dataNodeInfoList = dataNodeManager.chooseBestDataNode();
    FileResponse fileResponse = FileResponse.builder().fileName(realFileName)
        .parentFileName(createFileRequest.getPath() + "/" + createFileRequest.getFileName())
        .dataNodeInfos(dataNodeInfoList).build();
    requestWrapper.sendResponse(ServerResponse.successByData(fileResponse));
  }

  private boolean makeDir(NettyPacket nettyPacket, RequestWrapper requestWrapper) {
    ClientToNameNodeRequest clientToNameNodeRequest = JSONObject
        .parseObject(new String(nettyPacket.getBody()), ClientToNameNodeRequest.class);
    String dirPath = clientToNameNodeRequest.getPath();
    checkToken(requestWrapper, clientToNameNodeRequest);
    if (dirPath == null) {
      return true;
    }
    //WAL预写日志
    //记录操作日志备份磁盘
    PathOperateLog pathOperateLog = new PathOperateLog();
    pathOperateLog.setOperateType(PathOperateType.ADD.name());
    pathOperateLog.setContent(dirPath);
    try {
      PathOperateLogBuffer.addPathOperateLog(pathOperateLog);
    } catch (InterruptedException e) {
      requestWrapper.sendResponse(ServerResponse.fail());
      return true;
    }
    boolean mkrFlag = fileDirectoryTree.makeDir(dirPath);
    requestWrapper.sendResponse(ServerResponse.responseStatus(mkrFlag));
    return false;
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
      while (true) {
        try {
          diskFileSystem.savePathOperateLog(PathOperateLogBuffer);
        } catch (Exception e) {
          log.error("loadPathOperateLog error:{}", e);
        }
      }
    }
  }
}
