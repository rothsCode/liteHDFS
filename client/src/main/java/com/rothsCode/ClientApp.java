package com.rothsCode;

import com.rothsCode.client.ClientConfig;
import com.rothsCode.client.FileClient;
import com.rothsCode.client.FileSystem;
import java.io.File;

/**
 * 后续mvc方向
 */
public class ClientApp {

    public static void main(String[] args) {
        ClientConfig clientConfig = new ClientConfig();
        FileSystem fileSystem = FileClient.getFileSystem(clientConfig);
        fileSystem.makeDir("admin/tem/pic");
        File file = new File("D:\\logs\\redis.conf");
        fileSystem.uploadFile("admin/tem/pic", file);
    }
}
