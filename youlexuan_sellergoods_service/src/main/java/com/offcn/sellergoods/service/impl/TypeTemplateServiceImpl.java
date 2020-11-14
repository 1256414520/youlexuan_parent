package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbSpecificationOptionMapper;
import com.offcn.mapper.TbTypeTemplateMapper;
import com.offcn.pojo.TbSpecificationOption;
import com.offcn.pojo.TbSpecificationOptionExample;
import com.offcn.pojo.TbTypeTemplate;
import com.offcn.pojo.TbTypeTemplateExample;
import com.offcn.pojo.TbTypeTemplateExample.Criteria;
import com.offcn.sellergoods.service.TypeTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;

/**
 * 类型模板服务实现层
 * @author Administrator
 *
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;

	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbTypeTemplate> page=   (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			typeTemplateMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbTypeTemplateExample example=new TbTypeTemplateExample();
		Criteria criteria = example.createCriteria();
		
		if(typeTemplate!=null){			
						if(typeTemplate.getName()!=null && typeTemplate.getName().length()>0){
				criteria.andNameLike("%"+typeTemplate.getName()+"%");
			}			if(typeTemplate.getSpecIds()!=null && typeTemplate.getSpecIds().length()>0){
				criteria.andSpecIdsLike("%"+typeTemplate.getSpecIds()+"%");
			}			if(typeTemplate.getBrandIds()!=null && typeTemplate.getBrandIds().length()>0){
				criteria.andBrandIdsLike("%"+typeTemplate.getBrandIds()+"%");
			}			if(typeTemplate.getCustomAttributeItems()!=null && typeTemplate.getCustomAttributeItems().length()>0){
				criteria.andCustomAttributeItemsLike("%"+typeTemplate.getCustomAttributeItems()+"%");
			}	
		}
		
		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>)typeTemplateMapper.selectByExample(example);
		//调用保存品牌、规格数据到缓存方法
			saveToRedis();
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> findSpecList(Long id) {
		//1、根据模板编号，读取数据库中模板信息
		TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
		//2、获取模板对象包含规格数据 json字符串
		//[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
		String specJson = typeTemplate.getSpecIds();
		System.out.println("规格json:----------------"+specJson);
		//3、解析json字符串为集合
		List<Map> mapList = JSON.parseArray(specJson, Map.class);
//4、判断集合是否为空
		if(mapList!=null&&mapList.size()>0){
			//遍历集合
			for (Map map : mapList) {
				//获取规格编号
				Long specId = new Long((Integer) map.get("id"));
				//创建查询条件，根据规格编号，来查询对应规格选项
				TbSpecificationOptionExample example = new TbSpecificationOptionExample();
				example.createCriteria().andSpecIdEqualTo(specId);
				List<TbSpecificationOption> specificationOptionList = specificationOptionMapper.selectByExample(example);
				//把获取到规格选项集合存放到规格集合
				map.put("options",specificationOptionList);
			}
		}
		//[{"id":27,"text":"网络","options":["3G","4G","5G"]},{"id":32,"text":"机身内存","options":["64","128","256"]}]
		return mapList;
	}

	//保存模板对应品牌和规格数据到redis缓存
	public void saveToRedis(){
		//读取全部的模板数据
		List<TbTypeTemplate> typeTemplateList = findAll();
		//遍历模板数据集合
		for (TbTypeTemplate typeTemplate : typeTemplateList) {
			//提取模板里面品牌数据，存放到缓存
			String brandIds = typeTemplate.getBrandIds();
			List<Map> brandList = JSON.parseArray(brandIds, Map.class);

			redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(),brandList);

			//提起模板里面规格数据，存放到缓存,扩充规格选项
			List<Map> specList = findSpecList(typeTemplate.getId());
			//缓存规格集合数据到redis
			redisTemplate.boundHashOps("specList").put(typeTemplate.getId(),specList);



		}
	}
}
