package com.yunxin.tcp.onecheck;

import com.yunxin.tcp.onecheck.client.Client;
import com.yunxin.tcp.onecheck.server.SocketServer;

public class OneCheck extends AbstractOneCheck {


    @Override
    protected Client clientProvider() {
        return new Client("127.0.0.1", SocketServer.PORT);
    }

    @Override
    protected int portProvider() {
        return SocketServer.PORT;
    }



    public static void main(String[] args) {

        System.out.println("------PUBLISH VERSION------");
        OneCheck oneCheck = new OneCheck();

        oneCheck.hello();


    }

    @Override
    public void onOneCheckStateOk() {
        super.onOneCheckStateOk();
        System.out.println("pppppp");
    }
}
