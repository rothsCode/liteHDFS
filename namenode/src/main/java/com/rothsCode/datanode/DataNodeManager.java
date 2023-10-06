package com.rothsCode.datanode;

import com.rothsCode.NameNodeConfig;
import com.rothsCode.net.DataNodeInfo;
import com.rothsCode.net.request.FileInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;


/**
 * @author rothsCode
 * @Description: dataNode注册管理器
 * @date 2021/10/2813:54
 */
@Slf4j
public class DataNodeManager {

    //hostName:dataNodeInfo
    Map<String, DataNodeInfo> dataNodes = new ConcurrentHashMap<>();
    //文件对应多个dataNode的存储对应关系，由副本配置数决定
    Map<String, List<DataNodeInfo>> copyDataNodeByFile = new ConcurrentHashMap<>();
    //一个dataNode对应文件信息
    Map<String, List<FileInfo>> filesByData = new ConcurrentHashMap<>();
    private NameNodeConfig nameNodeConfig;

    public DataNodeManager(NameNodeConfig nameNodeConfig) {
        this.nameNodeConfig = nameNodeConfig;
        //初始化启动心跳检测线程
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService
            .scheduleWithFixedDelay(new HeartBeatCheckThread(), 30, 10, TimeUnit.SECONDS);
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
     * 默认查询可用容量优先的数据节点
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
            log.error("可用节点不足");
            return Collections.emptyList();
        }

        return dataNodeInfos.subList(0, copySize);
    }


    /**
     * 数据节点注册
     *
     * @param dataNodeInfo
     * @return
     */
    public Boolean register(DataNodeInfo dataNodeInfo) {
        if (dataNodes.containsKey(dataNodeInfo.getHostName())) {
            return false;
        }
        dataNodes.put(dataNodeInfo.getHostName(), dataNodeInfo);
        System.out.println("dataNode注册成功:" + dataNodes);
        return true;
    }

    /**
     * 节点心跳 --启动线程根据过期时间判断是否掉线
     */
    public Boolean heartBeat(String hostName) {
        DataNodeInfo dataNodeInfo = dataNodes.get(hostName);
        if (dataNodeInfo == null) {
            return false;
        }
        dataNodeInfo.setLastHeartTime(System.currentTimeMillis() + 6000);
        System.out.println("dataNode心跳检测:" + dataNodeInfo.getHostName());
        return true;
    }

    public DataNodeInfo getFileDataNode(String fileName) {
        return copyDataNodeByFile.get(fileName).get(0);
    }

    public class HeartBeatCheckThread implements Runnable {

        @Override
        public void run() {
            System.out.println("心跳线程开始启动");
            //时间过期则自动移除
            List<DataNodeInfo> dataNodeInfos = (List<DataNodeInfo>) dataNodes.values();
            List<DataNodeInfo> expiredNodes = dataNodeInfos.stream()
                .filter(s -> s.getLastHeartTime() < System.currentTimeMillis())
                .collect(Collectors.toList());
            expiredNodes.forEach(e -> {
                System.out.println("心跳任务移除过期节点:" + e.getHostName());
                dataNodes.remove(e.getHostName());
            });
        }
    }

}
