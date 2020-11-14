package com.offcn.page.service.impl;


import com.offcn.mapper.TbGoodsDescMapper;
import com.offcn.mapper.TbGoodsMapper;
import com.offcn.mapper.TbItemCatMapper;
import com.offcn.mapper.TbItemMapper;
import com.offcn.page.service.ItemPageService;
import com.offcn.pojo.TbGoods;
import com.offcn.pojo.TbGoodsDesc;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {

   //注入Freemarker配置项
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    //引入静态页面生成目录
    @Value("${pageDir}")
    private String pageDir;

    //引入读取商品基本信息表数据操作接口
    @Autowired
    private TbGoodsMapper goodsMapper;

    //引入读取商品扩展信息表的数据操作接口
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    
    //引入分类数据操作接口
    @Autowired
    private TbItemCatMapper itemCatMapper;

    //引入sku数据操作接口
    @Autowired
    private TbItemMapper itemMapper;


    @Override
    public boolean genItemHtml(Long goodsId) {
        try {
            //获取模板配置对象
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            //使用配置对象获取模板
            Template template = configuration.getTemplate("item.ftl");
            //准备动态数据
            Map dataModel=new HashMap();
            //1、读取商品基本信息
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
             //把读取到商品基本信息数据封装到动态数据集合
            dataModel.put("goods",goods);

            //2、读取商品扩展信息
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            //把商品扩展信息封装到动态数据集合
            dataModel.put("goodsDesc",goodsDesc);
            
            //3、根据商品一级分类id，读取对应一级分类名称
            String itemCat1  = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            //根据商品二级分类id，读取对应二级分类名称
            String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            //根据商品三级分类id，读取对应三级分类名称
            String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();

            dataModel.put("itemCat1",itemCat1);
            dataModel.put("itemCat2",itemCat2);
            dataModel.put("itemCat3",itemCat3);

            //4、读取商品sku信息
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(goodsId);
            //设置查询条件，商品状态为1
            criteria.andStatusEqualTo("1");
            //设置sku按照是否默认来排序，默认排到第一个
            example.setOrderByClause("is_default desc");

            //查询符合条件sku数据
            List<TbItem> itemList = itemMapper.selectByExample(example);

            //把sku数据封装到数据模型
            dataModel.put("itemList",itemList);

            //创建静态页面输出对象
            FileWriter out = new FileWriter(pageDir + goodsId + ".html");

            //调用模板执行渲染，输出静态页面
            template.process(dataModel,out);

            System.out.println("静态页面生成成功");

            out.close();

            return true;



        } catch (IOException | TemplateException e) {
            e.printStackTrace();
            return false;
        }


    }

    @Override
    public void deltePageHtml(Long[] goodsIds) {
        for (Long goodsId : goodsIds) {
            boolean delete = new File(pageDir + goodsId + ".html").delete();
            System.out.println("静态页面删除："+goodsId+" 结果:"+delete);

        }
    }
}
