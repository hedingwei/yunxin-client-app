package com.yunxin.tcp.client;

public class YxTcpClientException extends Throwable {
    Throwable nested;

    public YxTcpClientException(Throwable nested) {
        this.nested = nested;
    }

    public YxTcpClientException() {
    }

    public YxTcpClientException(String message, Throwable nested) {
        super(message);
        this.nested = nested;
    }

    public YxTcpClientException(String message) {
        super(message);
    }

    @Override
    public void printStackTrace() {
        super.printStackTrace();
        if(this.nested!=null){
            this.nested.printStackTrace();
        }
    }
}
