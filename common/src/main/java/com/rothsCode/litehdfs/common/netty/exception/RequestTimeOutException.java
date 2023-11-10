package com.rothsCode.litehdfs.common.netty.exception;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/11/15 14:36
 */
public class RequestTimeOutException extends Exception {

  public RequestTimeOutException(String message) {
    super(message);
  }
}
