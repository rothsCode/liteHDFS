package com.rothsCode.litehdfs.client;

import com.rothsCode.litehdfs.common.netty.response.ServerResponse;
import java.io.File;

/**
 * @author rothsCode
 * @Description:文件操作api
 * @date 2021/11/11 11:44
 */
public interface FileSystemClient {

  //创建文件目录
  ServerResponse makeDir(String path);

  //上传文件
  ServerResponse uploadFile(File file);

  //删除文件
  ServerResponse deleteFile(String filePath);

  //下载文件
  ServerResponse downFile(String fileName, String localDownloadPath);

  //获取目录下文件名
  ServerResponse getChildDirs(String filePath);

  //用户登录
  ServerResponse login(String userName, String token);
}
