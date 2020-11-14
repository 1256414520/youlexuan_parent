package com.offcn.page.service;

public interface ItemPageService {

    //传入指定的商品编号，生成静态页面
    public boolean genItemHtml(Long goodsId);

    //删除指定商品编号的静态页面
    public void deltePageHtml(Long[] goodsIds);
}
