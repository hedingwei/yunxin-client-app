package com.yunxin.tcp.onecheck.server;

import com.alibaba.fastjson.JSONObject;
import com.yunxin.tcp.onecheck.MessageBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class HelloServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf inBuffer = (ByteBuf) msg;

        String received = inBuffer.toString(CharsetUtil.UTF_8);

        try {
            System.out.println("Server received: " + received);
            JSONObject object = JSONObject.parseObject(received);
            String cmd = object.getString("cmd");
            JSONObject params = object.getJSONObject("params");

            if(cmd.equals("hello")){
                String id = params.getString("id");
                String ret = "";
                if(id.equals(SocketServer.getInstance().id)){
                    ret = "11";
                }else{
                    ret = "10";
                }
                final String _ret = ret;

                final String retMsg = MessageBuilder.buildMsg("hello_reply","ret",ret);
                ctx.write(Unpooled.copiedBuffer(retMsg, CharsetUtil.UTF_8)).addListener(new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        System.out.println("server send["+future.isSuccess()+"]:"+retMsg);
                    }
                });
            }else if(cmd.equals("shutdown")){
                ctx.write(Unpooled.copiedBuffer(MessageBuilder.buildMsg("shutdown_reply"), CharsetUtil.UTF_8)).addListener(new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        ctx.channel().close();
                        System.exit(0);
                    }
                });
            }else if(cmd.equals("signal")){
                try {
                    String type = params.getString("type");
                    if ("10".equals(type)) {
                        System.out.println("ccccc");
                    }

                    final String retMsg = MessageBuilder.buildMsg("signal_reply","type",type);
                    ctx.write(Unpooled.copiedBuffer(retMsg, CharsetUtil.UTF_8)).addListener(new GenericFutureListener<Future<? super Void>>() {
                        @Override
                        public void operationComplete(Future<? super Void> future) throws Exception {
                            System.out.println("server send["+future.isSuccess()+"]:"+retMsg);
                        }
                    });
                }catch (Throwable t){
                    t.printStackTrace();
                }
            }

        }catch (Throwable t){
            t.printStackTrace();
            ctx.channel().close();

        }

    }



    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("read complete");
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                .addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}