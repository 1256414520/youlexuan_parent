package com.offcn.util;

import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Solrutil {

    //注入数据库操作接口
    @Autowired
    private TbItemMapper itemMapper;

    //注入solrTemplate
    @Autowired
    private SolrTemplate solrTemplate;

    //定义数据导入方法

    public void importSolr(){
        //创建sku查询条件
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");//设置状态为可用
        //查询sku集合
        List<TbItem> itemList = itemMapper.selectByExample(example);

        //遍历sku集合
        for (TbItem item : itemList) {
            System.out.println("读取到商品标题:"+item.getTitle());

            //读取sku的规格json字符串
            String specjsonStr = item.getSpec();
            //把规格json字符串转换为 map {"机身内存":"16G","网络":"联通3G"}
            Map<String,String> specMap = JSON.parseObject(specjsonStr, Map.class);

            //把规格map关联到sku规格属性
            item.setSpecMap(specMap);

            //创建一个新的map存放key为拼音的规格map
            Map<String,String> specMapPinyin=new HashMap<String, String>();

            //遍历specMap
            for (String key : specMap.keySet()) {
                specMapPinyin.put(Pinyin.toPinyin(key,"").toLowerCase(),specMap.get(key))   ;
            }

            //把拼音map关联到sku
            item.setSpecMap(specMapPinyin);

        }

        //保存集合到solr
        solrTemplate.saveBeans(itemList);
        //提交事务
        solrTemplate.commit();
    }

    public static void main(String[] args) {
      //加载spring环境
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext-*.xml");
        //获取导入solr的工具类
     Solrutil solrutil= (Solrutil) context.getBean("solrutil");

     //调用导入方法
        solrutil.importSolr();

    }
}
