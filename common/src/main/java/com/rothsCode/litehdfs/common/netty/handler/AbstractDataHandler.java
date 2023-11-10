package com.rothsCode.litehdfs.common.netty.handler;


import com.rothsCode.litehdfs.common.netty.request.NettyPacket;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author rothsCode
 * @Description: server消息处理类
 * @date 2021/10/2816:43
 */
@Sharable
@Slf4j
public abstract class AbstractDataHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Executor executor = getExecutor();
        if (executor == null) {
            handleReadInternal(ctx, msg);
        } else {
            executor.execute(() -> handleReadInternal(ctx, msg));
        }
        ctx.flush();
    }

    private void handleReadInternal(ChannelHandlerContext ctx, Object msg) {
        NettyPacket nettyPacket = (NettyPacket) msg;
            boolean handleFlag = handleMsg(nettyPacket, ctx);
            if (!handleFlag) {
                ctx.fireChannelRead(msg);
            }

    }

    public abstract boolean handleMsg(NettyPacket nettyPacket, ChannelHandlerContext ctx);

    public abstract Executor getExecutor();

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
        log.error("exceptionCaught:{}", cause);

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
}
