package com.rothsCode.datanode;

import lombok.Data;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/11/417:10
 */
@Data
public class DataNodeConfig {

    private String hostName = "0.0.0.0";
    private int port = 8001;//节点绑定端口
    private int retryTime = 5;
    private int httpPort = 8002;//文件下载绑定端口
    private String nameNodeServerAddress = "127.0.0.1:9400";
    private int heartBeatTime = 60000;
    private String storageDir = "D:\\tmp\\liteHDFS\\datanode\\"; //dataNode存储地址
}
