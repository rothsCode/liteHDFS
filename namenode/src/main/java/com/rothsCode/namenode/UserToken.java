package com.rothsCode.namenode;

import java.util.Comparator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author rothsCode
 * @Description:用户token信息
 * @date 2021/12/16 10:14
 */
@Data
@Builder
@AllArgsConstructor
public class UserToken implements Comparator<UserToken> {

  private String userName;
  private String token;
  private long overdueTime;


  @Override
  public int compare(UserToken o1, UserToken o2) {
    if (o1.getOverdueTime() > o2.getOverdueTime()) {
      return 1;
    } else if (o1.getOverdueTime() < o2.getOverdueTime()) {
      return -1;
    }
    return 0;
  }
}
