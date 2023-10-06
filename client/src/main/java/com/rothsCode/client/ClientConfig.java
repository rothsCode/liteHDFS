package com.rothsCode.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/11/11 14:13
 */
@Data
@Builder
@AllArgsConstructor
public class ClientConfig {

    private String serverHostName = "127.0.0.1";
    private int serverPort = 9400;
    private String userName;
    private String password;
    private String authToken;
    private int retryTime = 5;
    private long fileBlockSize = 128;//切分文件块大小

    public ClientConfig() {

    }
}
