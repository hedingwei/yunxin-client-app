package com.yunxin.tcp.client;

public interface ConnectionListener {

    public void onConnected(YcTcpClient client);

    public void onConnectException(YcTcpClient client, Throwable t);

    public void onActive(YcTcpClient client);

    public void onInActive(YcTcpClient client);

    public void onShutdown(YcTcpClient client);

}
