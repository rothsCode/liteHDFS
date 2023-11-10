package com.rothsCode.litehdfs.common.netty.server;

import com.rothsCode.litehdfs.common.netty.handler.BaseChannelInitial;
import com.rothsCode.litehdfs.common.netty.thread.DefaultScheduler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @author rothsCode
 * @Description:nettyserver
 * @date 2021/10/2816:17
 */
@Slf4j
public class NetServer {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;
    private DefaultScheduler defaultScheduler;
    private BaseChannelInitial baseChannelInitial;

    public NetServer(DefaultScheduler defaultScheduler) {
        this.defaultScheduler = defaultScheduler;
        bossGroup = new NioEventLoopGroup(1);
        workGroup = new NioEventLoopGroup();
    }

    public void setBaseChannelInitial(BaseChannelInitial baseChannelInitial) {
        this.baseChannelInitial = baseChannelInitial;
    }

    /**
     * 多端口绑定
     */
    public void bindMultiPort(List<Integer> ports) {
        start(ports);
    }


    /**
     * 多端口绑定
     *
     * @param ports
     */
    public void start(List<Integer> ports) {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(baseChannelInitial);
            List<ChannelFuture> futures = new ArrayList<>();
            for (Integer port : ports) {
                ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
              log.info("nettyServer服务启动:" + port);
              futures.add(channelFuture);
            }
            for (ChannelFuture channelFuture : futures) {
                //阻塞至channel关闭
                channelFuture.channel().closeFuture()
                    .addListener((ChannelFutureListener) future -> future.channel().close());
                channelFuture.channel().closeFuture().sync();
            }

        } catch (InterruptedException e) {
          log.error("nettyServer服务启动异常");
          e.printStackTrace();
        } finally {
          log.info("服务端最终关闭");
          bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    public void shutDown() {
        if (bossGroup != null && workGroup != null) {
          log.info("服务端优雅关闭");
          bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

    }

}
