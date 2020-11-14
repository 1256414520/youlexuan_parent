package com.offcn.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Cart;
import com.offcn.mapper.TbOrderItemMapper;
import com.offcn.mapper.TbOrderMapper;
import com.offcn.mapper.TbPayLogMapper;
import com.offcn.order.service.OrderService;
import com.offcn.pojo.TbOrder;
import com.offcn.pojo.TbOrderExample;
import com.offcn.pojo.TbOrderExample.Criteria;
import com.offcn.pojo.TbOrderItem;
import com.offcn.pojo.TbPayLog;
import com.offcn.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 订单服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;

	@Autowired
	private TbOrderItemMapper orderItemMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private TbPayLogMapper payLogMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		//1、从redis读取购物车数据
	List<Cart> cartList= (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());

	//定义一个当前全部订单的总金额
		double total_money=0;
		//定义一个集合，存储全部的订单编号
		List<String> orderIdList=new ArrayList<>();
	  //判断从redis读取的购物车集合数据是否为空
		if(cartList!=null){
			//遍历购物车集合
			for (Cart cart : cartList) {
				//创建订单对象
				TbOrder tbOrder = new TbOrder();

				//获取订单编号
				long orderId = idWorker.nextId();
				System.out.println("订单编号:"+orderId);
				//把订单编号存放到订单集合
				orderIdList.add(orderId+"");
				//关联订单编号到订单对象
				tbOrder.setOrderId(orderId);
				//买家编号
				tbOrder.setUserId(order.getUserId());
				//支付类型
				tbOrder.setPaymentType(order.getPaymentType());
				//订单状态 状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价
				tbOrder.setStatus("1");
				//订单创建日期时间
				tbOrder.setCreateTime(new Date());
				//订单更新时间
				tbOrder.setUpdateTime(new Date());

				//订单收货人
				tbOrder.setReceiver(order.getReceiver());
				//订单收货地址
				tbOrder.setReceiverAreaName(order.getReceiverAreaName());
				//订单收货人电话
				tbOrder.setReceiverMobile(order.getReceiverMobile());

				//订单来源 订单来源：1:app端，2：pc端，3：M端，4：微信端，5：手机qq端
				tbOrder.setSourceType(order.getSourceType());

				//订单的商家编号
				tbOrder.setSellerId(cart.getSellerId());
				//定义一个变量，记录该购物车的总金额
				double money=0.0D;
				//遍历购物明细集合
				for (TbOrderItem orderItem : cart.getOrderItemList()) {

					money+=orderItem.getTotalFee().doubleValue();

					//设置订单详情id编号
					orderItem.setId(idWorker.nextId());

					//设置订单详情关联到订单编号
					orderItem.setOrderId(orderId);
					//设置订单详情的商家编号
					orderItem.setSellerId(cart.getSellerId());

					//保存订单详情到数据库
					orderItemMapper.insert(orderItem);

				}

				//设置订单的实付金额。精确到2位小数;单位:元。如:200.07，表示:200元7分
				tbOrder.setPayment(new BigDecimal(money));
				//累加到全部订单金额
				total_money+=money;

				//保存订单到订单数据表
				orderMapper.insert(tbOrder);



			}

			//判断支付方式，是否是扫码支付，创建保存支付日志
			if(order.getPaymentType().equals("1")){
				//创建支付日志对象
				TbPayLog payLog = new TbPayLog();
				//使用idWorker生成器生成一个支付订单编号
			String outTradeNo=	idWorker.nextId()+"";
			//关联支付订单编号到支付日志对象
				payLog.setOutTradeNo(outTradeNo);
				//设置创建时间
				payLog.setCreateTime(new Date());
				//支付金额：全部订单总金额
				//把订单总金额转换为高精度金额
				BigDecimal bigDecimal_total_money = new BigDecimal(total_money);
				//创建高精度乘数 100
				BigDecimal cs = new BigDecimal(100);
				//做乘法运算，把金额从元转换为分
				BigDecimal bigDecimal_total_money_fen = bigDecimal_total_money.multiply(cs);
				payLog.setTotalFee(bigDecimal_total_money_fen.toBigInteger().longValue());
			    //设置用户编号
				payLog.setUserId(order.getUserId());
				//设置交易状态 0 待付款
				payLog.setTradeState("0");
				//订单编号列表 [ 1001,1002,1003]---> 1001,1002,1003
				String orderListStr = orderIdList.toString().replace("[", "").replace("]", "").replace(" ", "");
				System.out.println("订单列表:"+orderListStr);
				payLog.setOrderList(orderListStr);
				//设置支付类型 1
				payLog.setPayType("1");

				//保存支付日志到数据库
				payLogMapper.insert(payLog);
				//同时支付日志保存到redis一份
				redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);


			}
			//清空redis购物车数据
			redisTemplate.boundHashOps("cartList").delete(order.getUserId());
		}else {
			System.out.println("保存订单时，读取购物车数据失败");
		}

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param
	 * @return
	 */
	@Override
	public TbOrder findOne(Long orderId){
		return orderMapper.selectByPrimaryKey(orderId);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] orderIds) {
		for(Long orderId:orderIds){
			orderMapper.deleteByPrimaryKey(orderId);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public TbPayLog searchPayLogFromRedis(String userId) {
		return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
	}

	@Override
	public void updateOrderStatus(String out_trade_no, String transaction_id) {
		//1、修改支付日志状态，支付成功
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		//修该支付状态 1已支付
		payLog.setTradeState("1");
		//设置付款成功时间
		payLog.setPayTime(new Date());
		//支付宝的交易流水号
		payLog.setTransactionId(transaction_id);
		//更新保存支付日志信息到数据库
		payLogMapper.updateByPrimaryKey(payLog);

		//2、修改支付日志对应订单状态
		String orderListStr = payLog.getOrderList();
		String[] orders = orderListStr.split(",");
		//遍历订单数组
		for (String orderId : orders) {
			//根据订单编号，查询订单对象
			TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
			//修改订单状态 2已经付款
			order.setStatus("2");
			//更新保存订单到数据库
			orderMapper.updateByPrimaryKey(order);
		}

		//3、清除redis中支付日志

        redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
	}
}
