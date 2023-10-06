package com.rothsCode.client;

/**
 * @author rothsCode
 * @Description文件上传下载客户端
 * @date 2021/11/11 11:43
 */
public class FileClient {

    public static DefaultFileSystem getFileSystem(ClientConfig clientConfig) {
        DefaultFileSystem defaultFileSystem = new DefaultFileSystem(clientConfig);
        defaultFileSystem.start();
        return defaultFileSystem;
    }


}
