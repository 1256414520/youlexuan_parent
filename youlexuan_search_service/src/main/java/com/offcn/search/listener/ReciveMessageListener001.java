package com.offcn.search.listener;

import com.alibaba.fastjson.JSON;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

@Component
public class ReciveMessageListener001 implements MessageListener {

    //注入搜索引擎服务
    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        if(message instanceof TextMessage){
            TextMessage textMessage= (TextMessage) message;
            try {
                String jsonStr = textMessage.getText();

                //把接收到sku集合json字符串转换为对象
                List<TbItem> itemList = JSON.parseArray(jsonStr, TbItem.class);
                //导入到搜索引擎
                itemSearchService.importList(itemList);

            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
