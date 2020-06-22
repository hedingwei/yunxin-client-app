package com.yunxin;

import com.alibaba.fastjson.JSONObject;
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
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

public class Test {

    public static void main(String[] args) throws Exception, YxTcpClientException {

        final TrayPopupMenu pm = Work.UI.SystemTray.getPopupMenu();
        final BufferedImage bi1 = ImageIO.read(new ByteArrayInputStream(Work.getResource("connected.png")));
        final BufferedImage bi = ImageIO.read(new ByteArrayInputStream(Work.getResource("disconnected.png")));

        final YcTcpClient ec =   new YcTcpClient("127.0.0.1", 5500); // 连接127.0.0.1/65535，并启动

        ec.setConnectionListener(new ConnectionListenerAdapter(){
            @Override
            public void onConnected(YcTcpClient client) {
                Work.UI.SystemTray.systemTrayManager().updateTrayIconImage(bi1);
                JSONObject object = new JSONObject();
                object.put("id", Reflection.generateUUIDPerComputer());
                object.put("cmd","touch");
                byte[] data = Work.Compression.GZip.gZip(object.toJSONString().getBytes());
                byte[] b4 = Work.Bytes.random(4);
                Pack pack = new Pack();
                pack.setBin(b4);
                pack.setBin(Work.Packer.YxPack1.pack(data,b4));
                try {
                    client.send(pack.getAll());
                } catch (YxTcpClientException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onShutdown(YcTcpClient client) {
                super.onShutdown(client);
                Work.UI.SystemTray.systemTrayManager().updateTrayIconImage(bi);
                System.out.println("on shuttttttt");
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            Thread.sleep(3000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        try {
//                            ec.start();
//                        } catch (YxTcpClientException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
            }
        });

        ec.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(byte[] data) {
                System.out.println("recv: "+Work.Bytes.hex(data));
            }
        });





        pm.add(new JMenuItem("连接"){
            {
                addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            ec.start();
                        } catch (YxTcpClientException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }
        });
        pm.add(new JMenuItem("断开连接"){
            {
                addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            ec.disconnect();

                        } catch (YxTcpClientException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }
        });

        final Animator animator = new Animator();

        BufferedImage[] images = new BufferedImage[58];
        for(int i=0;i<58;i++){
            String index = "";
            if(i<10){
                index = "0"+i;
            }else{
                index = ""+i;
            }
           images[i]= ImageIO.read(new ByteArrayInputStream(Work.getResource("loading_gray/t"+index+".png"))) ;
            System.out.println("sss:"+i);
//           images[i-1] = Work.transparent(images[i-1],null);
        }


        animator.setImages(images);



        pm.add(new JMenuItem("播放动画"){
            {
                addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        animator.setStep(10);
                        animator.setRepeatTimes(-1);
                        Work.UI.SystemTray.systemTrayManager().playIconAnimation(animator);
                    }
                });
            }
        });

        pm.add(new JMenuItem("停止动画"){
            {
                addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        animator.stop();
                    }
                });
            }
        });

        Work.UI.SystemTray.setSystemTrayInfo("hh",bi,null);
        Work.UI.SystemTray.rebuild();


    }

}

