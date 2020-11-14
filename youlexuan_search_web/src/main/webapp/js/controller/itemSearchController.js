app.controller('itemSearchController',function ($scope,$location, itemSearchService) {

    //查询方法
    $scope.search=function () {
        //把跳转到页码，转换为数字
        $scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo);
        itemSearchService.search($scope.searchMap).success(function (response) {

            //保存返回的查询结果
          $scope.resultMap=  response;

          //调用分页页签构造方法
            buildPageLabel();
        })
    }

    //定义搜索条件数据结构
    //keywords :搜索关键字   category:商品分类  brand:品牌
    //spec：规格以及规格选项数据
    //price：价格区间
    //分页相关参数 pageNo：当前页码   pageSize：每页显示的记录数
    //扩充排序的参数 sortField ：排序字段  sort ：排序方式 ASC DESC
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':10,'sortField':'','sort':''};

    //当用户点击对应搜索条件，把搜索数据添加到搜索条件数据对象
    //key：点击的搜索项目名称   value:点击的对一个的值
    //
    $scope.addSearchItem=function (key, value) {
       if(key=='category'||key=='brand'||key=='price'){
           //添加搜索条件的值到搜索数据对象
           $scope.searchMap[key]=value;
       }else {
         //规格数据，添加到规格对象里面
           $scope.searchMap.spec[key]=value;
       }

       //重置当前页码为第一页
        $scope.searchMap.pageNo=1;

       //发出查询请求
        $scope.search();
    }

    //当用户点击对应面包屑，撤销对应搜索条件
    $scope.removeSearchItem=function (key) {
        if(key=='category'||key=='brand'||key=='price'){
            //移除对应属性值
            $scope.searchMap[key]='';
        }else {
            delete $scope.searchMap.spec[key];
        }
        //重置当前页码为第一页
        $scope.searchMap.pageNo=1;

        //发出查询请求
        $scope.search();
    }

    //构建前端分页标签方法
    buildPageLabel=function () {
        //定义一个数组存储分页页码
        $scope.pageLabel=[];
        //设置一个变量，总页码
       var maxPageNo= $scope.resultMap.totalPage;
       //设置变量存储开始页码
        var firstPage=1;
        //设置一个变量存储结束页码
        var lastpage=maxPageNo;

        //开始位置显示省略号
        $scope.firstDot=true;
        //结束位置显示省略号
        $scope.lastDot=true;

        //判断总页码大于等于5页，显示部分页码
        if(maxPageNo>=5){

            //情况1，当前页码小于等于3，显示前5页
            if($scope.searchMap.pageNo<=3){
                //设置结束页码为5
                lastpage=5;
                //前3页，此时开始位置不需要显示省略号
                $scope.firstDot=false;
            } else if($scope.searchMap.pageNo>=maxPageNo-2){
                //情况2，当前页码+2大于等于总页码
                //设置开始页码
                firstPage=maxPageNo-4;
                //最后3页，结束位置不需要显示省略号
                $scope.lastDot=false;
            }else {
                //情况3：在中间位置
                firstPage=$scope.searchMap.pageNo-2;
                lastpage=$scope.searchMap.pageNo+2;
            }
        }else {
            //总页码小于5页，全部显示了页码，前后都不需要显示省略号
            $scope.firstDot=false;
            $scope.lastDot=false;
        }

        //根据开始页码、结束页码数据循环遍历，产生分页页签
        for(var i=firstPage;i<=lastpage;i++){
            //把页码存入到分页数组
            $scope.pageLabel.push(i);
        }
    }

    //跳转到指定页码 pageNo 要跳转到的页码
    $scope.queryByPage=function (pageNo) {
        //需要对要跳转的页码进行判断
        //跳转到页码不能是负数，要跳转到页码不能大于总页码
        if(pageNo<=0||pageNo>$scope.resultMap.totalPage){
            //不满足条件，结束跳转
            return;
        }
        //满足条件继续跳转

        //把要跳转到页码，设置为搜索查询条件的当前页码
        $scope.searchMap.pageNo=pageNo;

        //重新查询数据
        $scope.search();
    }

    //判断当前页码是否是第一页
    $scope.isTopPage=function () {
        //判断当前页码是否等于1
        if($scope.searchMap.pageNo==1){
            return true;
        }else {
            return false;
        }
    }

    //定义一个返回的总页码数据结构
    $scope.resultMap={"totalPage":1}

    //判断当前页码是否为最后一页
    $scope.isEndPage=function () {
        //把总页码和当前页码进行比对，如果相等，肯定是最后一页
        /*if($scope.searchMap.pageNo==$scope.resultMap.totalPage){
            return true;
        }else {
            return false;
        }
        */
      return  ($scope.searchMap.pageNo==$scope.resultMap.totalPage)?true:false;
    }
    
    //判断页码是否是当前页
    $scope.isPage=function (p) {
        if(parseInt(p)==parseInt($scope.searchMap.pageNo)){
            return true;
        }else {

            return false;
        }
    }

    //点击对应排序内容设置排序参数
    $scope.sortSearch=function (sortField, sort) {
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sort=sort;

        //发出查询
        $scope.search();
    }

    //判断搜索关键字，是否包含品牌内容
    $scope.keywordsIsBrand=function () {
        //获取品牌
        //遍历品牌集合
      for(var i=0;i<$scope.resultMap.brandList.length;i++){
          //获取到各个名牌，调用品牌名称和搜索关键字进行比对
          if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
              return true;
          }
      }

      return false;
    }

    //页面加载的时候捕获查询的关键字，发出查询
    $scope.loadkeywords=function () {
     $scope.searchMap.keywords=   $location.search()['keywords'];
     //发出查询
        $scope.search();
    }
})