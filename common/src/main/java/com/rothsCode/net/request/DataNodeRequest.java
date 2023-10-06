package com.rothsCode.net.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/11/514:46
 */
@Data
@Builder
@AllArgsConstructor
public class DataNodeRequest {

    private int nodeId;
    private String hostName;
    private int port;
    private int httpPort;
    private int healthyStatus;
    private long usedSpaceSize;
    private long remainSpaceSize;
    private List<FileInfo> fileInfos;


}
