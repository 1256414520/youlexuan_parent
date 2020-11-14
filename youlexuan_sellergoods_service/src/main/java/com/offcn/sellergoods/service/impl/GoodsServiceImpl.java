package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Goods;
import com.offcn.mapper.*;
import com.offcn.pojo.*;
import com.offcn.pojo.TbGoodsExample.Criteria;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 商品服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbSellerMapper sellerMapper;

	@Autowired
	private TbItemMapper itemMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		//设置商品审核状态，0 待审核
		goods.getGoods().setAuditStatus("0");
		//1、保存商品基本信息到数据库
		goodsMapper.insert(goods.getGoods());

		//模拟一个故障
		/*try {
			int x=1/0;
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		//关联商品扩展 信息和基本信息编号
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		//2、 保存商品扩展信息到数据库
		goodsDescMapper.insert(goods.getGoodsDesc());
        //3、调用保存sku集合到数据库的方法
		saveItemList(goods);

	}

	//抽取保存sku集合数据方法为公共方法
	public void saveItemList(Goods goods){

		//判断是否启用规格
		if("1".equals(goods.getGoods().getIsEnableSpec())) {
			//3、保存sku数据
			for (TbItem item : goods.getItemList()) {
				String title = goods.getGoods().getGoodsName();
				//1、设置sku商品标题  商品基本名称+规格选项值
				//{"网络":"移动4G","机身内存":"32G"}
				Map<String, Object> specMap = JSON.parseObject(item.getSpec());
				//获取规格map的所有的key集合
				for (String key : specMap.keySet()) {
					title += " " + specMap.get(key);
				}
				System.out.println("sku商品标题:" + title);
				//设置商品标题到sku对象
				item.setTitle(title);

				//调用设置sku属性方法
				setItemValue(goods,item);

				//10、保存sku数据到数据库
				itemMapper.insert(item);


			}
		}else {
			//未启用规格，处理
			//创建一个sku对象
			TbItem item = new TbItem();
			//设置sku标题，就是商品名称
			item.setTitle(goods.getGoods().getGoodsName());
			//设置价格
			item.setPrice(goods.getGoods().getPrice());
			//设置sku的状态
			item.setStatus("1");
			//设置sku是否默认 必须是默认1
			item.setIsDefault("1");
			//设置默认库存
			item.setNum(9999);
			//设置规格选项
			item.setSpec("{}");

			//调用设置sku属性方法
			setItemValue(goods,item);

			//保存sku数据到数据库
			itemMapper.insert(item);




		}
	}

	/**
	 * 抽取出来公共代码，专门用来设置sku的相关属性
	 * @param goods
	 * @param item
	 */

	private void setItemValue(Goods goods,TbItem item){
		//2、设置spu关联sku编号
		item.setGoodsId(goods.getGoods().getId());
		//3、设置sku的商家编号
		item.setSellerId(goods.getGoods().getSellerId());
		//4、设置sku的所属类目编号（三级分类编号）
		item.setCategoryid(goods.getGoods().getCategory3Id());
		//5、设置时间属性值
		item.setCreateTime(new Date());
		item.setUpdateTime(new Date());
		//6、获取品牌
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		//设置sku的品牌名称属性
		item.setBrand(brand.getName());

		//7、获取分类信息
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());

		//设置sku的分类名称属性
		item.setCategory(itemCat.getName());

		//8、获取商家名称
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		//设置sku的商家名称属性
		item.setSeller(seller.getNickName());

		//9、获取商品图片
		List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
		//判断图片集合不为空
		if (imageList != null && imageList.size() > 0) {
			//提取第一张配图,设置到sku的图片属性
			item.setImage((String) imageList.get(0).get("url"));
		}
	}
	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//1、保存商品基本信息对象
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		//2、保存商品扩展信息
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		//3、删除指定商品编号对应全部sku数据
		TbItemExample example = new TbItemExample();
		example.createCriteria().andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);

		//4、重新保存sku数据到数据库

		saveItemList(goods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		//创建组合商品对象
		Goods goods = new Goods();
		//设置组合对象里面商品基本信息
		goods.setGoods(goodsMapper.selectByPrimaryKey(id));
		//设置组合对象的商品扩展信息
		goods.setGoodsDesc(goodsDescMapper.selectByPrimaryKey(id));
		//sku集合
		TbItemExample example = new TbItemExample();
		example.createCriteria().andGoodsIdEqualTo(id);
		List<TbItem> itemList = itemMapper.selectByExample(example);
		//设置sku集合到商品组合对象
		goods.setItemList(itemList);
		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//goodsMapper.deleteByPrimaryKey(id);
			//根据要删除商品编号，获取数据库商品信息
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			//修改商品逻辑删除属性，设置为已删除
			goods.setIsDelete("1");
			//把商品信息更新保存到数据库
			goodsMapper.updateByPrimaryKey(goods);


		}
		//根据商品编号，读取对应sku集合
		List<TbItem> itemList = findItemListByGoodsIdsAndStatus(ids, "1");

		//遍历sku集合
		for (TbItem item : itemList) {
			//更新sku的状态为 3 删除
			item.setStatus("3");
			//保存sku数据到数据库
			itemMapper.updateByPrimaryKey(item);
		}

	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}	
		}
		//增加逻辑删除排除条件，所有设置为逻辑删除数据不再显示
			criteria.andIsDeleteIsNull();
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateStatus(Long[] ids, String status) {
		for (Long id : ids) {
			//根据商品编号，读取商品信息
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			//修改商品状态
			goods.setAuditStatus(status);
			//更新保存到数据
			goodsMapper.updateByPrimaryKey(goods);

			//2、创建查询条件，根据商品编号，查询sku数据集合
			TbItemExample example = new TbItemExample();
			example.createCriteria().andGoodsIdEqualTo(id);
			List<TbItem> itemList = itemMapper.selectByExample(example);
			//3、遍历sku集合
			for (TbItem item : itemList) {
				//修改sku的状态
				item.setStatus(status);
				//更新保存sku到数据库
				itemMapper.updateByPrimaryKey(item);
			}

		}
	}

	@Override
	public List<TbItem> findItemListByGoodsIdsAndStatus(Long[] goodsIds, String status) {
		TbItemExample example = new TbItemExample();
		example.createCriteria().andGoodsIdIn(Arrays.asList(goodsIds)).andStatusEqualTo(status);


		return itemMapper.selectByExample(example);
	}
}
