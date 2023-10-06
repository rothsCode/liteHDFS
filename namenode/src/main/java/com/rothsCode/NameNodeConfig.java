package com.rothsCode;

import lombok.Data;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/11/9 14:15
 */
@Data
public class NameNodeConfig {

    private int serverPort = 9400;
    private int copySize = 1;//副本数量
    private String storageFileTreeDir = "D:\\tmp\\liteHDFS";
}
