package com.rothsCode.net;

import com.rothsCode.net.request.NettyPacket;
import com.rothsCode.net.request.RequestWrapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

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
    private SyncSendSupport syncSendSupport;
    private Executor executor;

    public DefaultPacketHandler(DefaultScheduler defaultScheduler) {
        syncSendSupport = new SyncSendSupport(defaultScheduler);
        //适用耗时短任务
        executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
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

    @SneakyThrows
    public NettyPacket syncSendMsg(NettyPacket request) {
        if (socketChannel != null && socketChannel.isActive()) {
            return syncSendSupport.sendSync(request);
        }
        return null;
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
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        socketChannel = (SocketChannel) ctx.channel();
        syncSendSupport.setChannel(socketChannel);
        System.out.println("socketChannel is connected");
        handleConnected(true);
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        socketChannel = null;
        syncSendSupport.setChannel(socketChannel);
        System.out.println("socketChannel is lost");
        handleConnected(false);
        ctx.fireChannelInactive();
    }

    @SneakyThrows
    private void handleConnected(Boolean connected) {
        for (ConnectedListener connectedListener : connectedListeners) {
            connectedListener.connectedStatus(connected);
        }
    }

    @Override
    public boolean handleMsg(NettyPacket nettyPacket, ChannelHandlerContext ctx) {
        boolean consumeFlag = syncSendSupport.onResponse(nettyPacket);
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
