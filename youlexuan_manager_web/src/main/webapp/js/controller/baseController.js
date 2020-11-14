app.controller('baseController',function ($scope) {
    //配置分页属性
    $scope.paginationConf={
        currentPage: 1, //当前页码
        totalItems: 999,//总记录数
        itemsPerPage: 10,//每页显示的记录数
        perPageOptions:[10,20,30,40],//动态选择每页显示记录数
        onChange: function () {
            $scope.reloadList();  //当页码或者总记录数放生变化，就会请求后端方法
        }
    }

    //定义一个分页向后端发出请求的入口方法
    $scope.reloadList=function () {
        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage)
    }

    //定义一个数组，存储要删除品牌编号
    $scope.selectIds=[];


    //当复选框选中或者取消选中，更新要删除品牌id数组的值
    //$event 是angularjs内置的事件源对象，可以捕获到复选框的状态
    $scope.updateSelection=function ($event,id) {
        //判断复选框的状态，如果是选中
        if($event.target.checked){
            //把选中id编号存放到要删除的品牌数组
            $scope.selectIds.push(id);
        }else {
            //当复选框的状态为取消选中，要把要删除品牌数组的对应数据移除
            //查找id所在角标
            var index=	$scope.selectIds.indexOf(id);
            //根据角标删除数组中指定的数据
            //参数1：角标 参数2：删除数量
            $scope.selectIds.splice(index,1);
        }
    }

    //提取指定json数组里面，指定属性名称对应的值，拼接全部值为一个字符串
    //参数1：要提取内容json字符串  参数2：要提取的属性名称
    $scope.jsonToString=function (jsonString, key) {
        var value="";
        //判断json字符串是否为空
        if(jsonString){
            //把json字符串转换为json对象  [{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
          var jsonObj=  JSON.parse(jsonString);
          //遍历json数组
            for(var i=0;i<jsonObj.length;i++){
                //提取指定属性名称 {"id":27,"text":"网络"}
                if(i>0){
                    value+=","
                }
               value+= jsonObj[i][key];
            }
        }

        return value;
    }
})