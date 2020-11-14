package com.offcn.seckill.service;

import com.offcn.entity.PageResult;
import com.offcn.pojo.TbSeckillGoods;

import java.util.List;

/**
 * 秒杀商品服务层接口
 * @author Administrator
 *
 */
public interface SeckillGoodsService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeckillGoods> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);


	/**
	 * 增加
	*/
	public void add(TbSeckillGoods seckill_goods);


	/**
	 * 修改
	 */
	public void update(TbSeckillGoods seckill_goods);


	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillGoods findOne(Long id);


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
	public PageResult findPage(TbSeckillGoods seckill_goods, int pageNum, int pageSize);


	//列出当前正在秒杀商品
	public List<TbSeckillGoods> findList();

	//从redis缓存，读取指定秒杀商品信息
	public TbSeckillGoods findOneFromredis(Long id);
}
