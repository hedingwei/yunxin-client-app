package com.yunxin.tcp.onecheck;

import com.alibaba.fastjson.JSONObject;

public class MessageBuilder {

    public static String buildMsg(String cmd, String... params){
        if(params.length%2!=0){
            return null;
        }else{
            JSONObject msg = new JSONObject();
            msg.put("cmd",cmd);
            JSONObject paramsJSONObject = new JSONObject();
            for(int i=0;i<params.length;i = i + 2){
                paramsJSONObject.put(params[i],params[i+1]);
            }
            msg.put("params",paramsJSONObject);
            return msg.toString();
        }
    }



}
