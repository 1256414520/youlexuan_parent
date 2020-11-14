app.service('uploadService',function ($http) {
    
    //上传文件方法
    this.upload=function () {
        //创建对文件数据进行封装工具对象
      var formData=  new FormData();
      //把要上传的文件内容封装起来
        formData.append("file",file.files[0]);
        //发出上传请求
      return  $http({
            method: 'POST', //请求方法post
            url: '/upload.do',//请求上传服务器地址
            data: formData, //请求发送的数据
            headers: {'Content-Type':undefined},//设置请求头为未定义
            transformRequest: angular.identity

        });
    }
})