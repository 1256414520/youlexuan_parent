package com.offcn.group;

import com.offcn.pojo.TbOrderItem;

import java.io.Serializable;
import java.util.List;

/**
 * 购物车对象组合实体类
 */
public class Cart implements Serializable {

    //商家编号
    private String sellerId;

    //商家名称
    private String sellerName;

    //从该商家购买明细清单
    private List<TbOrderItem> orderItemList;

    public Cart() {
    }

    public Cart(String sellerId, String sellerName, List<TbOrderItem> orderItemList) {
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.orderItemList = orderItemList;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public List<TbOrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<TbOrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }
}
