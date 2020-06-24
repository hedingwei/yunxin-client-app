package com.yunxin.tcp.onecheck.client;

public interface Callback {
    public void onMsgReturn(String msg);
    public void onException(Throwable throwable);
    public void onConnectFailedOverTimes(int times);
    public void onConnectFailed(int times);
    public void onMessageReturnFailed(int type);
}
