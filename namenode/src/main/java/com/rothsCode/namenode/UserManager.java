package com.rothsCode.namenode;

import com.rothsCode.net.DefaultScheduler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author rothsCode
 * @Description: 用户权限管理
 * @date 2021/12/15 17:20
 */
public class UserManager {

  private final static long delayTime = 60000 * 5;//5分钟过期
  private Map<String, UserInfo> userInfoMap = new ConcurrentHashMap<>();
  private List<UserToken> userTokens = new ArrayList<>();//token过期
  private DefaultScheduler defaultScheduler;

  public UserManager(DefaultScheduler defaultScheduler) {
    this.defaultScheduler = defaultScheduler;
    this.defaultScheduler.schedule("定期过期token", this::expireToken, 0, 60, TimeUnit.SECONDS);
    //模拟数据
    UserInfo userInfo = new UserInfo();
    userInfo.setUserName("admin");
    userInfo.setPassword("123");
    userInfoMap.put("admin", userInfo);
    UserInfo userInfo1 = new UserInfo();
    userInfo1.setUserName("abc");
    userInfo1.setPassword("123");
    userInfoMap.put("abc", userInfo1);
  }

  public void expireToken() {
    if (userTokens.size() > 0 && userTokens.get(0).getOverdueTime() > System.currentTimeMillis()) {
      return;
    }
    List<UserToken> removeTokens = new ArrayList<>();
    for (UserToken userToken : userTokens) {
      if (userToken.getOverdueTime() > System.currentTimeMillis()) {
        return;
      }
      removeTokens.add(userToken);
    }
    userTokens.removeAll(removeTokens);
  }


  public String login(String userName, String password) {
    if (Objects.isNull(userName)) {
      return null;
    }
    UserInfo userInfo = userInfoMap.get(userName);
    if (userInfo != null) {
      if (Objects.equals(userInfo.getPassword(), password)) {
        //生成token;
        String token = userName + System.currentTimeMillis();
        Optional<UserToken> userToken1 = userTokens.stream()
            .filter(u -> Objects.equals(u.getUserName(), userInfo.getUserName())).findFirst();
        if (userToken1.isPresent()) {
          userToken1.get().setToken(token);
        } else {
          UserToken userToken = UserToken.builder().userName(userName)
              .token(token).overdueTime(System.currentTimeMillis() + delayTime).build();
          userTokens.add(userToken);
        }
        return token;
      }
    }
    return null;
  }

  public boolean checkToken(String userName, String token) {
    Optional<UserToken> userToken = userTokens.stream().filter(
        e -> Objects.equals(e.getUserName(), userName) && Objects.equals(e.getToken(), token)
            && e.getOverdueTime() > System.currentTimeMillis()).findFirst();
    if (userToken.isPresent()) {
      userToken.get().setOverdueTime(userToken.get().getOverdueTime() + delayTime);//续期
      return true;
    } else {
      return false;
    }
  }

  public boolean loginOut(String userName) {
    Optional<UserToken> userToken = userTokens.stream()
        .filter(e -> Objects.equals(e.getUserName(), userName)).findFirst();
    if (userToken.isPresent()) {
      boolean removeFlag = userTokens.remove(userToken.get());
      return removeFlag;
    }
    return true;
  }
}
