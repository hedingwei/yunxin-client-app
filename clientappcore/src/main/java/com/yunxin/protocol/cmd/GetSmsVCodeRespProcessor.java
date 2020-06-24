package com.yunxin.protocol.cmd;

import javax.swing.*;
import java.util.Map;

public class GetSmsVCodeRespProcessor extends AbstractCmdProcessor{
    @Override
    public void process(Map<String, Object> map) {
         Object result = map.get("result");
         if(Boolean.TRUE.equals(result)){
             getApp().getLoginWindow().getVCodeInput().setEnabled(true);
             getApp().getLoginWindow().getVCodeButton().setEnabled(false);
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     for(int i=0;i<60;i++){
                         final int finalI = i;
                         SwingUtilities.invokeLater(new Runnable() {
                             @Override
                             public void run() {
                                 getApp().getLoginWindow().getVCodeButton().setText((60- finalI)+"");
                             }
                         });
                         try {
                             Thread.sleep(1000);
                         } catch (InterruptedException e) {
                         }
                     }
                 }
             }).start();
         }else{
             JOptionPane.showMessageDialog(getApp().getLoginWindow(),"非常抱歉，发送验证码失败，请稍后再尝试.");
         }
    }
}
