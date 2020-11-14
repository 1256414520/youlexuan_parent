package com.offcn.listener;

import com.aliyuncs.CommonResponse;
import com.offcn.utils.SmsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
@Component
public class SmsSendListener implements MessageListener {

    @Autowired
    private SmsUtil smsUtil;
    public void onMessage(Message message) {
        //判断消息的类型是否是MapMessage
        if(message instanceof MapMessage){
            MapMessage mapMessage= (MapMessage) message;
            //从mapmessage提取短信发送相关信息
            //1、接收方手机号码
            try {
                String mobile = mapMessage.getString("mobile");

                //2、短信签名
                String sign = mapMessage.getString("sign");
                //3、模板编号
                String template_code = mapMessage.getString("template_code");
                //4、模板参数值
                String parm = mapMessage.getString("parm");
                //5、调用短信发送工具类，执行短信发送
                CommonResponse commonResponse = smsUtil.sendSms(mobile, template_code, sign, parm);
                System.out.println("短信发送结果:"+commonResponse.getHttpStatus());
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
