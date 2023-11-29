package com.rothsCode.litehdfs.datanode.config;

import lombok.Data;

/**
 * @author rothsCode
 * @Description: 数据节点配置
 * @date 2021/11/417:10
 */
@Data
public class DataNodeConfig {

    private int serverPort = 8501;

    private int retryTime = 5;
    //文件下载绑定端口
    private int httpPort = 8002;

    private String nameNodeServerAddress = "127.0.0.1:9400";

    private int heartBeatTime = 5000;
    //dataNode存储地址
    private String dataPath;
    //单次传输文件大小 单位byte 1024*1024*4
    private int sendFileBufferSize = 1024 * 1024 * 4;
}
