package com.rothsCode.net.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/11/15 11:09
 */
@Data
@Builder
@AllArgsConstructor
public class ServerResponse {

  private boolean success;
  private Object data;
  private String errorMsg;
  private long currentTime;

  public static ServerResponse successByData(Object data) {
    return ServerResponse.builder().currentTime(System.currentTimeMillis())
        .data(data).success(true).build();
  }

  public static ServerResponse failByMsg(String errorMsg) {
    ServerResponse serverResponse = ServerResponse.builder().currentTime(System.currentTimeMillis())
        .success(false).errorMsg(errorMsg).build();
    return serverResponse;
  }

  public static ServerResponse success() {
    ServerResponse serverResponse = ServerResponse.builder().currentTime(System.currentTimeMillis())
        .success(true).build();
    return serverResponse;
  }

  public static ServerResponse fail() {
    ServerResponse serverResponse = ServerResponse.builder().currentTime(System.currentTimeMillis())
        .success(false).build();
    return serverResponse;
  }

  public static ServerResponse responseStatus(boolean success) {
    ServerResponse serverResponse = ServerResponse.builder().currentTime(System.currentTimeMillis())
        .success(success).build();
    return serverResponse;
  }

  public boolean getSuccess() {
    return success;
  }


}
