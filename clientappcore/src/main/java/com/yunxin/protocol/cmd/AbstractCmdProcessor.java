package com.yunxin.protocol.cmd;

import com.yunxin.tcp.app.YxAppImpl;
import com.yunxin.utils.Reflection;

import java.util.HashMap;
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

    public Map<String,Object> buildCommand(String command,Map map){
        Map<String,Object> data = new HashMap<>();
        data.put("command",command);
        data.put("deviceId",Reflection.generateUUIDPerComputer());
        data.put("data", Reflection.string2JSonObject(Reflection.javaObject2JSONString(map)));
        return data;
    }
}
