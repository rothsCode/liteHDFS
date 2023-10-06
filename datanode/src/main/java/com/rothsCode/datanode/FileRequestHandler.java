package com.rothsCode.datanode;

import com.rothsCode.net.AbstractDataHandler;
import com.rothsCode.net.request.NettyPacket;
import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.Executor;

/**
 * @author rothsCode
 * @Description:文件下载处理类
 * @date 2021/11/8 10:47
 */
public class FileRequestHandler extends AbstractDataHandler {

    @Override
    public boolean handleMsg(NettyPacket nettyPacket, ChannelHandlerContext ctx) {
        return false;
    }

    @Override
    public Executor getExecutor() {
        return null;
    }
}
