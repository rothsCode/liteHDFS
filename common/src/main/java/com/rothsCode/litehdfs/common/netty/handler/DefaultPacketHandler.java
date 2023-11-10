package com.rothsCode.litehdfs.common.netty.handler;

import com.rothsCode.litehdfs.common.netty.listener.ConnectedListener;
import com.rothsCode.litehdfs.common.netty.listener.FailConnectedListener;
import com.rothsCode.litehdfs.common.netty.listener.NettyPacketListener;
import com.rothsCode.litehdfs.common.netty.request.NettyPacket;
import com.rothsCode.litehdfs.common.netty.request.RequestWrapper;
import com.rothsCode.litehdfs.common.netty.thread.DefaultScheduler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/11/415:05
 */
@Slf4j
public class DefaultPacketHandler extends AbstractDataHandler {

    private volatile boolean isOtherHandler;
    private volatile SocketChannel socketChannel;
    private List<NettyPacketListener> listeners = new ArrayList<>();
    private List<ConnectedListener> connectedListeners = new ArrayList<>();
    private List<FailConnectedListener> failConnectedListeners = new ArrayList<>();
    private NettySendClient nettySendClient;
    private Executor executor;

    public DefaultPacketHandler(DefaultScheduler defaultScheduler) {
        nettySendClient = new NettySendClient();
        //适用耗时短任务
        executor = new ThreadPoolExecutor(0, 10000, 60, TimeUnit.SECONDS,
            new SynchronousQueue<>());
    }

    public void clearListener() {
        listeners.clear();
        connectedListeners.clear();
        failConnectedListeners.clear();

    }

    public void sendMsg(NettyPacket request) {
        if (socketChannel != null && socketChannel.isActive()) {
            socketChannel.writeAndFlush(request);
        }
    }

    public NettyPacket syncSendMsg(NettyPacket request) {
        if (socketChannel != null && socketChannel.isActive()) {
            return nettySendClient.sendSync(request);
        }
        log.error("socketChannel is null {}:", request.toString());
        return null;
    }

    public CompletableFuture<NettyPacket> asyncSendMsg(NettyPacket request) {
        if (socketChannel != null && socketChannel.isActive()) {
            return nettySendClient.asyncSend(request);
        }
        return null;
    }

    public void asyncSendMsg(NettyPacket request, ChannelFutureListener channelFutureListener) {
        if (socketChannel != null && socketChannel.isActive()) {
            nettySendClient.asyncSend(request, channelFutureListener);
        }
    }

    private SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setOtherHandler(Boolean flag) {
        isOtherHandler = flag;
    }

    public void addLister(NettyPacketListener nettyPacketListener) {
        listeners.add(nettyPacketListener);
    }

    public void addConnectedListener(ConnectedListener connectedListener) {
        connectedListeners.add(connectedListener);
    }

    public void addFailConnectedListener(FailConnectedListener failConnectedListener) {
        failConnectedListeners.add(failConnectedListener);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        socketChannel = (SocketChannel) ctx.channel();
        nettySendClient.setChannel(socketChannel);
        handleConnected(true);
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        socketChannel = null;
        nettySendClient.setChannel(null);
        handleConnected(false);
        ctx.fireChannelInactive();
    }

    private void handleConnected(Boolean connected) {
        if (CollectionUtils.isNotEmpty(connectedListeners)) {
            try {
                for (ConnectedListener connectedListener : connectedListeners) {
                    connectedListener.connectedStatus(connected);
                }
            } catch (Exception e) {
                log.error("connect error:{}", e);
            }
        }
    }

    @Override
    public boolean handleMsg(NettyPacket nettyPacket, ChannelHandlerContext ctx) {
        boolean consumeFlag = nettySendClient.onResponse(nettyPacket);
        RequestWrapper requestWrapper = RequestWrapper.builder().nettyPacket(nettyPacket)
            .ctx(ctx).build();
        for (NettyPacketListener listener : listeners) {
            listener.callBack(requestWrapper);
        }
        return isOtherHandler && consumeFlag;

    }

    @Override
    public Executor getExecutor() {
        return null;
    }
}
