package com.offcn.sellergoods.service;

import com.offcn.entity.PageResult;
import com.offcn.group.Goods;
import com.offcn.pojo.TbGoods;
import com.offcn.pojo.TbItem;

import java.util.List;

/**
 * 商品服务层接口
 * @author Administrator
 *
 */
public interface GoodsService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbGoods> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);


	/**
	 * 增加
	*/
	public void add(Goods goods);


	/**
	 * 修改
	 */
	public void update(Goods goods);


	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public Goods findOne(Long id);


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
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize);


	//商品状态修改方法
	public void updateStatus(Long[] ids,String status);



	/**
	 * 根据商品编号以及商品状态，读取对应sku数据集合
	 * @param goodsIds  商品编号的数组
	 * @param status    商品状态
	 * @return
	 */
	public List<TbItem> findItemListByGoodsIdsAndStatus(Long[] goodsIds,String status);
	
}
