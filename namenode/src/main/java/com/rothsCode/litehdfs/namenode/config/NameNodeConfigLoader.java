package com.rothsCode.litehdfs.namenode.config;

import com.rothsCode.litehdfs.common.util.PropertiesUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * @author roths
 * @Description: nameNode配置加载类
 * @date 2023/8/17 19:48
 */
@Slf4j
public class NameNodeConfigLoader {

  private static final String CONFIG_FILE = "/nameNode.properties";
  private static final NameNodeConfigLoader configLoader = new NameNodeConfigLoader();
  /**
   * 网关配置信息
   */
  private NameNodeConfig nameNodeConfig;

  private NameNodeConfigLoader() {

  }

  public static NameNodeConfigLoader getInstance() {
    return configLoader;
  }

  public NameNodeConfig getNameNodeConfig() {
    return nameNodeConfig;
  }

  public NameNodeConfig loadConfig() {
    //加载配置文件
    InputStream inputStream = NameNodeConfigLoader.class.getResourceAsStream(CONFIG_FILE);
    if (inputStream != null) {
      Properties properties = new Properties();
      try {
        properties.load(inputStream);
        NameNodeConfig nameNodeConfig = new NameNodeConfig();
        PropertiesUtils.properties2Object(properties, nameNodeConfig, "nameNode");
        this.nameNodeConfig = nameNodeConfig;
        return nameNodeConfig;
      } catch (Exception e) {
        log.error("load NameNodeConfig error:{}", e);
      } finally {
        try {
          inputStream.close();
        } catch (IOException e) {
          log.error("load NameNodeConfig close error:{}", e);
        }
      }
    }
    return null;
  }

}
