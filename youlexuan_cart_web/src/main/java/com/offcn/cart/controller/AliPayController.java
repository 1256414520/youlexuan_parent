package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.Result;
import com.offcn.order.service.OrderService;
import com.offcn.pay.service.AliPayService;
import com.offcn.pojo.TbPayLog;
import com.offcn.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("pay")
public class AliPayController {

    //引入dubbo远程支付服务
    @Reference
    private AliPayService aliPayService;

    @Autowired
    private IdWorker idWorker;

    //引入订单服务
    @Reference
    private OrderService orderService;

    //预下单方法
    @RequestMapping("createNative")
    public Map createNative(){
        //1、生成订单编号
       /* long out_trade_no = idWorker.nextId();*/
        //使用Security获取当前登录用户id
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //调用订单服务，根据用户id，获取对应支付日志
        TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
        if(payLog!=null){
            Map map = aliPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee() + "");
            return map;
        }

        return null;
    }

    //查询指定订单编号的交易状态
    @RequestMapping("queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        Result result=null;
        //定义一个变量，记录查询次数
        int x=0;
        while (true){
           //调用查询指定订单编号的订单状态方法
            Map map = aliPayService.queryPayStatus(out_trade_no);

            //1、判断响应结果map，是否为空
            if(map==null){
                //标示查询订单状态服务，失败
               result=new Result(false,"订单查询服务调用失败");
               break;
            }

            //2、map不为空，标示有查询结果
            //2.1 、第一种情况，查询到结果，支付成功
            if(map.get("tradestatus")!=null&&map.get("tradestatus").equals("TRADE_SUCCESS")){
                result =new Result(true,"交易成功");
                //调用订单服务，更新订单状态
                orderService.updateOrderStatus(out_trade_no,(String) map.get("trade_no"));
                break;
            }
            //2.2、第二种情况 ，交易关闭，交易结束
            if(map.get("tradestatus")!=null&&map.get("tradestatus").equals("TRADE_CLOSED")){
                result=new Result(false,"交易关闭，交易结束");
                break;
            }
            //2.3、第三种情况 ，交易结束，不可退款
            if(map.get("tradestatus")!=null&&map.get("tradestatus").equals("TRADE_FINISHED")){
                result=new Result(false,"交易结束，不可退款");


                break;
            }

            //3、让查询等待3秒
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //4、累加查询次数
            ++x;
            //判断查询次数，超过设置数值，结束循环，不在查询
            if(x>100){
                result=new Result(false,"超时");
                break;
            }

        }

        return  result;
    }
}
