package com.rothsCode.litehdfs.datanode;

import com.rothsCode.litehdfs.common.netty.request.FileInfo;
import com.rothsCode.litehdfs.common.netty.vo.DataNodeInfo;
import com.rothsCode.litehdfs.namenode.config.NameNodeConfig;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;


/**
 * @author rothsCode
 * @Description: dataNode注册管理器
 * @date 2021/10/2813:54
 */
@Slf4j
public class DataNodeInfoManager {

    private static final ScheduledExecutorService scheduledExecutorService = Executors
        .newScheduledThreadPool(1);
    //address:dataNodeInfo
    private Map<String, DataNodeInfo> dataNodes = new ConcurrentHashMap<>();
    //address:dataNodeInfo
    private Map<String, DataNodeInfo> expireDataNodes = new ConcurrentHashMap<>();
    //文件对应多个dataNode的存储对应关系，由副本配置数决定
    private Map<String, List<DataNodeInfo>> copyDataNodeByFile = new ConcurrentHashMap<>();

    private NameNodeConfig nameNodeConfig;
    //一个dataNode对应文件信息
    private Map<String, List<FileInfo>> filesByData = new ConcurrentHashMap<>();

    public DataNodeInfoManager(NameNodeConfig nameNodeConfig) {
        this.nameNodeConfig = nameNodeConfig;
        //初始化启动心跳检测线程 TODO 时间轮处理
        scheduledExecutorService
            .scheduleWithFixedDelay(new HeartBeatCheckThread(), 10, 10, TimeUnit.SECONDS);
    }

    public synchronized void addReplicateFile(String hostName, FileInfo fileInfo) {
        DataNodeInfo dataNodeInfo = dataNodes.get(hostName);
        if (dataNodeInfo == null) {
            return;
        }
        dataNodeInfo.addFileSize(fileInfo.getFileSize());
        copyDataNodeByFile.computeIfAbsent(fileInfo.getFileName(), k -> new ArrayList<>())
            .add(dataNodeInfo);
        filesByData.computeIfAbsent(fileInfo.getHostName(), k -> new ArrayList<>()).add(fileInfo);
    }

    /**
     * 默认查询可用容量优先的数据节点 TODO 机架感知
     */
    public List<DataNodeInfo> chooseBestDataNode() {
        List<DataNodeInfo> dataNodeInfos = new ArrayList<>(dataNodes.values());
        if (dataNodeInfos.size() == 0) {
            return Collections.emptyList();
        }
        //处于监控状态的节点
        dataNodeInfos = dataNodeInfos.stream().filter(d -> d.getHealthyStatus() == 1)
            .collect(Collectors.toList());
        int copySize = nameNodeConfig.getCopySize();
        if (copySize > dataNodeInfos.size()) {
            log.error(" no available dataNodeSize");
            return Collections.emptyList();
        }
        // TODO 机架感知
        return dataNodeInfos.subList(0, copySize);
    }


    /**
     * 数据节点注册
     *
     * @param dataNodeInfo
     * @return
     */
    public Boolean register(DataNodeInfo dataNodeInfo) {
        //设置过期时间
        dataNodeInfo
            .setLastHeartTime(System.currentTimeMillis() + nameNodeConfig.getDataNodeRenewalTime());
        dataNodes.put(dataNodeInfo.getAddress(), dataNodeInfo);
        log.debug("dataNode register success:{}", dataNodeInfo.toString());
        return true;
    }

    /**
     * 节点心跳 --启动线程根据过期时间判断是否掉线
     */
    public Boolean heartBeat(String dataNodeAddress) {
        DataNodeInfo dataNodeInfo = dataNodes.get(dataNodeAddress);
        if (dataNodeInfo == null) {
            // 连接断开此时重新加入
            dataNodeInfo = expireDataNodes.get(dataNodeAddress);
            if (dataNodeInfo == null) {
                log.error("heartBeat error not find dataNode");
                return false;
            }
            dataNodes.put(dataNodeAddress, dataNodeInfo);
        }
        //续期
        dataNodeInfo
            .setLastHeartTime(System.currentTimeMillis() + nameNodeConfig.getDataNodeRenewalTime());
        return true;
    }

    public DataNodeInfo getFileDataNode(String fileName) {
        return copyDataNodeByFile.get(fileName).get(0);
    }

    public class HeartBeatCheckThread implements Runnable {
        @Override
        public void run() {
            try {
                //remove expire dataNode
                if (CollectionUtils.isEmpty(dataNodes.values())) {
                    return;
                }
                //连续三次未续期则标识节点失联
                long checkTime =
                    System.currentTimeMillis() - nameNodeConfig.getDataNodeRenewalTime() * 3;
                Collection<DataNodeInfo> dataNodeInfos = dataNodes.values();
                List<DataNodeInfo> expiredNodes = dataNodeInfos.stream()
                    .filter(s -> s.getLastHeartTime() < checkTime)
                    .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(expiredNodes)) {
                    expiredNodes.forEach(e -> {
                        log.info("currTime:{},lastHeartTime:{}", checkTime, e.getLastHeartTime());
                        log.info("expiredNodes:{}", e.getAddress());
                        dataNodes.remove(e.getAddress());
                        expireDataNodes.put(e.getAddress(), e);
                    });
                }
            } catch (Exception e) {
                log.error("HeartBeatCheckThread run error:{}", e);
            }

        }
    }

}
