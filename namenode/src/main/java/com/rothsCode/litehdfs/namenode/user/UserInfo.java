package com.rothsCode.litehdfs.namenode.user;

import lombok.Data;

/**
 * @author rothsCode
 * @Description:用户信息
 * @date 2021/12/15 17:25
 */
@Data
public class UserInfo {

  private String userName;
  private String password;
  private long storageLimitCapacity;//用户容量限制额度
  private long storageUserCapacity;//用户已使用容量


}
