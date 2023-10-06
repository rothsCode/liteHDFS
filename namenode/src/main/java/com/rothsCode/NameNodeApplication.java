package com.rothsCode;

import com.rothsCode.datanode.DataNodeManager;
import com.rothsCode.namenode.DiskFileSystem;
import com.rothsCode.namenode.FileDirectoryTree;
import com.rothsCode.namenode.NameNodeApiHandler;
import com.rothsCode.namenode.NameNodeServer;
import com.rothsCode.namenode.UserManager;
import com.rothsCode.net.DefaultScheduler;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * nameNode启动类
 */
public class NameNodeApplication {

    private final DataNodeManager dataNodeManager;
    private final NameNodeApiHandler nameNodeApiHandler;
    private final NameNodeServer nameNodeServer;
    private AtomicBoolean startStatus = new AtomicBoolean(false);

    public NameNodeApplication(NameNodeConfig nameNodeConfig) throws IOException {
        dataNodeManager = new DataNodeManager(nameNodeConfig);
        FileDirectoryTree fileDirectoryTree = new FileDirectoryTree();
        DiskFileSystem diskFileSystem = new DiskFileSystem(nameNodeConfig, fileDirectoryTree);
        FileDirectoryTree diskFileTree = diskFileSystem.loadDiskParseData();//初始化镜像
        if (diskFileTree != null) {
            fileDirectoryTree = diskFileTree;
        }
        DefaultScheduler defaultScheduler = new DefaultScheduler("nameNodeBackupsScheduler");
        UserManager userManager = new UserManager(defaultScheduler);
        nameNodeApiHandler = new NameNodeApiHandler(nameNodeConfig, userManager, diskFileSystem,
            defaultScheduler, dataNodeManager, fileDirectoryTree);
        nameNodeServer = new NameNodeServer(nameNodeApiHandler, nameNodeConfig);

    }

    public static void main(String[] args) throws IOException {
        //解析配置文件
        NameNodeConfig nameNodeConfig = new NameNodeConfig();
        NameNodeApplication nameNodeApplication = new NameNodeApplication(nameNodeConfig);
        Runtime.getRuntime().addShutdownHook(new Thread(nameNodeApplication::shutDown));
        nameNodeApplication.start();
    }

    public void start() {
        if (startStatus.compareAndSet(false, true)) {
            nameNodeServer.start();
        }
    }

    public void shutDown() {
        if (startStatus.compareAndSet(true, false)) {
            nameNodeServer.shutDown();
        }
    }
}
