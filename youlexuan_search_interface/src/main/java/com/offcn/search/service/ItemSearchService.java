package com.offcn.search.service;

import com.offcn.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    /**
     * sku商品信息搜索方法
     * @param searchMap  封装搜索条件集合
     * @return  搜索结果
     */
    public Map<String,Object> search(Map searchMap);

    //导入指定sku集合数据到搜索引擎
    public void importList(List<TbItem> itemList);

    //删除指定商品搜索引擎记录
    public void deleteByGoodsIds(List goodsIds);
}
