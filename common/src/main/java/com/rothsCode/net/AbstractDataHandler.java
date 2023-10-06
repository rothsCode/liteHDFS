package com.rothsCode.net;


import com.rothsCode.net.request.NettyPacket;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author rothsCode
 * @Description: server消息处理类
 * @date 2021/10/2816:43
 */
@ChannelHandler.Sharable
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
        //System.out.println("读取消息:" + JSONObject.toJSONString(nettyPacket));
        try {
            boolean handleFlag = handleMsg(nettyPacket, ctx);
            if (!handleFlag) {
                //处理失败交给后续链路处理
                ctx.fireChannelRead(msg);
            }
        } catch (Exception e) {
            log.error("读取错误:{}", e.getMessage());
        }
    }

    public abstract boolean handleMsg(NettyPacket nettyPacket, ChannelHandlerContext ctx);

    public abstract Executor getExecutor();

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        System.out.println("发生错误:" + cause);

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
