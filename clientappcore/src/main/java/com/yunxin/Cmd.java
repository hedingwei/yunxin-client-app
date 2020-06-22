package com.yunxin;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Cmd {

    
    private int id;

    
    private String msg;

    
    private int type;

    
    private double angle;

    
    private float c;

    
    private boolean flag;

    
    private Boolean ct;

    
    private List<String> strings;

    
    private Map<String, Cmd> cmdMap;


}
