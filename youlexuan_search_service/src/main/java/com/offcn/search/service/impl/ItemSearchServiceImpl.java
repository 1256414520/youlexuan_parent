package com.offcn.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.pojo.TbBrand;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
@org.springframework.stereotype.Service
public class ItemSearchServiceImpl implements ItemSearchService {

    //引入solr模板操作工具类
    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {
     /*   //1、创建一个简单的查询器对象
        SimpleQuery query = new SimpleQuery("*:*");
        //2、创建查询条件对象
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //3、关联查询条件到查询器对象
        query.addCriteria(criteria);

        //4、发出查询
        ScoredPage<TbItem> scoredPage = solrTemplate.queryForPage(query, TbItem.class);

        System.out.println("查询到的总记录数:"+scoredPage.getTotalElements());

        List<TbItem> content = scoredPage.getContent();*/

     //处理搜索关键字里面如果带空格，去除空格
        if(searchMap.get("keywords")!=null&&searchMap.get("keywords").toString().indexOf(" ")>=0){
            //把搜索关键字的里面空格移除
            String keywords = searchMap.get("keywords").toString().replaceAll(" ", "");
            //重新设置到map
            searchMap.put("keywords",keywords);
        }

        Map<String, Object> map=new HashMap<>();
        Map<String, Object> searchListMap = searchList(searchMap);
        //把查询到结果封装到map
        map.putAll(searchListMap);
        //处理查询条件，获取对应分类集合
        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);

