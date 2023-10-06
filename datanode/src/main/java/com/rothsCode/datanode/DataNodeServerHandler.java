package com.rothsCode.datanode;

import com.alibaba.fastjson.JSONObject;
import com.rothsCode.net.AbstractDataHandler;
import com.rothsCode.net.DefaultScheduler;
import com.rothsCode.net.FileTransferType;
import com.rothsCode.net.PacketType;
import com.rothsCode.net.request.ClientTransferFileInfo;
import com.rothsCode.net.request.FileInfo;
import com.rothsCode.net.request.NettyPacket;
import com.rothsCode.net.request.RequestWrapper;
import com.rothsCode.net.response.ServerResponse;
import io.netty.channel.ChannelHandlerContext;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/11/5 16:12
 */
@Slf4j
public class DataNodeServerHandler extends AbstractDataHandler {

    private DataNodeConfig dataNodeConfig;
    private FileCallBackHandler fileCallBackHandler;
    private DataNodeStorageInfo storageInfo;
    //每个文件对应一个文件操作类,文件传输超时定时任务
    private Map<String, FileAppender> transferFileMap = new ConcurrentHashMap<>();
    private Map<String, Long> transferFileTimeMap = new ConcurrentHashMap<>();

    public DataNodeServerHandler(DataNodeStorageInfo storageInfo, DataNodeConfig dataNodeConfig,
        DefaultScheduler defaultScheduler, FileCallBackHandler fileCallBackHandler) {
        this.storageInfo = storageInfo;
        this.dataNodeConfig = dataNodeConfig;
        this.fileCallBackHandler = fileCallBackHandler;
        defaultScheduler
            .schedule("文件传输超时处理任务默认五分钟", () -> handleTransferTimeOut(300), 1, 10, TimeUnit.SECONDS);
    }

    //后续优化为时间轮算法
    public void handleTransferTimeOut(long timeOut) {
        for (Map.Entry<String, Long> entry : transferFileTimeMap.entrySet()) {
            String fileName = entry.getKey();
            Long transferTime = entry.getValue();
            if (System.currentTimeMillis() - transferTime >= timeOut) {
                log.info("文件超时传输任务被移除:{}", fileName);
                FileAppender fileAppender = transferFileMap.remove(fileName);
                if (fileAppender != null) {
                    fileAppender.close();
                }
            }

        }

    }

    @Override
    public boolean handleMsg(NettyPacket nettyPacket, ChannelHandlerContext ctx) {
        System.out.println("dataNode消息体" + nettyPacket);
        int packetValue = nettyPacket.getPackageType();
        PacketType packetType = PacketType.getEnum(packetValue);
        RequestWrapper requestWrapper = new RequestWrapper(nettyPacket.getSequence(), packetValue,
            ctx);
        //处理注册请求
        byte[] body = nettyPacket.getBody();
        ClientTransferFileInfo transferRequest = JSONObject
            .parseObject(new String(body), ClientTransferFileInfo.class);
        switch (packetType) {
            case TRANSFER_FILE:
                if (FileTransferType.HEAD.name().equals(transferRequest.getTransferType())) {
                    //生成写文件客户端
                    String fileName = transferRequest.getFileName();
                    FileAppender fileAppender = new FileAppender(fileCallBackHandler,
                        dataNodeConfig.getStorageDir(), fileName);
                    FileAppender createFileAppender = transferFileMap
                        .putIfAbsent(fileName, fileAppender);
                    if (createFileAppender != null) {
                        requestWrapper
                            .sendResponse(ServerResponse.failByMsg("文件客户端重复生成" + fileName));
                    } else {
                        transferFileTimeMap.put(fileName, System.currentTimeMillis());
                        requestWrapper.sendResponse(ServerResponse.success());
                    }

                } else if (FileTransferType.BODY.name().equals(transferRequest.getTransferType())) {
                    String fileName = transferRequest.getFileName();
                    FileAppender fileAppender = transferFileMap.get(fileName);
                    if (fileAppender == null) {
                        requestWrapper
                            .sendResponse(ServerResponse.failByMsg("文件客户端不存在" + fileName));
                        break;
                    }
                    try {
                        fileAppender.append(transferRequest.getBody());
                        requestWrapper.sendResponse(ServerResponse.success());
                    } catch (Exception e) {
                        requestWrapper.sendResponse(ServerResponse.failByMsg("写入文件失败" + fileName));
                    }

                } else if (FileTransferType.TAIL.name().equals(transferRequest.getTransferType())) {
                    String fileName = transferRequest.getFileName();
                    FileAppender fileAppender = transferFileMap.remove(fileName);
                    if (fileAppender == null) {
                        requestWrapper
                            .sendResponse(ServerResponse.failByMsg("文件客户端不存在文件写入异常" + fileName));
                        break;
                    }
                    try {
                        boolean checkFlag = fileAppender.checkCrc32(transferRequest.getCrc32());
                        if (checkFlag) {
                            requestWrapper.sendResponse(ServerResponse.success());
                        } else {
                            requestWrapper
                                .sendResponse(ServerResponse.failByMsg("文件校验不一致" + fileName));
                        }
                        //落盘后更新data节点文件索引以及向nameNode发送存储成功的消息
                        FileInfo fileInfo = FileInfo.builder()
                            .fileType(transferRequest.getFileType())
                            .createTime(System.currentTimeMillis()).fileName(fileName)
                            .hostName(dataNodeConfig.getHostName()).build();
                        fileAppender.complete(fileInfo);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    } finally {
                        fileAppender.close();
                    }
                }
                break;
            case GET_FILE:
                String fileName = transferRequest.getFileName();
                String absolutePath = storageInfo.getAbsolutePath(fileName);
                if (absolutePath == null) {
                    requestWrapper.sendResponse(ServerResponse.fail());
                    break;
                }
                ClientTransferFileInfo headFile = ClientTransferFileInfo.builder()
                    .fileName(fileName)
                    .transferType(FileTransferType.HEAD.name()).build();
                requestWrapper.sendResponse(headFile);
                File downFile = new File(absolutePath);
                //body传输
                handleFile(requestWrapper, fileName, downFile);
                //tail
                try {
                    ClientTransferFileInfo tailFileInfo = ClientTransferFileInfo.builder()
                        .fileName(fileName).transferType(FileTransferType.TAIL.name())
                        .crc32(FileUtils.checksumCRC32(downFile)).build();
                    requestWrapper.sendResponse(tailFileInfo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                System.out.println("消息类型异常");
        }

        return false;
    }

    @Override
    public Executor getExecutor() {
        return null;
    }

    private void handleFile(RequestWrapper requestWrapper, String fileName, File file) {
        RandomAccessFile rds = null;
        try {
            int hasRead = 0;
            int readLength = 0;
            rds = new RandomAccessFile(file, "r");
            int remainFileLength = (int) rds.length();
            while (remainFileLength > 0) {
                long bodyLength = Math.min(1024 * 1024, remainFileLength);
                byte[] bodyByte = new byte[(int) bodyLength];
                hasRead = rds.read(bodyByte);
                remainFileLength = (remainFileLength - hasRead);
                ClientTransferFileInfo bodyFileInfo = ClientTransferFileInfo.builder()
                    .fileSize(hasRead)
                    .fileName(fileName).transferType(FileTransferType.BODY.name()).body(bodyByte)
                    .build();
                requestWrapper.sendResponse(bodyFileInfo);
                log.info("发送文件内容:{}", fileName);
                readLength += hasRead;
                double progress = (double) readLength / file.length();
                log.info("发送文件内容进度:{}", progress);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rds != null) {
                    rds.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
