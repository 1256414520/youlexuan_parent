package com.offcn.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.content.service.ContentService;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbContentMapper;
import com.offcn.pojo.TbContent;
import com.offcn.pojo.TbContentExample;
import com.offcn.pojo.TbContentExample.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * 内容服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;

	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insert(content);
		//获取新增广告所属分类
        Long categoryId = content.getCategoryId();
        //删除对应广告分类的缓存
        redisTemplate.boundHashOps("content").delete(categoryId);
    }

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
	    //1、获取修改前广告所属分类编号
        Long categoryIdBefore = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
        //删除修改前广告所属分类对应缓存数据
        redisTemplate.boundHashOps("content").delete(categoryIdBefore);
        contentMapper.updateByPrimaryKey(content);

        //判断分类编号是否发生修改
        if(categoryIdBefore.longValue()!=content.getCategoryId().longValue()){
            //分类编号发生修改，移除新分类所属的广告缓存
            redisTemplate.boundHashOps("content").delete(content.getCategoryId());
        }
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
            //根据广告编号，获取广告所属分类编号
            Long categoryId = contentMapper.selectByPrimaryKey(id).getCategoryId();
            //移除广告分类对应缓存数据
            redisTemplate.boundHashOps("content").delete(categoryId);
			contentMapper.deleteByPrimaryKey(id);

        }
	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<TbContent> findByCategoryId(Long categoryId) {
		//从redis读取对应分类id的广告数据
	List<TbContent> contentList= (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);


	//判断当从redis读取的指定分类广告数据为空,继续从数据库读取
		if(contentList==null) {
			TbContentExample example = new TbContentExample();
			Criteria criteria = example.createCriteria();
			//设置查询条件1，分类编号
			criteria.andCategoryIdEqualTo(categoryId);
			//设置查询条件2，广告状态必须是启用
			criteria.andStatusEqualTo("1");
			//设置排序
			example.setOrderByClause("sort_order");

			contentList= contentMapper.selectByExample(example);
			//把从数据库读取到数据，写入到redis缓存
			redisTemplate.boundHashOps("content").put(categoryId,contentList);
		}else {
			System.out.println("从缓存读取到了广告数据");
		}

		return contentList;
	}
}
