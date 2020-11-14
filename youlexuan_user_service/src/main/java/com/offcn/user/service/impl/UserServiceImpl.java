package com.offcn.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbUserMapper;
import com.offcn.pojo.TbUser;
import com.offcn.pojo.TbUserExample;
import com.offcn.pojo.TbUserExample.Criteria;
import com.offcn.user.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import java.util.Date;
import java.util.List;

/**
 * 用户表服务实现层
 * @author Administrator
 *
 */
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private TbUserMapper userMapper;

	//注入redis操作工具类
	@Autowired
	private RedisTemplate redisTemplate;

	//注入jmsTemplate
	@Autowired
	private JmsTemplate jmsTemplate;

	//接收短信消息队列
	@Autowired
	private Destination smsSendQueue;

	@Value("${sign}")
	private String sign;

	@Value("${template_code}")
	private String template_code;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbUser> findAll() {
		return userMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbUser> page=   (Page<TbUser>) userMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbUser user) {
		//设置创建日期
		user.setCreated(new Date());
		//设置更新日期
		user.setUpdated(new Date());
		//使用MD5的加密器加密明文密码
		String md5Hex = DigestUtils.md5Hex(user.getPassword());
		//设置加密后密码到用户对象
		user.setPassword(md5Hex);
		userMapper.insert(user);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbUser user){
		userMapper.updateByPrimaryKey(user);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbUser findOne(Long id){
		return userMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			userMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbUser user, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbUserExample example=new TbUserExample();
		Criteria criteria = example.createCriteria();
		
		if(user!=null){			
						if(user.getUsername()!=null && user.getUsername().length()>0){
				criteria.andUsernameLike("%"+user.getUsername()+"%");
			}			if(user.getPassword()!=null && user.getPassword().length()>0){
				criteria.andPasswordLike("%"+user.getPassword()+"%");
			}			if(user.getPhone()!=null && user.getPhone().length()>0){
				criteria.andPhoneLike("%"+user.getPhone()+"%");
			}			if(user.getEmail()!=null && user.getEmail().length()>0){
				criteria.andEmailLike("%"+user.getEmail()+"%");
			}			if(user.getSourceType()!=null && user.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+user.getSourceType()+"%");
			}			if(user.getNickName()!=null && user.getNickName().length()>0){
				criteria.andNickNameLike("%"+user.getNickName()+"%");
			}			if(user.getName()!=null && user.getName().length()>0){
				criteria.andNameLike("%"+user.getName()+"%");
			}			if(user.getStatus()!=null && user.getStatus().length()>0){
				criteria.andStatusLike("%"+user.getStatus()+"%");
			}			if(user.getHeadPic()!=null && user.getHeadPic().length()>0){
				criteria.andHeadPicLike("%"+user.getHeadPic()+"%");
			}			if(user.getQq()!=null && user.getQq().length()>0){
				criteria.andQqLike("%"+user.getQq()+"%");
			}			if(user.getIsMobileCheck()!=null && user.getIsMobileCheck().length()>0){
				criteria.andIsMobileCheckLike("%"+user.getIsMobileCheck()+"%");
			}			if(user.getIsEmailCheck()!=null && user.getIsEmailCheck().length()>0){
				criteria.andIsEmailCheckLike("%"+user.getIsEmailCheck()+"%");
			}			if(user.getSex()!=null && user.getSex().length()>0){
				criteria.andSexLike("%"+user.getSex()+"%");
			}	
		}
		
		Page<TbUser> page= (Page<TbUser>)userMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void createSmsCode(String phone) {
		//创建一个随机数 65478.877
	String code=	(long)(Math.random()*1000000)+"";
		System.out.println("验证码:"+code);
		//2、保存验证码到redis缓存
		redisTemplate.boundHashOps("smscode").put(phone,code);
		//3、发送验证码到消息中间件，调用短信发送功能
       jmsTemplate.send(smsSendQueue, new MessageCreator() {
		   @Override
		   public Message createMessage(Session session) throws JMSException {
			   MapMessage mapMessage = session.createMapMessage();

			   //1、接收手机号码
			   mapMessage.setString("mobile",phone);
			   //2、签名
			   mapMessage.setString("sign",sign);
			   //3、模板编号
			   mapMessage.setString("template_code",template_code);
			   //4、模板参数值
			   mapMessage.setString("parm","{\"code\":\""+code+"\"}");
			   return mapMessage;
		   }
	   });

	}

	@Override
	public boolean checkSmsCode(String phone, String smscode) {

		//从redis读取指定手机号的验证码
	String smscodeRedis= (String) redisTemplate.boundHashOps("smscode").get(phone);

	//判断从redis读取到验证码是否为空
		if(smscodeRedis==null){
			return false;
		}

		//比对从redis读取到验证和用户输入的验证码是否一致
		if (!smscode.equals(smscodeRedis)){
			return false;
		}
		return true;
	}
}
