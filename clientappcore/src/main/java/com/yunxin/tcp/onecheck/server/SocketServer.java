package com.yunxin.tcp.onecheck.server;

import com.yunxin.tcp.onecheck.IServerStateProtocol;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.UUID;

public class SocketServer implements IServerStateProtocol {

    public static SocketServer instance = null;

    public static int PORT = 54719;


    private int state = 0;

    public String id = UUID.randomUUID().toString();

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public static SocketServer getInstance(){
        if(instance==null){
            instance = new SocketServer();
        }
        return instance;
    }

    EventLoopGroup group = new NioEventLoopGroup();
    ServerBootstrap serverBootstrap = null;

    private SocketServer() {

    }

    public void init() {
        try{
            serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(group);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.localAddress(new InetSocketAddress("127.0.0.1", PORT));

            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new HelloServerHandler());
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            channelFuture.channel().closeFuture().sync();
        } catch(Throwable e){
            e.printStackTrace();
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void start(Runnable runnable){ }


    public static void main(String[] args){
        SocketServer socketServer = SocketServer.getInstance();
        socketServer.init();
    }



}
