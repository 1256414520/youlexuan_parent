app.controller('contentController',function ($scope,contentService) {

    //创建一个数组来存储广告数据
    $scope.contentList=[];
    //根据分类编号，获取对应广告数据
    $scope.findByCategoryId=function (categoryId) {
        contentService.findByCategoryId(categoryId).success(function (response) {
             $scope.contentList[categoryId]=response;
        })
    }

    //点击搜索按钮，调用此方法，把搜索关键字传递到search_web进行查询
    $scope.search=function () {
        location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
    }
})