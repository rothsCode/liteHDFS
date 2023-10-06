package com.rothsCode.namenode;

import com.alibaba.fastjson.JSONObject;
import com.rothsCode.NameNodeConfig;
import com.rothsCode.backup.BackNodeInfo;
import com.rothsCode.datanode.DataNodeManager;
import com.rothsCode.net.AbstractDataHandler;
import com.rothsCode.net.DataNodeInfo;
import com.rothsCode.net.DefaultScheduler;
import com.rothsCode.net.FileResponse;
import com.rothsCode.net.request.ClientToNameNodeRequest;
import com.rothsCode.net.request.DataNodeRequest;
import com.rothsCode.net.request.FileInfo;
import com.rothsCode.net.request.NettyPacket;
import com.rothsCode.net.response.ServerResponse;
import com.rothsCode.vo.OperateLog;
import com.rothsCode.vo.OperateType;
import io.netty.channel.ChannelHandlerContext;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * @author rothsCode
 * @Description:请求处理类
 * @date 2021/11/8 15:29
 */
@Slf4j
public class NameNodeApiHandler extends AbstractDataHandler {

    private DataNodeManager dataNodeManager;
    private FileDirectoryTree fileDirectoryTree;
    private Executor executor;
    private OperateLogBuffer operateLogBuffer;
    private DefaultScheduler defaultScheduler;
    private DiskFileSystem diskFileSystem;
    private UserManager userManager;
    private BackNodeInfo backNodeInfo;
    private NameNodeConfig nameNodeConfig;

