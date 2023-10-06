package com.rothsCode.net;

import java.util.Comparator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DataNodeInfo implements Comparator<DataNodeInfo> {

    private long nodeId;
    private String hostName;
    private int port;
    private int httpPort;
    private long lastHeartTime;
    private Integer healthyStatus;
    private volatile long usedSpaceSize;
    private volatile long remainSpaceSize;

    public synchronized void addFileSize(long fileSize) {
        usedSpaceSize += fileSize;
        remainSpaceSize -= fileSize;
    }

    @Override
    public int compare(DataNodeInfo o1, DataNodeInfo o2) {
        if (o1.getRemainSpaceSize() > o2.getRemainSpaceSize()) {
            return 1;
        } else if (o1.getRemainSpaceSize() < o2.getRemainSpaceSize()) {
            return -1;
        }
        return 0;
    }
}

