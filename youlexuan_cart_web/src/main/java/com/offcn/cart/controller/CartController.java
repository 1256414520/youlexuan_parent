package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.offcn.cart.service.CartService;
import com.offcn.entity.Result;
import com.offcn.group.Cart;
import com.offcn.utils.CookieUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("cart")
public class CartController {

    @Reference
    private CartService cartService;


    //1、读取本地浏览器的Cookie，显示购物车数据
    @RequestMapping("findCartList")
    public List<Cart> findCartList(HttpServletRequest request, HttpServletResponse response){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录用户:"+name);
        List<Cart> cartList_redis=new ArrayList<>();

        List<Cart> cartList_Cookie=new ArrayList<>();

        //1、借助于Cookie工具类，读取本地浏览器Cookie
        String cookieValue = CookieUtil.getCookieValue(request, "cartList", "UTF-8");

        if (!StringUtils.isEmpty(cookieValue)) {
            if (cookieValue.indexOf("[") >= 0) {
                //2、把从Cookie读取到json字符串数据，转换为集合
                cartList_Cookie = JSON.parseArray(cookieValue, Cart.class);


            }
        }

        //判断当前登录用户是否等于anonymousUser,

        //等于anonymousUser，表示用户未登录，购物车数据，从Cookie读取
        if(name.equals("anonymousUser")) {
          //返回cookie购物车数据
            return  cartList_Cookie;
        }else {
            //2、用户已经登录，从redis读取购物车数据
          cartList_redis=  cartService.findCartListFromRedis(name);

          //判断当用户登录后，cookie购物车数据仍然有数据，就发起合并
            if(cartList_Cookie!=null&&cartList_Cookie.size()>0){
                cartList_redis=  cartService.mergeCartList(cartList_Cookie,cartList_redis);
                //把合并后购物车数据保存更新到redis
                cartService.saveCartListToRedis(name,cartList_redis);
                //合并完成，清空本地cookie购物车数据
                CookieUtil.deleteCookie(request,response,"cartList");
            }
          return cartList_redis;
        }




    }


    //2、添加商品到购物车
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:9105",allowCredentials = "true")
    public Result addGoodsToCartList(HttpServletRequest request,HttpServletResponse response,Long itemId,Integer num){
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        System.out.println("当前登录用户:"+userName);

        //设置支持跨域的响应头
       // response.setHeader("Access-Control-Allow-Origin","http://localhost:9105");
         //设置跨域请求读取浏览器Cookie
       // response.setHeader("Access-Control-Allow-Credentials","true");

            try {
                //1、要从cookie读取当前购物车数据集合
                List<Cart> cartList = findCartList(request, response);

                //2、调用购物车服务，添加到购物车操作
                cartList = cartService.addGoodsToCartList(cartList, itemId, num);

                //3、把最新的购物车数据，存储到cookie
                //3.1、把购物车集合转换为json字符串
                String jsonString = JSON.toJSONString(cartList);
                //1、判断当前登录用户等于anonymousUser，匿名登录，从cookie读取、保存购物车数据
                if(userName.equals("anonymousUser")) {
                    //3.2、存储到cookie
                    CookieUtil.setCookie(request, response, "cartList", jsonString, 3600 * 24, "UTF-8");
                }else {
                    //当前登录不是匿名用户，保存购物车数据到redis
                    cartService.saveCartListToRedis(userName,cartList);
                }

                return new Result(true, "添加商品到购物车成功");
            } catch (Exception e) {
                e.printStackTrace();
                return new Result(false, e.getMessage());
            }






    }

}
