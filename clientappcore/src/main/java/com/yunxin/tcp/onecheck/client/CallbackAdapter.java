package com.yunxin.tcp.onecheck.client;

public class CallbackAdapter implements Callback{

    @Override
    public void onMsgReturn(String msg) {

    }

    @Override
    public void onException(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onConnectFailedOverTimes(int times) {

    }

    @Override
    public void onConnectFailed(int times) {

    }

    @Override
    public void onMessageReturnFailed(int type) {

    }


}
