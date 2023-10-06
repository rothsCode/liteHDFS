package com.rothsCode.backup;

import com.rothsCode.net.AbstractDataHandler;
import com.rothsCode.net.request.NettyPacket;
import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.Executor;

/**
 * @author rothsCode
 * @Description:backNode备用节点感知器
 * @date 2021/12/24 18:17
 */
public class AwareHandler extends AbstractDataHandler {

    @Override
    public boolean handleMsg(NettyPacket nettyPacket, ChannelHandlerContext ctx) {
        return false;
    }

    @Override
    public Executor getExecutor() {
        return null;
    }
}
