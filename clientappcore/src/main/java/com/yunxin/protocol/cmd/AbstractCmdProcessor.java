package com.yunxin.protocol.cmd;

import com.yunxin.tcp.app.YxAppImpl;

import java.util.Map;


public abstract class AbstractCmdProcessor {
    private YxAppImpl app;

    public abstract void process(Map<String,Object> map);

    public YxAppImpl getApp() {
        return app;
    }

    public void setApp(YxAppImpl app) {
        this.app = app;
    }
}