        //判断分类集合不为空，提取第一个分类，
        if(categoryList!=null&&categoryList.size()>0){
            String categoryName = categoryList.get(0);

            //判断查询过滤条件是否存在分类，如果存在，使用查询过滤条件中分类来进行查询对应品牌个规格
            if(searchMap.get("category")!=null&&!searchMap.get("category").equals("")){
                categoryName= (String) searchMap.get("category");
            }
            //根据分类名称读取对应的品牌、规格数据
         map.putAll(searchBrandAndSpec(categoryName));
        }
        return map;
    }

    //定义一个支持高亮查询的方法
    private Map<String, Object> searchList(Map searchMap){
        //1、创建支持高亮查询器对象
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        //2、创建高亮配置选项对象
        HighlightOptions options = new HighlightOptions();
        //配置高亮字段
        options.addField("item_title");
        //设置高亮前缀
        options.setSimplePrefix("<em style='color:red'>");
        //设置高亮后缀
        options.setSimplePostfix("</em>");
        //把高亮选项对象关联到高亮查询器对象
        query.setHighlightOptions(options);
        //3、创建查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //把查询条件关联查询器对象
        query.addCriteria(criteria);


        //过滤条件1：按照商品分类进行过滤

           //判断商品分类查询条件是否存在
            if(searchMap.get("category")!=null&&!searchMap.get("category").equals("")){
                //创建查询条件
                Criteria criteriaCategory = new Criteria("item_category").is(searchMap.get("category"));
                //创建过滤器对象
                SimpleFilterQuery filterQuery = new SimpleFilterQuery(criteriaCategory);
                //把过滤器对象关联到查询器对象
                query.addFilterQuery(filterQuery);
            }
        //过滤条件2：按照品牌进行过滤

           if(searchMap.get("brand")!=null&&!searchMap.get("brand").equals("")){
               Criteria criteriabrand = new Criteria("item_brand").is(searchMap.get("brand"));

               SimpleFilterQuery filterQueryBrand = new SimpleFilterQuery(criteriabrand);
               query.addFilterQuery(filterQueryBrand);

           }

           //过滤条件3：按照商品规格和规格选项执行过滤
        if(searchMap.get("spec")!=null){
            //{"网络":"联通4G","机身内存":"64G"}
            //读取规格和规格选项map集合
         Map map= (Map) searchMap.get("spec");
         //遍历map
            for (Object key : map.keySet()) {
                //转换中文规格名称为拼音
                String keyPinyin = Pinyin.toPinyin(key.toString(), "").toLowerCase();
                System.out.println("keyPinyin："+keyPinyin);
                //创建查询条件对象
                Criteria criteriaSpec = new Criteria("item_spec_" + keyPinyin).is(map.get(key));
                SimpleFilterQuery filterQuerySpec = new SimpleFilterQuery(criteriaSpec);
                //管理过滤器对象到查询器对象
                query.addFilterQuery(filterQuerySpec);
            }
        }

        //过滤条件4：按照价格区间，进行过滤
        if(searchMap.get("price")!=null&&!searchMap.get("price").equals("")){
            //读取价格区间 500-1000\1000-1500
          String priceStr= (String) searchMap.get("price");
          //把价格区间字符串切开成开始价格和结束价格
            String[] prices = priceStr.split("-");

            //判断开始价格，不等于0，就添加过滤条件
            if(prices!=null&&!prices[0].equals("0")) {
                //设置开始价格
                Criteria criteriaBegin = new Criteria("item_price").greaterThan(prices[0]);
                //设置过滤器对象
                SimpleFilterQuery filterQueryBegin = new SimpleFilterQuery(criteriaBegin);
                //关联过滤器对象到查询器对象
                query.addFilterQuery(filterQueryBegin);
            }

            //判断结束价格，不等于*，就添加过滤条件
             if(prices!=null&&!prices[1].equals("*")) {
                 //设置结束价格
                 Criteria criteriaEnd = new Criteria("item_price").lessThanEqual(prices[1]);
                 SimpleFilterQuery filterQueryEnd = new SimpleFilterQuery(criteriaEnd);
                 query.addFilterQuery(filterQueryEnd);
             }
        }

        //获取前端传递过来分页参数

        //1、分页参数1：当前页码
      Integer pageNo= (Integer) searchMap.get("pageNo");
        //判断前端传递的当前页码是否为空
        if(pageNo==null){
            //设置一个默认当前页码 1
            pageNo=1;
        }

        //2、分页参数2：每页显示的记录数
       Integer pageSize= (Integer) searchMap.get("pageSize");
        //判断前端传递的每页显示的记录数是否为空
        if(pageSize==null){
            pageSize=10;
        }

        //设置solr分页相关参数

         //设置游标开始位置
        int start = (pageNo - 1) * pageSize;
        //设置solr的游标开始位置
        query.setOffset(start);
        //设置每页显示的记录数
        query.setRows(pageSize);

        //添加设置排序处理

          //1、接收要排序的字段
        String sortField= (String) searchMap.get("sortField");
        //2、排序方式 升序 ASC、降序 DESC
       String sortValue= (String) searchMap.get("sort");

       //3、判断要排序的字段和排序方式都不为空
        //sortField!=null&&!sortField.equals("")
        if(!StringUtils.isEmpty(sortField)&&!StringUtils.isEmpty(sortValue)){
            //判断排序方式是升序还是降序
            if(sortValue.equals("ASC")){
                //添加升序排序对象
                Sort sort = new Sort(Sort.Direction.ASC, "item_" + sortField);
                //关联排序对象到查询器对象
                query.addSort(sort);
            }else  if(sortValue.equals("DESC")){
                Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
                query.addSort(sort);
            }
        }

        //4、发出查询，支持高亮查询
        HighlightPage<TbItem> highlightPage  = solrTemplate.queryForHighlightPage(query, TbItem.class);

        //5、获取普通查询结果
        List<TbItem> itemList = highlightPage.getContent();
        //6、遍历普通查询结果集合
        for (TbItem item : itemList) {
            //获取针对sku对象的高亮结果集合
            List<HighlightEntry.Highlight> highlightList = highlightPage.getHighlights(item);
            //判断集合不为空
            if(highlightList!=null&&highlightList.size()>0){
                //读取指定字段高亮结果
                List<String> snipplets = highlightList.get(0).getSnipplets();
                System.out.println("高亮标题:"+snipplets.get(0));
                //提取第一高亮结果替换商品标题
                item.setTitle(snipplets.get(0));

            }
        }

        Map<String,Object> map=new HashMap<>();
        //查询到的结果封装到map
        map.put("rows",itemList);

        //把分页相关结果封装返回
        //把符合查询条件的总记录数，返回
        map.put("total",highlightPage.getTotalElements());
        //总页码，封装返回
        map.put("totalPage",highlightPage.getTotalPages());

        return map;
    }

    //新增一个方法，根据查询条件，获取对应分类集合
    private List<String> searchCategoryList(Map searchMap){

        List<String> list=new ArrayList<>();
        //1、创建一个查询器对象
        SimpleQuery query = new SimpleQuery();
        //2、创建查询条件对象
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //关联查询条件到查询器对象
        query.addCriteria(criteria);
        //3、创建一个分组选项对象,设置了要分组的字段名称
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        //分组选项关联到查询器对象
        query.setGroupOptions(groupOptions);
        //4、发出支持分组的查询
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);
        //5、获取分组的集合
        List<GroupEntry<TbItem>> groupEntryList = groupPage.getGroupResult("item_category").getGroupEntries().getContent();
        //遍历分组集合
        for (GroupEntry<TbItem> groupEntry : groupEntryList) {
          //把读取到分组值，存储到分组集合

            list.add(groupEntry.getGroupValue());
        }


        return list;
    }

    //根据分类名称获取对应品牌和规格数据
    private Map searchBrandAndSpec(String category){

        Map map=new HashMap();

        //1、根据分类名称，去缓存查询分类名称对应的模板编号
      Long typeTemplateId= (Long) redisTemplate.boundHashOps("itemCat").get(category);

      //2、判断模板编号存在
        if(typeTemplateId!=null){
            //3、根据模板编号，从redis缓存读取对应品牌数据
         List brandList= (List) redisTemplate.boundHashOps("brandList").get(typeTemplateId);
         //把读取到的品牌集合封装到map
            System.out.println("从内存读取到品牌集合:"+brandList);
            map.put("brandList",brandList);
            //4、根据模板编号，从redis缓存读取对应规格数据
         List specList= (List) redisTemplate.boundHashOps("specList").get(typeTemplateId);
            System.out.println("从内存读取到规格集合:"+specList);
         map.put("specList",specList);
        }else {
            System.out.println("从缓存模板编号读取失败");
        }

        return map;

    }

    @Override
    public void deleteByGoodsIds(List goodsIds) {
        //创建查询器对象
        SimpleQuery query = new SimpleQuery();
        //设置删除条件
        Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
        //把删除条件关联到查询器对象
        query.addCriteria(criteria);
        //执行删除
        solrTemplate.delete(query);
        solrTemplate.commit();
        System.out.println("执行搜索引擎删除操作:"+goodsIds);
    }

    @Override
    public void importList(List<TbItem> itemList) {
        //遍历sku集合
        if(itemList!=null&&itemList.size()>0){
            for (TbItem item : itemList) {
                //读取sku对应规格属性 json
                String specJsonStr = item.getSpec();
                //解析成{"机身内存":"16G","网络":"联通3G"}
                Map<String,String> specMap = JSON.parseObject(specJsonStr, Map.class);
                //创建一个新map，存放key值为拼音
                Map<String,String> specPinyinMap=new HashMap<>();
                //遍历规格map
                for (String key : specMap.keySet()) {
                    specPinyinMap.put(Pinyin.toPinyin(key,"").toLowerCase(),specMap.get(key));
                }

                //设置拼音map到动态域
                item.setSpecMap(specPinyinMap);
            }

            //调用solr操作工具类，保存数据到搜索引擎
            solrTemplate.saveBeans(itemList);
            solrTemplate.commit();

            System.out.println("成功导入数据到搜索引擎:"+itemList);
        }


    }
}
