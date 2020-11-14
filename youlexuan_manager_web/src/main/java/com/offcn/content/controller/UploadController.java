package com.offcn.content.controller;

import com.offcn.entity.Result;
import com.offcn.utils.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {

    //注入文件存储服务实际访问地址http
    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;


    //文件上传方法
    @RequestMapping("/upload")
    public Result upload(MultipartFile file){
        //1、获取上传文件的原始名称  111.jpg 222.txt  333.xlsx
        String filename = file.getOriginalFilename();
        //2、获取文件扩展名
        String extName = filename.substring(filename.lastIndexOf(".") + 1);

        //3、使用Fasddfs客户端，初始化
        try {
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");

            //4、调用客户端，执行文件上传操作
            String uploadFileId = fastDFSClient.uploadFile(file.getBytes(), extName);

            String url=FILE_SERVER_URL+uploadFileId;
            System.out.println("上传文件实际访问地址:"+url);

            return new Result(true,url);


        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传文件失败");
        }


    }
}
