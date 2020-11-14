package com.offcn.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.search.service.ItemSearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("itemsearch")
public class ItemSearchController {

    //引入dubbo远程调用ItemSearchService服务
    @Reference
    private ItemSearchService itemSearchService;

    @RequestMapping("search")
    public Map search(@RequestBody Map searchMap){
        return itemSearchService.search(searchMap);
    }
}