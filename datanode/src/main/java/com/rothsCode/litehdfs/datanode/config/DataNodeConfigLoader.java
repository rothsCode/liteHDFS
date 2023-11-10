package com.rothsCode.litehdfs.datanode.config;

import com.rothsCode.litehdfs.common.util.PropertiesUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * @author roths
 * @Description: dataNode配置加载类
 * @date 2023/8/17 19:48
 */
@Slf4j
public class DataNodeConfigLoader {

  private static final String CONFIG_FILE = "/dataNode.properties";
  private static final DataNodeConfigLoader configLoader = new DataNodeConfigLoader();
  /**
   * dataNode配置信息
   */
  private DataNodeConfig dataNodeConfig;

  private DataNodeConfigLoader() {

  }

  public static DataNodeConfigLoader getInstance() {
    return configLoader;
  }

  public DataNodeConfig getDataNodeConfig() {
    return dataNodeConfig;
  }

  public DataNodeConfig loadConfig(String[] args) {
    //加载配置文件
    InputStream inputStream = DataNodeConfigLoader.class.getResourceAsStream(CONFIG_FILE);
    if (inputStream != null) {
      try {
        Properties properties = new Properties();
        properties.load(inputStream);
        //运行参数解析
        if (args != null && args.length > 0) {
          for (String arg : args) {
            if (arg.startsWith("--") && arg.contains("=")) {
              properties
                  .put(arg.substring(2, arg.indexOf("=")), arg.substring(arg.indexOf("=") + 1));
            }
          }
        }
        DataNodeConfig dataNodeConfig = new DataNodeConfig();
        PropertiesUtils.properties2Object(properties, dataNodeConfig, "dataNode");
        this.dataNodeConfig = dataNodeConfig;
        return dataNodeConfig;
      } catch (Exception e) {
        log.error("load dataNodeConfig  error:{}", e);
      } finally {
        try {
          inputStream.close();
        } catch (IOException e) {
          log.error("dataNodeConfig close error:{}", e);
        }
      }
    }
    return null;
  }

}
