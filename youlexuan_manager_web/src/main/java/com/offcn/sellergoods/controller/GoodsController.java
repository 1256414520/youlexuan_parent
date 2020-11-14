package com.offcn.sellergoods.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.group.Goods;
/*import com.offcn.page.service.ItemPageService;*/
import com.offcn.pojo.TbGoods;
import com.offcn.pojo.TbItem;
/*import com.offcn.search.service.ItemSearchService;*/
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Arrays;
import java.util.List;

/**
 * 商品controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

	/*@Reference
	private ItemSearchService itemSearchService;*/

	//引用dubbo，静态页面生成服务
	/*@Reference
	private ItemPageService itemPageService;*/

	//注入jmsTemplate
	@Autowired
	private JmsTemplate jmsTemplate;

	//注入当审核通过后，给搜索服务发送消息的队列
	@Autowired
	private Destination queue;

	//删除队列
	@Autowired
	private Destination solrDeleteQueue;

	//引入静态页面生成主题
	@Autowired
	private Destination topicPageDestination;

	//引入删除静态页面主题
	@Autowired
	private Destination topicPageDeleteDestination;

	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(int page, int rows){
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(final Long [] ids){
		try {
			goodsService.delete(ids);
			//调用搜索服务，删除对应搜索数据
			//itemSearchService.deleteByGoodsIds(Arrays.asList(ids));

			jmsTemplate.send(solrDeleteQueue, new MessageCreator() {
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});

			//发送消息到主题，来删除静态页面
			jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});

			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}

	/***
	 * 审核修改商品状态
	 * @param ids
	 * @param status
	 * @return
	 */
	@RequestMapping("updateStatus")
	public Result updateStatus(final Long[] ids, String status){
		try {
			goodsService.updateStatus(ids, status);
			//判断状态是否为审核通过？
			if(status.equals("1")){
				//根据审核通过商品数组，获取对应sku数据集合
				List<TbItem> itemList = goodsService.findItemListByGoodsIdsAndStatus(ids, status);
				//调用搜索引擎服务，更新搜索引擎
				//itemSearchService.importList(itemList);
				//把要导入搜索引擎的sku数据集合转换为json字符串
				final String jsonString = JSON.toJSONString(itemList);
				//调用jmsTemplate发送消息到中间件
				jmsTemplate.send(queue, new MessageCreator() {
					public Message createMessage(Session session) throws JMSException {
						return session.createTextMessage(jsonString);
					}
				});
				System.out.println("发送更新搜索引擎消息到中间件:"+jsonString);

				//遍历审核通过商品编号数组
				/*for (Long id : ids) {
					//调用静态页面生成服务，生成静态页面
					itemPageService.genItemHtml(id);
				}*/
				//审核通过，发送消息到主题，来生成静态页面
				jmsTemplate.send(topicPageDestination, new MessageCreator() {
					public Message createMessage(Session session) throws JMSException {
						return session.createObjectMessage(ids);
					}
				});

			}
			return new Result(true,"修改状态成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"修改状态失败");
		}
	}

	//单独编写一个生成静态页面的请求方法
/*	@RequestMapping("genItemHtml")
	public String genItemHtml(Long goodsId){
		boolean b = itemPageService.genItemHtml(goodsId);
		return b+"";
	}
	*/
}