    public NameNodeApiHandler(NameNodeConfig nameNodeConfig, UserManager userManager,
        DiskFileSystem diskFileSystem, DefaultScheduler defaultScheduler,
        DataNodeManager dataNodeManager, FileDirectoryTree fileDirectoryTree) {
        this.nameNodeConfig = nameNodeConfig;
        this.userManager = userManager;
        this.diskFileSystem = diskFileSystem;
        this.operateLogBuffer = new OperateLogBuffer();
        this.dataNodeManager = dataNodeManager;
        this.fileDirectoryTree = fileDirectoryTree;
        this.defaultScheduler = defaultScheduler;
        executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
            new SynchronousQueue<>());
        ExecutorService editService = Executors.newFixedThreadPool(1);
        editService.execute(new EditLogRunnable());//修改日志落盘任务
        defaultScheduler.schedule("定时解析日志并备份镜像任务", diskFileSystem::loadFileTreeDiskFile, 60, 120,
            TimeUnit.SECONDS);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
    }

    @Override
    public boolean handleMsg(NettyPacket nettyPacket, ChannelHandlerContext ctx) {
        int packetValue = nettyPacket.getPackageType();
        com.rothsCode.net.PacketType packetType = com.rothsCode.net.PacketType.getEnum(packetValue);
        com.rothsCode.namenode.RequestWrapper requestWrapper = new com.rothsCode.namenode.RequestWrapper(
            nettyPacket.getSequence(), packetValue, ctx);
        //处理注册请求
        byte[] body = nettyPacket.getBody();
        DataNodeRequest request = JSONObject.parseObject(new String(body), DataNodeRequest.class);
        switch (packetType) {
            case DATA_NODE_REGISTER:
                DataNodeInfo dataNodeInfo = DataNodeInfo.builder().hostName(request.getHostName())
                    .port(request.getPort()).httpPort(request.getHttpPort())
                    .healthyStatus(request.getHealthyStatus())
                    .remainSpaceSize(request.getRemainSpaceSize())
                    .usedSpaceSize(request.getUsedSpaceSize()).build();
                dataNodeManager.register(dataNodeInfo);
                requestWrapper.sendResponse(ServerResponse.success());
                break;
            case HEART_BRET:
                //处理心跳请求
                dataNodeManager.heartBeat(request.getHostName());
                break;
            case MKDIR:
                //处理文件目录请求
                ClientToNameNodeRequest clientToNameNodeRequest = JSONObject
                    .parseObject(new String(nettyPacket.getBody()), ClientToNameNodeRequest.class);
                String dirPath = clientToNameNodeRequest.getPath();
                checkToken(requestWrapper, clientToNameNodeRequest);
                if (dirPath == null) {
                    return false;
                }
                boolean mkrFlag = fileDirectoryTree.makeDir(dirPath);
                if (mkrFlag) {
                    //记录操作日志备份磁盘
                    OperateLog operateLog = OperateLog.builder().operateType(OperateType.ADD.name())
                        .content(dirPath).build();
                    try {
                        operateLogBuffer.addOperateLog(operateLog);
                    } catch (InterruptedException e) {
                        requestWrapper.sendResponse(ServerResponse.responseStatus(false));
                        break;
                    }
                }
                requestWrapper.sendResponse(ServerResponse.responseStatus(mkrFlag));
                break;
            case CREATE_FILE:
                //处理创建文件请求
                ClientToNameNodeRequest createFileRequest = JSONObject
                    .parseObject(new String(nettyPacket.getBody()), ClientToNameNodeRequest.class);
                checkToken(requestWrapper, createFileRequest);
                long currentTime = System.currentTimeMillis();
                String realFileName =
                    File.separator + createFileRequest.getUserId() + File.separator
                        + createFileRequest.getFileName();
                FileInfo fileInfo = FileInfo.builder().fileName(realFileName)
                    .fileSize(createFileRequest.getFileSize())
                    .createTime(currentTime).updateTime(currentTime).build();
                //1:在目录树中增加文件属性信息
                boolean addFlag = fileDirectoryTree
                    .AddFileInfo(createFileRequest.getPath(), fileInfo);
                //2：返回最优的数据节点
                if (addFlag) {
                    List<DataNodeInfo> dataNodeInfoList = dataNodeManager.chooseBestDataNode();
                    FileResponse fileResponse = FileResponse.builder().fileName(realFileName)
                        .dataNodeInfos(dataNodeInfoList).build();
                    System.out.println("返回最优的数据节点");
                    requestWrapper.sendResponse(ServerResponse.successByData(fileResponse));
                } else {
                    requestWrapper.sendResponse(ServerResponse.fail());
                }
                break;
            case REPLICA_RECEIVE:
                FileInfo dataNodeFileInfo = JSONObject
                    .parseObject(new String(nettyPacket.getBody()), FileInfo.class);
                log.info("收到文件副本请求");
                dataNodeManager.addReplicateFile(dataNodeFileInfo.getHostName(), dataNodeFileInfo);
                requestWrapper.sendResponse(ServerResponse.success());
                break;
            case CLIENT_LIST_FILES:
                ClientToNameNodeRequest dirPathsRequest = JSONObject
                    .parseObject(new String(nettyPacket.getBody()), ClientToNameNodeRequest.class);
                String parentPath = dirPathsRequest.getPath();
                checkToken(requestWrapper, dirPathsRequest);
                if (parentPath == null) {
                    return false;
                }
                List<String> childPaths = fileDirectoryTree.getChildPaths(parentPath);
                requestWrapper.sendResponse(ServerResponse.successByData(childPaths));
                break;
            case REPORT_STORAGE_INFO:
                String hostName = request.getHostName();
                if (request.getFileInfos().size() > 0) {
                    for (FileInfo info : request.getFileInfos()) {
                        dataNodeManager.addReplicateFile(hostName, info);
                    }
                }
                break;
            case GET_DATA_NODE_FOR_FILE:
                ClientToNameNodeRequest downFileRequest = JSONObject
                    .parseObject(new String(nettyPacket.getBody()), ClientToNameNodeRequest.class);
                String fileName = downFileRequest.getFileName();
                checkToken(requestWrapper, downFileRequest);
                DataNodeInfo dataNodeInfo1 = dataNodeManager.getFileDataNode(fileName);
                requestWrapper.sendResponse(ServerResponse.successByData(dataNodeInfo1));
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
            case REPORT_BACKUP_NODE_INFO:
                BackNodeInfo backNodeInfo = JSONObject
                    .parseObject(new String(nettyPacket.getBody()), BackNodeInfo.class);
                this.backNodeInfo = backNodeInfo;
                requestWrapper.sendResponse(nameNodeConfig);
                break;
            default:
                System.out.println("消息类型异常");

        }
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
            System.out.println("启动editLog刷盘任务");
            while (true) {
                try {
                    diskFileSystem.loadOperateLog(operateLogBuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}
