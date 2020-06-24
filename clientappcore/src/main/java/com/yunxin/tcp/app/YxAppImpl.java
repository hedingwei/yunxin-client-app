package com.yunxin.tcp.app;

import bsh.Interpreter;
import com.alibaba.fastjson.JSON;
import com.yunxin.protocol.cmd.AbstractCmdProcessor;
import com.yunxin.protocol.cmd.GetSmsVCodeRespProcessor;
import com.yunxin.protocol.cmd.SmsVCodeVerifyRespProcessor;
import com.yunxin.protocol.cmd.TouchResponseProcessor;
import com.yunxin.tcp.app.ui.LoginWindow;
import com.yunxin.tcp.client.ConnectionListener;
import com.yunxin.tcp.client.MessageListener;
import com.yunxin.tcp.client.YcTcpClient;
import com.yunxin.tcp.client.YxTcpClientException;
import com.yunxin.utils.Reflection;
import com.yunxin.utils.Work;
import com.yunxin.utils.bytes.UnPack;
import com.yunxin.utils.ui.Animator;
import com.yunxin.utils.ui.TrayPopupMenu;

import javax.imageio.ImageIO;
import javax.management.relation.Relation;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class YxAppImpl implements ConnectionListener, MessageListener {

    TrayPopupMenu pm;

    YcTcpClient yxTcpClient;

    LoginWindow loginWindow;

    BufferedImage connectedIcon;
    BufferedImage disconnectedIcon;
    Animator connectingAnimator;

    IYxServerAddressProvider serverAddressProvider;

    private boolean isConnected = false;

    private String deviceId;


    Interpreter interpreter = new Interpreter();

    JFrame frame = null;

    JMenuItem controlCenter = null;

    JMenuItem loginMenuItem = null;

    static Map<String,Class<? extends AbstractCmdProcessor>> processors = new HashMap<>();
    {
        processors.put("touchResp", TouchResponseProcessor.class);
        processors.put("getSmsVerifyCode", GetSmsVCodeRespProcessor.class);
        processors.put("smsCodeVerify", SmsVCodeVerifyRespProcessor.class);
    }
    public YxAppImpl() {

        try {
            connectedIcon = ImageIO.read(new ByteArrayInputStream(Work.getResource("connected.png")));
            disconnectedIcon = ImageIO.read(new ByteArrayInputStream(Work.getResource("disconnected.png")));
            connectingAnimator = new Animator();
            BufferedImage[] images = new BufferedImage[58];
            for(int i=0;i<58;i++){
                String index = "";
                if(i<10){
                    index = "0"+i;
                }else{
                    index = ""+i;
                }
                images[i]= ImageIO.read(new ByteArrayInputStream(Work.getResource("loading_gray/t"+index+".png"))) ;
            }
            connectingAnimator.setImages(images);
        } catch (IOException e) {
        }


        deviceId = Reflection.generateUUIDPerComputer();
        pm = Work.UI.SystemTray.getPopupMenu();
        Work.UI.SystemTray.setSystemTrayInfo("小微助手",disconnectedIcon,null);
        initMenus();
        Work.UI.SystemTray.rebuild();
        initServerAddressProvider();
        yxTcpClient = new YcTcpClient(serverAddressProvider.getHost(), serverAddressProvider.getPort());
        yxTcpClient.setConnectionListener(this);
        yxTcpClient.setMessageListener(this);
        try {
            playConnectingAnimation();
            yxTcpClient.start();

        } catch (YxTcpClientException e) {
            e.printStackTrace();
        }

    }

    private void playConnectingAnimation(){
        connectingAnimator.setStep(10);
        connectingAnimator.setRepeatTimes(-1);
        Work.UI.SystemTray.systemTrayManager().playIconAnimation(connectingAnimator);
    }

    private void tryTouch(){
        Map map = new HashMap();
        map.put("deviceId",Reflection.generateUUIDPerComputer());
        map.put("command","touch");
        try {
            yxTcpClient.send(map);
        } catch (YxTcpClientException e) {
            e.printStackTrace();
        }
    }

    private void initServerAddressProvider() {
        serverAddressProvider = new IYxServerAddressProvider() {
            @Override
            public String getHost() {
                return "localhost";
            }

            @Override
            public int getPort() {
                return 5500;
            }
        };
//        serverAddressProvider = new IYxServerAddressProvider() {
//            @Override
//            public String getHost() {
//                return "14.63.165.127";
//            }
//
//            @Override
//            public int getPort() {
//                return 443;
//            }
//        };

//        serverAddressProvider = new IYxServerAddressProvider() {
//            @Override
//            public String getHost() {
//                return "120.76.121.210";
//            }
//
//            @Override
//            public int getPort() {
//                return 5500;
//            }
//        };
    }

    @Override
    public void onConnected(YcTcpClient client) {
        tryTouch();
    }

    @Override
    public void onConnectException(YcTcpClient client, Throwable t) {
        setConnected(false);
    }

    @Override
    public void onActive(YcTcpClient client) { }

    @Override
    public void onInActive(YcTcpClient client) { }

    @Override
    public void onShutdown(YcTcpClient client) {

        setConnected(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    playConnectingAnimation();
                    Thread.sleep(5000);
                    yxTcpClient.start();
                } catch (InterruptedException | YxTcpClientException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void initMenus(){
        controlCenter = new JMenuItem("控制台");
        controlCenter.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
//                    Desktop.getDesktop().browse(new URI("http://120.76.121.210"));
                    Desktop.getDesktop().browse(new URI("http://14.63.165.127"));
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (URISyntaxException uriSyntaxException) {
                    uriSyntaxException.printStackTrace();
                }
            }
        });


        loginMenuItem = new JMenuItem("登陆");
        loginMenuItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(loginWindow!=null){
                    try {
                        loginWindow.dispose();
                        loginWindow = null;
                    }catch (Throwable t){
                        t.printStackTrace();
                    }
                }else{
                    loginWindow = new LoginWindow();
                    loginWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                    loginWindow.setYxApp(YxAppImpl.this);
                    loginWindow.setLocationRelativeTo(null);
                    loginWindow.setResizable(false);
                    loginWindow.setVisible(true);
                    loginWindow.setAlwaysOnTop(true);
                }
            }
        });


        pm.add(loginMenuItem);
        pm.add(controlCenter);
        pm.add(new JSeparator());
        pm.add(new JMenuItem("退出"){
            {
                addActionListener(new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.exit(0);
                    }
                });
            }
        });
        Work.UI.SystemTray.rebuild();
    }

    public void onCommand(Map map){
        if(map.containsKey("command")){
            try {
                String command = (String) map.get("command");
                if(processors.containsKey(command)){
                    AbstractCmdProcessor processor =  processors.get(command).newInstance();
                    processor.setApp(this);
                    processor.process(map);
                }
            }catch (Throwable t){
                disconnect();
            }
        }else{
            disconnect();
        }
    }

    public void disconnect(){
        try {
            yxTcpClient.disconnect();
        } catch (YxTcpClientException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(byte[] data) {
        UnPack unPack = new UnPack(data);
        unPack.getInt();//length;
        byte[] b4  = unPack.getBin(4);
        data = unPack.getAll();
        data = Work.Packer.YxPack1.unPack(data,b4);
        if(data==null){
            try {
                yxTcpClient.disconnect();
            } catch (YxTcpClientException e) {
                e.printStackTrace();
            }
        }else{
            data = Work.Compression.GZip.unGZip(data);
            if(data==null){
                try {
                    yxTcpClient.disconnect();
                } catch (YxTcpClientException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    Map<String,Object> map = Reflection.jsonString2JavaObject(new String(data),Map.class);
                    System.out.println(JSON.toJSONString(map));
                    if(map.containsKey("command")){
                        String cmd = (String) map.get("command");
                        onCommand(map);
                        return;
                    }

                }catch (Throwable t){
                    t.printStackTrace();
                    try {
                        yxTcpClient.disconnect();
                    } catch (YxTcpClientException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(final boolean connected) {

        isConnected = connected;
        connectingAnimator.stop(new Runnable() {
            @Override
            public void run() {
                if(connected){
                    Work.UI.SystemTray.systemTrayManager().updateTrayIconImage(connectedIcon);
                }else{
                    Work.UI.SystemTray.systemTrayManager().updateTrayIconImage(disconnectedIcon);
                }
            }
        });


    }

    public void send(Map<String,Object> data){
        try {
            yxTcpClient.send(data);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void send(String command, Map<String,Object> data){
        Map<String, Object> map = new HashMap<>();
        map.put("command",command);
        map.put("data", Reflection.string2JSonObject(Reflection.javaObject2JSONString(data)));
        send(map);
    }
    public String getDeviceId() {
        return deviceId;
    }

    public LoginWindow getLoginWindow() {
        return loginWindow;
    }
}
