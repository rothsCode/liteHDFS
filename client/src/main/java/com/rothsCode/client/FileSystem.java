package com.rothsCode.client;

import java.io.File;
import java.util.List;

/**
 * @author rothsCode
 * @Description:文件操作api
 * @date 2021/11/11 11:44
 */
public interface FileSystem {

    //创建文件目录
    boolean makeDir(String path);

    //上传文件
    boolean uploadFile(String path, File file);

    //删除文件
    boolean removeFile(String filePath);

    //下载文件
    void downFile(String fileName, String destPath);

    //获取目录下文件名
    List<String> getChildDirs(String filePath);

    //用户登录
    boolean login(String userName, String token);
}
