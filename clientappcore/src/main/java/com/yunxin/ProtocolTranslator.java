package com.yunxin;

import com.alibaba.fastjson.JSON;
import com.yunxin.utils.Factory;
import com.yunxin.utils.Work;

import java.util.Date;

public class ProtocolTranslator implements IProtocolTranslator {
    @Override
    public byte[] toBytes(Object o){
        byte[] data = JSON.toJSONBytes(o);
        return data;
    }

    public static void main(String[] args) {
        IProtocolTranslator protocolTranslator = Factory.buildSafe(ProtocolTranslator.class);
        System.out.println(Work.Bytes.hex(protocolTranslator.toBytes(new Date())));
    }
}
