package com.rothsCode.backup;


import com.alibaba.fastjson.JSONObject;
import com.rothsCode.NameNodeConfig;
import com.rothsCode.net.DefaultScheduler;
import com.rothsCode.net.NetClient;
import com.rothsCode.net.PacketType;
import com.rothsCode.net.request.NettyPacket;
import com.rothsCode.net.request.RequestWrapper;
import lombok.extern.slf4j.Slf4j;

/**
 * @author rothsCode
 * @Description: 主nameNode客户端
 * @date 2021/11/417:08
 */
@Slf4j
public class NameNodeClient {

  private com.rothsCode.net.NetClient netClient;
  private BackNodeConfig backNodeConfig;
  private DefaultScheduler defaultScheduler;


  public NameNodeClient(BackNodeConfig backNodeConfig, DefaultScheduler defaultScheduler) {
    this.backNodeConfig = backNodeConfig;
    this.defaultScheduler = defaultScheduler;
    this.netClient = new NetClient("backNode-nameNodeClient_",
        backNodeConfig.getActiveNodeServer().split(":")[0],
        Integer.parseInt(backNodeConfig.getActiveNodeServer().split(":")[1]),
        backNodeConfig.getRetryTime(), defaultScheduler);
  }

  /**
   * 启动nameNode客户端
   */
  public void start() {
    netClient.addConnectedLister(connected -> {
      if (connected) {
        //连接成功后发送自身的信息并返回主节点配置
        defaultScheduler.scheduleOnce("上报备用节点信息", () -> reportBackNodeInfo());
      } else {
        log.error("连接失败---");
      }
    });
    netClient.addFailConnectedLister(() -> {
      System.out.println("主节点连接失败主备切换--");
      NodeSwitchController.getInstance().switchNode();
    });
    netClient.startConnect();


  }

  private void reportBackNodeInfo() {

    BackNodeInfo backNodeInfo = BackNodeInfo.builder()
        .hostName(backNodeConfig.getBackNodeServer().split(":")[0])
        .port(Integer.parseInt(backNodeConfig.getActiveNodeServer().split(":")[1])).build();
    NettyPacket nettyPacket = NettyPacket
        .buildPacket(JSONObject.toJSONString(backNodeInfo).getBytes(),
            PacketType.REPORT_BACKUP_NODE_INFO.value);
    NettyPacket response = netClient.sendSync(nettyPacket);
    //获取nameNode配置信息
    if (response != null && response.getBody() != null) {
      NameNodeConfig nameNodeConfig = JSONObject
          .parseObject(new String(response.getBody()), NameNodeConfig.class);
      NodeSwitchController.getInstance().setNameNodeConfig(nameNodeConfig);
    }

  }

  /**
   * 针对nameNode消息监听处理类
   *
   * @param requestWrapper
   */
  private void callBack(RequestWrapper requestWrapper) {

  }


}
