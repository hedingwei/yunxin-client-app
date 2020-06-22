package com.yunxin.client;

import lombok.Data;

@Data
public class YxPackHeader implements LD4Data{
    int seqNo;
    int appId;
    short version;
    String cmd;
    byte[] bytesData;
    String[] cc;
    long[] dd;
    short[] cd;
    byte[] c1;


    public byte[] toLD4() {

        return new byte[0];
    }





}
