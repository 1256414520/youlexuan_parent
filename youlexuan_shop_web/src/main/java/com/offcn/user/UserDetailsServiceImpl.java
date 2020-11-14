package com.offcn.user;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.pojo.TbSeller;
import com.offcn.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
public class UserDetailsServiceImpl implements UserDetailsService {

    @Reference
    private SellerService sellerService;


    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //1、授权
        //创建一个权限存储集合
        List<GrantedAuthority> list=new ArrayList<GrantedAuthority>();
//盐
        //向角色、权限集合增加角色
        list.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        list.add(new SimpleGrantedAuthority("ROLE_GUEST"));
        //2、认证
       //根据username获取对应商家信息
        TbSeller seller = sellerService.findOne(username);
        //判断商家对象不为空
        if(seller!=null){
            //判断商家状态是否是 1审核通过
            if(seller.getStatus().equals("1")){
                //创建返回User对象，把用户名和数据库获取到密码，以及权限封装返回
                return new User(username,seller.getPassword(),list);
            }else {
                System.out.println("商家："+username+" 状态不正确:"+seller.getStatus());
            }
        }else {
            return null;
        }

      return null;

    }
}
