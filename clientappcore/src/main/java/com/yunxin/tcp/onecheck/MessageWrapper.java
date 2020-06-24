package com.yunxin.tcp.onecheck;


import com.yunxin.tcp.onecheck.client.Callback;

import java.util.Objects;

public class MessageWrapper {
    public String msg;
    public String returnMsg;
    public int retryCount;
    public long timestamp;
    public Callback callback;
    public boolean isDone = false;
    public boolean isDropped = false;
    public boolean isSent = false;

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }

    public boolean isDropped() {
        return isDropped;
    }

    public void setDropped(boolean dropped) {
        isDropped = dropped;
    }

    public String getReturnMsg() {
        return returnMsg;
    }

    public void setReturnMsg(String returnMsg) {
        this.returnMsg = returnMsg;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public MessageWrapper(String msg, int retryCount) {
        this.msg = msg;
        this.retryCount = retryCount;
        this.timestamp = System.currentTimeMillis();
    }

    public MessageWrapper(String msg, int retryCount, long timestamp) {
        this.msg = msg;
        this.retryCount = retryCount;
        this.timestamp = timestamp;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageWrapper that = (MessageWrapper) o;
        return timestamp == that.timestamp &&
                Objects.equals(msg, that.msg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(msg, timestamp);
    }

    @Override
    public String toString() {
        return "MessageWrapper{" +
                "msg='" + msg + '\'' +
                ", returnMsg='" + returnMsg + '\'' +
                ", isDone=" + isDone +
                '}';
    }
}
