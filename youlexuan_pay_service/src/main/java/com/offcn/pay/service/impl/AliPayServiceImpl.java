package com.offcn.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeCreateRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.offcn.pay.service.AliPayService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
@Service
public class AliPayServiceImpl implements AliPayService {

    //引入给阿里平台发出请求的客户端对象
    @Autowired
    private AlipayClient alipayClient;
    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        Map map=new HashMap();
        //1、创建一个预下单请求对象
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        //转换金额
        Long total_feeLong = Long.valueOf(total_fee);
        //转换为高精度
        BigDecimal total_fee_fen_bigDecimal = new BigDecimal(total_feeLong);
        //创建高精度除数100，
        BigDecimal bigDecimalCS = new BigDecimal(100);
        //进行高精度除法运算，计算 元
        BigDecimal total_fee_yuan_bigDecimal = total_fee_fen_bigDecimal.divide(bigDecimalCS);

        //2、设置请求参数
        request.setBizContent("{"   +
                "    \"out_trade_no\":\""+out_trade_no+"\","   + //商户订单号
                "    \"total_amount\":\""+total_fee_yuan_bigDecimal.doubleValue()+"\","   +  //预下单金额，单位元
                "    \"subject\":\"优乐选商城测试商品\","   +//商品名称
                "    \"store_id\":\"NJ_001\","   + //店铺编号
                "    \"timeout_express\":\"90m\"}");//超时时间


        //3、发出预下单请求
        try {
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            //4、获取响应状态码
            String code = response.getCode();
            System.out.println("预下单请求响应状态码:"+code);
            //获取全部响应内容
            String body = response.getBody();
            System.out.println("预下单请求全部的响应结果:"+body);

            //判断响应状态等于10000，响应成功
            if(code!=null&&code.equals("10000")){
                //获取响应的预下单二维码地址
                String qrcode = response.getQrCode();
                //把预下单二维码封装到map
                map.put("qrcode",qrcode);
                //把订单编号封装map
                map.put("out_trade_no",response.getOutTradeNo());
                //订单金额 单位：分
                map.put("total_fee",total_fee);
            }else {
                System.out.println("预下单请求失败:"+code);
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public Map queryPayStatus(String out_trade_no) {
        Map map=new HashMap();

        //1、定义一个查询交易状态请求对象
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        //2、设置查询参数
        request.setBizContent("{"   +
                "    \"out_trade_no\":\""+out_trade_no+"\","   +
                "    \"trade_no\":\"\"}");
        //3、发出查询请求
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            //获取状态码
            String code = response.getCode();
            //获取全部响应内容
            String body = response.getBody();
            System.out.println("查询交易状态--code:"+code+" 返回全部结果:"+body);
            //判断状态码是 10000
            if(code!=null&&code.equals("10000")){
                //获取交易状态
                String tradeStatus = response.getTradeStatus();
                //封装交易状态到map
                map.put("tradestatus",tradeStatus);
                //封装交易订单编号
                map.put("out_trade_no",out_trade_no);
                //支付宝提供交易流水号
                map.put("trade_no",response.getTradeNo());
            }else {
                System.out.println("查询交易状态失败:code:"+code);
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        return map;
    }
}
