package com.yunxin.protocol.cmd;

import com.yunxin.utils.Reflection;

import java.util.HashMap;
import java.util.Map;

public class TouchResponseProcessor extends AbstractCmdProcessor{
    @Override
    public void process(Map<String, Object> map) {
        getApp().setConnected(true);

        Map deviceInfo = Reflection.getPcInfo();
        if(deviceInfo!=null){
            getApp().send(buildCommand("deviceInfo",deviceInfo));
        }

    }
}
