package com.offcn.page.service.impl;

import com.offcn.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@Component
public class DeletePageListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {

        if(message instanceof ObjectMessage){
            ObjectMessage objectMessage= (ObjectMessage) message;
            try {
                Long[] ids=(Long[])   objectMessage.getObject();
                //调用页面服务，删除指定编号的商品的静态页面
                itemPageService.deltePageHtml(ids);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
