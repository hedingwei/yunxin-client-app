package com.yunxin.protocol.cmd;

import javax.swing.*;
import java.util.Map;

public class SmsVCodeVerifyRespProcessor extends AbstractCmdProcessor{
    @Override
    public void process(Map<String, Object> map) {
         Object result = map.get("result");
         if(Boolean.TRUE.equals(result)){
             getApp().getLoginWindow().dispose();

             JOptionPane.showMessageDialog(null,"恭喜登陆成功");
         }else{
             JOptionPane.showMessageDialog(getApp().getLoginWindow(),"非常抱歉，发送验证码失败，请稍后再尝试.");
         }
         getApp().getLoginWindow().getVCodeInput().setEnabled(true);
    }
}
