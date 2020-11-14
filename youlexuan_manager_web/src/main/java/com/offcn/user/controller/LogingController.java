package com.offcn.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("login")
public class LogingController {


    //获取当前登录用户名方法
    @RequestMapping("/showLoginName")
    public Map showLoginName(){
        //使用springSecurity来获取登录用户名
        String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map=new HashMap();
        map.put("loginName",loginName);
        return map;

    }
}
