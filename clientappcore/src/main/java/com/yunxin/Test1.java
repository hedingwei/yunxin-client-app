package com.yunxin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.yunxin.protocol.LoginRequest;
import com.yunxin.tcp.app.YxAppImpl;
import com.yunxin.tcp.client.ConnectionListenerAdapter;
import com.yunxin.tcp.client.MessageListener;
import com.yunxin.tcp.client.YcTcpClient;
import com.yunxin.tcp.client.YxTcpClientException;
import com.yunxin.utils.Reflection;
import com.yunxin.utils.Work;
import com.yunxin.utils.bytes.Pack;
import com.yunxin.utils.ui.Animator;
import com.yunxin.utils.ui.TrayPopupMenu;

import javax.imageio.ImageIO;
import javax.management.relation.Relation;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Type;
import java.net.URL;

public class Test1 {

    public static void main(String[] args) throws Exception, YxTcpClientException {

        YxAppImpl app = new YxAppImpl();

//        File file = new File("/Users/hedingwei/Downloads/test/loading");
//        File[] files = file.listFiles();
//        for(File f: files){
//            if(f.getName().contains("-")){
//                f.renameTo(new File(f.getParent(),f.getName().split("-")[1]));
//            }
//
//        }


    }

}

