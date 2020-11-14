package com.offcn.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailServiceImpl implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       //创建权限集合
        List<GrantedAuthority> list=new ArrayList<>();

        //添加角色
        list.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        list.add(new SimpleGrantedAuthority("ROLE_USER"));
       //spring security不在进行登录认证工作，密码 为空白
        return new User(username,"",list);
    }
}
