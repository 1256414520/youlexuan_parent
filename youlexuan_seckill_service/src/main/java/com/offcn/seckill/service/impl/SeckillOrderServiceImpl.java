package com.offcn.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbSeckillGoodsMapper;
import com.offcn.mapper.TbSeckillOrderMapper;
import com.offcn.pojo.TbSeckillGoods;
import com.offcn.pojo.TbSeckillOrder;
import com.offcn.pojo.TbSeckillOrderExample;
import com.offcn.pojo.TbSeckillOrderExample.Criteria;
import com.offcn.seckill.service.SeckillOrderService;
import com.offcn.utils.IdWorker;
import com.offcn.utils.RedisLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.List;

/**
 * 秒杀订单服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;

	@Autowired
	private RedisLock redisLock;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void submitOrder(Long seckillId, String userId) {
		String appId="createOrderLock";

		//设置锁的超时时间
		long extime=1*1000L;

		String value=(System.currentTimeMillis()+extime)+"";

		//尝试获取redis分布式锁
		boolean lock = redisLock.lock(appId, value);


		if(lock) {
			//1、从缓存读取对应秒杀商品
			TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
			//2、判断从缓存是否读取到了秒杀商品，如果未读取到
			if (seckillGoods == null) {
				throw new RuntimeException("秒杀商品不存在");
			}
			//3、判断秒杀商品库存是否大于0
			if (seckillGoods.getStockCount() <= 0) {
				throw new RuntimeException("商品库存小于0，已经被秒杀一空");
			}

			//4、幸运用户，秒杀中，把秒杀商品库存减去1件
			seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
			//更新最新库存秒杀对象到redis
			redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);

			//5、创建秒杀订单
			TbSeckillOrder seckillOrder = new TbSeckillOrder();
			//创建订单主键
			long seckillOrderId = idWorker.nextId();
			seckillOrder.setId(seckillOrderId);
			//设置秒杀商品编号
			seckillOrder.setSeckillId(seckillGoods.getId());
			//支付金额 设置秒杀价格
			seckillOrder.setMoney(seckillGoods.getCostPrice());
			//设置用户id
			seckillOrder.setUserId(userId);
			//设置商家编号
			seckillOrder.setSellerId(seckillGoods.getSellerId());
			//订单创建时间
			seckillOrder.setCreateTime(new Date());
			//订单状态 0待付款
			seckillOrder.setStatus("0");
			//保存秒杀订单，保存到redis
			redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);


			//6、处理秒杀商品剩余库存如果等于0，要把该秒杀商品从redis秒杀商品列表中移除
			//同事要更新秒杀商品数据到数据库
			if (seckillGoods.getStockCount() == 0) {
				//更新秒杀商品最新状态到数据库
				seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
				//清除redis缓存中秒杀商品
				redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
			}

			//解锁
			redisLock.unlock(appId,value);
		}
	}

	@Override
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
		return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
	}

	@Override
	public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
		//根据用户编号，读取redis中订单信息
	TbSeckillOrder seckillOrder= (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);

	//判断秒杀订单是否存在
		if(seckillOrder==null){
			throw new RuntimeException("秒杀订单不存在");
		}

		//比对传入订单编号和redis读取到秒杀订单的编号是否一致
		if(seckillOrder.getId().longValue()!=orderId.longValue()){
			throw new RuntimeException("订单编号不一致");
		}

		//设置秒杀订单 支付宝交易流水号
		seckillOrder.setTransactionId(transactionId);
		//设置秒杀订单状态 1 支付完成
		seckillOrder.setStatus("1");
		//支付时间
		seckillOrder.setPayTime(new Date());

		//保存秒杀订单数据到数据库
		seckillOrderMapper.insert(seckillOrder);

		//删除该用户在redis的秒杀订单
		redisTemplate.boundHashOps("seckillOrder").delete(userId);
	}

	@Override
	public void deleteOrderFromRedis(String userID, Long orderId) {
		//1、根据用户编号读取redis中秒杀订单
	TbSeckillOrder seckillOrder= (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userID);

	if(seckillOrder==null){
		throw new RuntimeException("秒杀订单不存在");
	}else {
		//秒杀订单存在，比对订单编号
		if(seckillOrder.getId().longValue()==orderId.longValue()){
			//删除秒杀订单从redis

			redisTemplate.boundHashOps("seckillOrder").delete(userID);

			//从redis读取当前秒杀商品对象
		TbSeckillGoods seckillGoods= (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
		  if(seckillGoods!=null){
		  	seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
		  	//更新最新秒杀商品对象到redis缓存
			  redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(),seckillGoods);
		  }

		}
	}
	}
}
