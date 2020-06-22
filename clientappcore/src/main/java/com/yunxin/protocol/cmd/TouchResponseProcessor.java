package com.yunxin.protocol.cmd;

import java.util.Map;

public class TouchResponseProcessor extends AbstractCmdProcessor{
    @Override
    public void process(Map<String, Object> map) {
        getApp().setConnected(true);
    }
}
