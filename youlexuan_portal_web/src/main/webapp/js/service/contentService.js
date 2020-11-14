app.service('contentService',function ($http) {

    //获取指定分类id，广告数据
    this.findByCategoryId=function (categoryId) {
      return  $http.get('content/findByCategoryId.do?categoryId='+categoryId);
    }
})