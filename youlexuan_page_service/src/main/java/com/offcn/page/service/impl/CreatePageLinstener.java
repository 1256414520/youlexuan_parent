package com.offcn.page.service.impl;

import com.offcn.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@Component
public class CreatePageLinstener implements MessageListener {

    //引入页面生成服务
    @Autowired
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {
        //判断消息类型
        if(message instanceof ObjectMessage){
            ObjectMessage objectMessage= (ObjectMessage) message;
            try {
                Long[] ids=(Long[])   objectMessage.getObject();
                //循环遍历商品编号数组
                for (Long id : ids) {
                    //调用页面生成服务，生成静态页面
                  boolean is=  itemPageService.genItemHtml(id);
                    System.out.println("静态页面生成："+id+" 结果:"+is);
                }

            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
