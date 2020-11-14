package com.offcn.pay.service;

import java.util.Map;

public interface AliPayService {



    /**
     * 预下单方法
     * @param out_trade_no  订单编号，是优乐选电商平台内部生成编号
     * @param total_fee     金额：单位 分
     * @return  预习下单返回结果
     */
    public Map createNative(String out_trade_no,String total_fee);

    //查询交易状态
    public Map queryPayStatus(String out_trade_no);
}
