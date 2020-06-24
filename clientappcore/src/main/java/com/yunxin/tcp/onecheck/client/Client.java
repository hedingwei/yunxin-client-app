package com.yunxin.tcp.onecheck.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.yunxin.tcp.onecheck.MessageWrapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Client implements RemovalListener<Integer, MessageWrapper> {

    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private EventLoopGroup loop = new NioEventLoopGroup();

    private Bootstrap bootstrap= null;

    private ChannelFuture channelFuture = null;

    private int retryTimes = 3;

    private int retryCount = 3;

    private long retryInterval = 1500;

    private long readTimeout = 3000;

    private String host;

    private int port;

    private Queue<MessageWrapper> messages = new ArrayDeque<>();
    private MessageWrapper currentMessageWrapper = null;
    private MessageWrapper lastSendMessageWrapper = null;

    private Cache<Integer,MessageWrapper> cache = null;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
        cache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(readTimeout,TimeUnit.MILLISECONDS)
                .removalListener(this)
                .build();

        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                cache.cleanUp();
                if(getCurrentMessageWrapper()==null){
                    if(hasNext()){
                        connect();
                    }
                }else{
                    if(getCurrentMessageWrapper().isDone){
                        if(hasNext()){
                            connect();
                        }
                    }
                }

            }
        },1,200,TimeUnit.MILLISECONDS);

    }

    public void shutdown(){
        try {
            channelFuture.channel().closeFuture().sync();
            loop.shutdownGracefully();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setCurrentMessage(MessageWrapper messageWrapper){
        this.currentMessageWrapper = messageWrapper;
    }

    public MessageWrapper getCurrentMessageWrapper() {
        return currentMessageWrapper;
    }

    public boolean hasNext(){
        cache.cleanUp();
        return !messages.isEmpty();
    }

    public MessageWrapper previewNext(){
        return messages.peek();
    }

    public synchronized void next(){


        if(messages.isEmpty()){
        }else{
            if(currentMessageWrapper==null){
                lastSendMessageWrapper = null;
                setCurrentMessage(messages.peek());
                cache.put(0,getCurrentMessageWrapper());

                assert currentMessageWrapper.equals(messages.peek());

            }else{
                if(currentMessageWrapper.isDone){
                    MessageWrapper tmp = messages.poll();
                    assert  tmp.equals(currentMessageWrapper);
                    lastSendMessageWrapper = tmp;
                    setCurrentMessage(messages.peek());
                    if(getCurrentMessageWrapper()!=null){
                        cache.put(0,getCurrentMessageWrapper());

                    }
                    assert currentMessageWrapper.equals(messages.peek());
                }

            }
        }
    }

    public void connect(){
        createBootstrap(new Bootstrap(), loop);
    }


    public Client send(String msg, Callback callback){
        MessageWrapper messageWrapper = new MessageWrapper(msg,retryTimes);
        messageWrapper.setCallback(callback);
        messages.add(messageWrapper);
        if(bootstrap==null){
            next();
            connect();
        }
        return this;
    }



    public Bootstrap createBootstrap(Bootstrap bootstrap, EventLoopGroup eventLoop ) {
        if (bootstrap != null) {
            final MyInboundHandler handler = new MyInboundHandler(this);
            bootstrap.group(eventLoop);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(handler);
                }
            });
            bootstrap.remoteAddress(new InetSocketAddress(host, port));
            channelFuture = bootstrap.connect().addListener(new ConnectionListener(this));


        }
        this.bootstrap = bootstrap;
        return bootstrap;
    }

    @Override
    public void onRemoval(RemovalNotification<Integer, MessageWrapper> removalNotification) {
        if((getCurrentMessageWrapper()!=null)&&(getCurrentMessageWrapper().isSent)&&(!getCurrentMessageWrapper().isDone)){
            getCurrentMessageWrapper().callback.onMessageReturnFailed(2);
            getCurrentMessageWrapper().setDropped(true);
            messages.poll(); // 丢弃
            channelFuture.channel().close();
        }
    }


    public static class ConnectionListener implements ChannelFutureListener {
        private Client client;

        public ConnectionListener(Client client) {
            this.client = client;
        }
        @Override
        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            if (!channelFuture.isSuccess()) {
                if(client.retryCount>0){
                    if(client.currentMessageWrapper!=null){
                        client.currentMessageWrapper.callback.onConnectFailed(client.retryTimes-client.retryCount+1);
                    }
                    final EventLoop loop = channelFuture.channel().eventLoop();
                    loop.schedule(new Runnable() {
                        @Override
                        public void run() {
                            client.retryCount = client.retryCount - 1;

                            client.connect();
                        }
                    }, client.retryInterval, TimeUnit.MILLISECONDS);
                }else{

                    if(client.hasNext()){
                        client.next();
                    }
                    client.connect();
                    if(client.getCurrentMessageWrapper()!=null){
                        try {
                            client.currentMessageWrapper.callback.onConnectFailedOverTimes(client.retryTimes);
                            client.currentMessageWrapper.setDone(true);
                            client.retryCount = client.retryTimes;
                            client.next();

                        }catch (Throwable t){
                            t.printStackTrace();
                        }
                    }




                }

            }
        }
    }



    public static class MyInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {
        private Client client;
        private Callback callback;
        public MyInboundHandler(Client client) {
            this.client = client;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            if(client.hasNext()){
                client.next();
                if((client.lastSendMessageWrapper!=null)&&(client.currentMessageWrapper!=null)){
                    ctx.writeAndFlush(Unpooled.copiedBuffer(client.currentMessageWrapper.msg, CharsetUtil.UTF_8)).addListener(new GenericFutureListener<Future<? super Void>>() {
                        @Override
                        public void operationComplete(Future<? super Void> future) throws Exception {
                            client.retryCount = client.retryTimes;
                            System.out.println("client send["+future.isSuccess()+"]:"+client.currentMessageWrapper.msg);
                            client.currentMessageWrapper.setSent(true);
                        }
                    });
                }else{

                    if(client.currentMessageWrapper!=null){
                        ctx.writeAndFlush(Unpooled.copiedBuffer(client.currentMessageWrapper.msg, CharsetUtil.UTF_8)).addListener(new GenericFutureListener<Future<? super Void>>() {
                            @Override
                            public void operationComplete(Future<? super Void> future) throws Exception {
                                client.retryCount = client.retryTimes;
                                System.out.println("client send["+future.isSuccess()+"]:"+client.currentMessageWrapper.msg);
                                client.currentMessageWrapper.setSent(true);
                            }
                        });
                    }else{
                        ctx.close();
                    }

                }
            }else{
                ctx.close();
            }
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
            try {
                System.out.println("client received: "+msg.toString(CharsetUtil.UTF_8));
                client.currentMessageWrapper.setReturnMsg(msg.toString(CharsetUtil.UTF_8));
                client.currentMessageWrapper.callback.onMsgReturn(msg.toString(CharsetUtil.UTF_8));
                client.currentMessageWrapper.setDone(true);
            }catch (Throwable t){
                t.printStackTrace();
            }
            client.next();
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {

            if((client.currentMessageWrapper!=null)&&(client.currentMessageWrapper.isSent)&&(!client.currentMessageWrapper.isDone)){
                if(!client.currentMessageWrapper.isDropped){
                    client.currentMessageWrapper.callback.onMessageReturnFailed(1);
                    client.currentMessageWrapper.isDone = true;
                }

            }

            if(client.hasNext()){
                client.connect();
            }

        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            if(client.currentMessageWrapper!=null){
                client.currentMessageWrapper.callback.onException(cause);
            }

        }
    }




}
