package com.yunxin.tcp.client;

import com.yunxin.utils.Reflection;
import com.yunxin.utils.Work;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
public class YcTcpClient extends SimpleChannelInboundHandler<byte[]> implements Runnable{
    private final String host;
    private final int port;

    private  EventLoopGroup group = null;

    private  ChannelFuture connectChannelFeature;

    private  ChannelFuture closeChannelFeature;

    private ConnectionListener connectionListener;

    private MessageListener messageListener;

    private  Thread current = null;

    private Integer connectTimeout = 5;

    public YcTcpClient() {
        this(0);
    }

    public YcTcpClient(int port) {
        this("localhost", port);
    }

    public YcTcpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public synchronized void start() throws YxTcpClientException {

        if(current==null){
            current = new Thread(this);
            current.start();
        }else{
            throw new YxTcpClientException("A existing instance is running");
        }
    }


    public void disconnect() throws YxTcpClientException {
        try {
            connectChannelFeature.channel().disconnect();

        }catch (Throwable t){
            throw new YxTcpClientException(t);
        }
    }


    public void send(Map map) throws YxTcpClientException {
        if(connectChannelFeature.channel().isWritable()){
            try {
                connectChannelFeature.channel().writeAndFlush(Unpooled.copiedBuffer(Work.Packer.YxPack1.packWell(Reflection.javaObject2JSONString(map).getBytes())));
            }catch (Throwable t){
                throw new YxTcpClientException(t);
            }
        }else{
            throw new YxTcpClientException("No Writable");
        }

    }

    public void send(byte... data) throws YxTcpClientException {
        if(connectChannelFeature.channel().isWritable()){
            try {
                connectChannelFeature.channel().writeAndFlush(Unpooled.copiedBuffer(Work.Bytes.ld4Data(data)));
            }catch (Throwable t){
                throw new YxTcpClientException(t);
            }
        }else{
            throw new YxTcpClientException("No Writable");
        }
    }


    @Override
    public void run() {
        try {
            group = new NioEventLoopGroup();
            Bootstrap b = new Bootstrap();
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) (connectTimeout*1000));
            b.group(group) // 注册线程池
                    .channel(NioSocketChannel.class) // 使用NioSocketChannel来作为连接用的channel类
                    .remoteAddress(new InetSocketAddress(this.host, this.port)) // 绑定连接端口和host信息
                    .handler(new ChannelInitializer<SocketChannel>() { // 绑定连接初始化器
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new IdleStateHandler(9, 6, 12, TimeUnit.SECONDS));
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(10*1024*1024,0,4));
                            ch.pipeline().addLast(new ByteArrayDecoder());
                            ch.pipeline().addLast(YcTcpClient.this);
                        }
                    });

            try{
                connectChannelFeature = b.connect().sync();
                if(connectionListener!=null){
                    connectionListener.onConnected(this);
                }
            }catch (Throwable t){
                if(connectionListener!=null){
                    connectionListener.onConnectException(this,t);
                }
            }

            if(connectChannelFeature!=null){
                closeChannelFeature = connectChannelFeature.channel().closeFuture();
                closeChannelFeature.sync();
            }

        } catch (Throwable e) {
            if(connectionListener!=null){
                connectionListener.onConnectException(this,e);
            }
        } finally {
            try {
                group.shutdownGracefully().sync(); // 释放线程池资源
//                System.out.println("shutdown");
            } catch (Throwable e) {
                e.printStackTrace();
            }
            group = null;
            connectChannelFeature = null;
            this.closeChannelFeature = null;
            this.current = null;
            if(connectionListener !=null){
                try{
                    connectionListener.onShutdown(this);
                }catch (Throwable t){
                    t.printStackTrace();
                }
            }
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if(connectionListener!=null){
            connectionListener.onActive(this);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(connectionListener!=null){
            connectionListener.onInActive(this);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        if(messageListener!=null){
            messageListener.onMessage(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
//        System.out.println(evt);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
//        System.out.println(ctx);
    }

    public ConnectionListener getConnectionListener() {
        return connectionListener;
    }

    public void setConnectionListener(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }


    public MessageListener getMessageListener() {
        return messageListener;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isRunning(){
        return this.current!=null;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}