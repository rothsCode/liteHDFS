package com.rothsCode;

import com.rothsCode.datanode.DataNodeConfig;
import com.rothsCode.datanode.DataNodeServer;
import com.rothsCode.datanode.DataNodeServerHandler;
import com.rothsCode.datanode.DataNodeStorageInfo;
import com.rothsCode.datanode.FileCallBackHandler;
import com.rothsCode.datanode.NameNodeClient;
import com.rothsCode.datanode.StorageManager;
import com.rothsCode.net.DefaultScheduler;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Hello world!
 */
public class DataNodeApplication {

    private NameNodeClient nameNodeClient;
    private AtomicBoolean startStatus = new AtomicBoolean(false);
    private DataNodeServer dataNodeServer;
    private StorageManager storageManager;
    private DataNodeServerHandler dataNodeServerHandler;
    private DefaultScheduler defaultScheduler;
    private FileCallBackHandler fileCallBackHandler;

    public DataNodeApplication(DataNodeConfig dataNodeConfig) {
        this.defaultScheduler = new DefaultScheduler("FS_dataNodeScheduler");
        this.storageManager = new StorageManager();
        DataNodeStorageInfo dataNodeStorageInfo = storageManager
            .scanFiles(dataNodeConfig.getStorageDir());
        if (dataNodeStorageInfo != null) {
            storageManager.setStorageInfo(dataNodeStorageInfo);
        }
        this.nameNodeClient = new NameNodeClient(dataNodeStorageInfo, dataNodeConfig,
            defaultScheduler);
        fileCallBackHandler = new FileCallBackHandler(storageManager, nameNodeClient);
        dataNodeServerHandler = new DataNodeServerHandler(dataNodeStorageInfo, dataNodeConfig,
            defaultScheduler, fileCallBackHandler);
        dataNodeServer = new DataNodeServer(defaultScheduler, dataNodeConfig, dataNodeServerHandler,
            storageManager);

    }

    public static void main(String[] args) {
        //配置值填充
        DataNodeConfig dataNodeConfig = new DataNodeConfig();
        DataNodeApplication application = new DataNodeApplication(dataNodeConfig);
        //启动nameNodeClient
        application.start();
    }

    public void start() {
        if (startStatus.compareAndSet(false, true)) {
            this.nameNodeClient.start();
            this.dataNodeServer.startServer();
        }

    }

}
