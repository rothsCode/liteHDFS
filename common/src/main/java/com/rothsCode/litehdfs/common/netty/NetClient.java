package com.rothsCode.litehdfs.common.netty;

import com.rothsCode.litehdfs.common.netty.client.ISendClient;
import com.rothsCode.litehdfs.common.netty.handler.BaseChannelInitial;
import com.rothsCode.litehdfs.common.netty.handler.DefaultPacketHandler;
import com.rothsCode.litehdfs.common.netty.listener.ConnectedListener;
import com.rothsCode.litehdfs.common.netty.listener.FailConnectedListener;
import com.rothsCode.litehdfs.common.netty.listener.NettyPacketListener;
import com.rothsCode.litehdfs.common.netty.request.NettyPacket;
import com.rothsCode.litehdfs.common.netty.thread.DefaultScheduler;
import com.rothsCode.litehdfs.common.netty.thread.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

/**
 * @author rothsCode
 * @Description:通信客户端
 * @date 2021/10/2816:18
 */
@Slf4j
public class NetClient implements ISendClient {

    private final String hostName;
    private final int port;
    private final int retryTime;
    private int connectedTime;
    private DefaultPacketHandler defaultPacketHandler;
    private AtomicBoolean startStatus = new AtomicBoolean(false);
    private EventLoopGroup group;
    private BaseChannelInitial baseChannelInitial;
    private String name;
    private AtomicLong sequenceNum = new AtomicLong(1);
    private DefaultScheduler defaultScheduler;

    public NetClient(String name, String hostName, int port, int retryTime,
        DefaultScheduler defaultScheduler) {
        this.defaultScheduler = defaultScheduler;
        this.name = name;
        this.hostName = hostName;
        this.port = port;
        this.retryTime = retryTime;
        this.defaultPacketHandler = new DefaultPacketHandler(defaultScheduler);
        this.baseChannelInitial = new BaseChannelInitial();
        baseChannelInitial.addHandler(defaultPacketHandler);
        this.group = new NioEventLoopGroup(1,
            new NamedThreadFactory("dfsNetClient-Event-", false));
    }

    /**
     * 设置序列号
     */
    public String setSequenceNum() {
        return name + "_" + hostName + "_" + sequenceNum.incrementAndGet();
    }


    /**
     * 不需要返回结果
     *
     * @param nettyPacket
     */
    public void send(NettyPacket nettyPacket) {
        nettyPacket.setSequence(setSequenceNum());
        defaultPacketHandler.sendMsg(nettyPacket);
    }

    /**
     * 同步获取返回结果
     *
     * @param request
     */
    public NettyPacket sendSync(NettyPacket request) {
        request.setSequence(setSequenceNum());
        return defaultPacketHandler.syncSendMsg(request);
    }

    /**
     * 异步获取返回结果
     *
     * @param request
     */
    public CompletableFuture<NettyPacket> asyncSend(NettyPacket request) {
        request.setSequence(setSequenceNum());
        return defaultPacketHandler.asyncSendMsg(request);
    }

    /**
     * 异步调用回调模式
     *
     * @param request
     * @param channelFutureListener
     */
    public void asyncSend(NettyPacket request, ChannelFutureListener channelFutureListener) {
        defaultPacketHandler.asyncSendMsg(request, channelFutureListener);
    }

    public void addPacketLister(NettyPacketListener nettyPacketListener) {
        defaultPacketHandler.addLister(nettyPacketListener);
    }

    public void addConnectedLister(ConnectedListener connectedListener) {
        defaultPacketHandler.addConnectedListener(connectedListener);
    }

    public void addFailConnectedLister(FailConnectedListener failConnectedListener) {
        defaultPacketHandler.addFailConnectedListener(failConnectedListener);
    }

    /**
     * 启动连接
     */
    public void startConnect() {
        //重新开启线程避免连接主线程阻塞
        defaultScheduler.scheduleOnce("连接服务端", () -> {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .channel(NioSocketChannel.class)
                .handler(baseChannelInitial);
            try {
                ChannelFuture channelFuture = bootstrap.connect(hostName, port).sync();
                startStatus.set(true);
                channelFuture.channel().closeFuture()
                    .addListener((ChannelFutureListener) f -> f.channel().close());
                channelFuture.channel().closeFuture().sync();
            } catch (Exception e) {
                log.error("连接失败:{}", e);
            } finally {
                //重试机制
                connectedTime++;
                retryConnect(connectedTime, retryTime);
            }
        }, 0);
    }

    private void retryConnect(int connectedTime, int retryTime) {
        if (!startStatus.get()) {
            return;
        }
        if (connectedTime < retryTime) {
            startConnect();
        } else {
            log.error("连接失败超过重试次数");
            startStatus.set(false);
            group.shutdownGracefully();
        }
    }

    public void shutDown() {
        if (startStatus.get()) {
            startStatus.set(false);
            baseChannelInitial.clearList();
            defaultPacketHandler.clearListener();
            group.shutdownGracefully();
        }
    }


}
