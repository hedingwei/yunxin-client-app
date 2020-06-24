package com.yunxin.tcp.onecheck;

import com.alibaba.fastjson.JSONObject;
import com.yunxin.tcp.onecheck.client.CallbackAdapter;
import com.yunxin.tcp.onecheck.client.Client;
import com.yunxin.tcp.onecheck.server.SocketServer;

public abstract class AbstractOneCheck {


    static {
        try{
            javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
                    new javax.net.ssl.HostnameVerifier(){
                        public boolean verify(String hostname,
                                              javax.net.ssl.SSLSession sslSession) {
                            return true;
                        }
                    });
        }catch (Throwable t){ }



    }


    Client client = null;

    int port;


    abstract protected Client clientProvider();

    protected abstract int portProvider();


    public AbstractOneCheck() {
        client = clientProvider();
        port = portProvider();
    }

    public void sendSignal(int signal){

        client.send(MessageBuilder.buildMsg("signal","type",signal+""),new CallbackAdapter(){
            @Override
            public void onMsgReturn(String msg) {
                System.out.println("signal return: "+msg);
                System.exit(0);
            }

        });
    }


    public void hello() {

        client.send(MessageBuilder.buildMsg("hello", "id", SocketServer.getInstance().id), new CallbackAdapter() {
            @Override
            public void onMsgReturn(String msg) {
                System.out.println("msg: " + msg);
                JSONObject m = JSONObject.parseObject(msg);
                if (m.getString("cmd").equals("hello_reply")) {
                    JSONObject params = m.getJSONObject("params");
                    String ret = params.getString("ret");
                    if (ret.equals("11")) {
                        System.out.println("本地服务已启动，是本进程启动");
                        try {
                            onOneCheckStateOk();
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    } else if (ret.equals("10")) {

                        System.out.println("服务端已经启动，但不是本进程所启动");
//                        System.out.println("发送信号10");
                        sendSignal(10);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                System.out.println("系统退出");
                                System.exit(0);
                            }
                        }).start();
                    } else {
                    }
                }
            }

            @Override
            public void onConnectFailedOverTimes(int times) {

                System.out.println("fff");
            }

            @Override
            public void onConnectFailed(int times) {
                System.out.println("本地服务未启动");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SocketServer.PORT = portProvider();
                        SocketServer.main(null);
                    }
                }).start();

                System.out.println("本地服务状态监控已启动");
            }
        });


    }

    public void shutdown() {
        client.send(MessageBuilder.buildMsg("shutdown"), new CallbackAdapter() {
            @Override
            public void onMsgReturn(String msg) {
                System.out.println("msg: " + msg);
                JSONObject m = JSONObject.parseObject(msg);
                if (m.getString("cmd").equals("shutdown_reply")) {
                    System.out.println("远端服务已经关闭");
                    hello();
                }
            }

            @Override
            public void onConnectFailed(int times) {
                System.out.println("本地服务未启动");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SocketServer.main(null);
                    }
                }).start();

                System.out.println("本地服务状态监控已启动");
            }
        });
    }

    public void onOneCheckStateOk() {



        System.out.println("onOneCheckStateOk");

    }


}
