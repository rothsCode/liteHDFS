package com.rothsCode.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/11/514:54
 */
public class IPUtil {

  public static final String DEFAULT_IP = "127.0.0.1";

  /**
   * 直接根据第一个网卡地址作为其内网ipv4地址，避免返回 127.0.0.1
   *
   * @return
   */
  public static String getLocalIpByNetcard() {
    try {
      for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
          e.hasMoreElements(); ) {
        NetworkInterface item = e.nextElement();
        for (InterfaceAddress address : item.getInterfaceAddresses()) {
          if (item.isLoopback() || !item.isUp()) {
            continue;
          }
          if (address.getAddress() instanceof Inet4Address) {
            Inet4Address inet4Address = (Inet4Address) address.getAddress();
            return inet4Address.getHostAddress();
          }
        }
      }
      return InetAddress.getLocalHost().getHostAddress();
    } catch (SocketException | UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }

  public static String getLocalIP() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }


}
