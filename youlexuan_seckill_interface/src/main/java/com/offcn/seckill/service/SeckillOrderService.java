package com.offcn.seckill.service;

import com.offcn.entity.PageResult;
import com.offcn.pojo.TbSeckillOrder;

import java.util.List;

/**
 * 秒杀订单服务层接口
 * @author Administrator
 *
 */
public interface SeckillOrderService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeckillOrder> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);


	/**
	 * 增加
	*/
	public void add(TbSeckillOrder seckill_order);


	/**
	 * 修改
	 */
	public void update(TbSeckillOrder seckill_order);


	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillOrder findOne(Long id);


	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbSeckillOrder seckill_order, int pageNum, int pageSize);




	/**
	 * 秒杀下单方法
	 * @param seckillId 秒杀商品编号
	 * @param userId   用户编号
	 */
	public void submitOrder(Long seckillId,String userId);

	//根据用户id，获取对应秒杀订单对象

	public TbSeckillOrder searchOrderFromRedisByUserId(String userId);


	//把秒杀订单从redis缓存保存到数据库中
	public void saveOrderFromRedisToDb(String userId,Long orderId,String transactionId);

	//删除redis中秒杀订单
	public void deleteOrderFromRedis(String userID,Long orderId);
}
