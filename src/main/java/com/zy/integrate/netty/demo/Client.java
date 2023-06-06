package com.zy.integrate.netty.demo;

import com.zy.integrate.handler.demo.ClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author: zhangyong
 * @date 2022年11月04日
 */
public class Client {

    private int port;

    private String ip;

    public Client(int port, String ip) {
        this.port = port;
        this.ip = ip;
    }

    public void run() throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            // 创建客户端 主要就是 Bootstrap 和channel用的不一样了。
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ClientHandler());
                }
            });

            // 启动客户端
            ChannelFuture f = b.connect(ip, port).sync();

            // 等待连接关闭
            f.channel().closeFuture().sync();
        }finally {
            workerGroup.shutdownGracefully();
        }
    }

    public static void startClient(){
        try {
            System.out.println("enter startClient");
            new Client(8988,"127.0.0.1").run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        startClient();
    }
}
