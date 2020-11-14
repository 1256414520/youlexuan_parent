app.controller('itemController',function ($scope,$http) {


    //编写调整购买数量方法
    $scope.addNum=function (num) {
        //把用户输入的购买数量转换为整数

        //在当前记录的购买数量基础之上进行运算
        $scope.num=parseInt($scope.num)+num;

        //判断购买数量，不能小于1
        if($scope.num<1){
            //设置默认值为1
            $scope.num=1;
        }
    }

    //定义一个对象记录用户选择的规格选项
    $scope.specificationItems={};

    //编写用户选择制定规格选项时要调用方法,记录用户选择的规格选项
    //name：用户选择的规格名称 value：用户选择的规格选项值
    $scope.selectSpecification=function (name, value) {

        $scope.specificationItems[name]=value;

        //更新sku数据
        searchSku();
    }

    //判断指定的规格和规格选项是否被选中
    $scope.isSelectEd=function (name, value) {

        if($scope.specificationItems[name]==value){
            return true;
        }else {
            return false;
        }
    }
    
    //加载默认sku对象
    $scope.loadSku=function () {
        //{
        //                 id:1369299,
        //                 title:"高考手机纪念版2020款 移动4G 128G",
        //                 price:1,
        //                 spec:{"网络":"移动4G","机身内存":"128G"}
        //             }
      $scope.sku=skuList[0];

      //深克隆技术，把默认商品的规格数据，复制到用户选中的记录规格选项数据对象
        $scope.specificationItems=JSON.parse(JSON.stringify($scope.sku.spec));


    }

    //定义一个方法，比对2个json对象，看是否相同
    matchObject=function (map1, map2) {
        //遍历map1
        for(var k in map1){
            //比对map1各个元素和map2比对
            if(map1[k]!=map2[k]){
                return false;
            }
        }

        //遍历map2
        for(var j in map2){
            if(map2[j]!=map1[j]){
                return false;
            }
        }

        return true;
    }


    //遍历sku集合，和用户选择的规格选项进行比对
    searchSku=function () {
        //遍历sku集合
     for(var i=0;i<skuList.length;i++){
         //提取sku的规格属性和用户选择的规格选项比对
         if(matchObject(skuList[i].spec,$scope.specificationItems)){
             //当前sku对象设置为选中sku变量
           $scope.sku=  skuList[i];
           return;
         }
     }

     return $scope.sku={id:0,title:'----',price:0};
        
    }
    
    //点击添加到购物车，调用方法
    $scope.addToCart=function () {
       // alert("添加商品编号为:"+$scope.sku.id+" 到购物车成功");
        //调用购物车web的添加到购物车接口
        $http.get('http://localhost:9108/cart/addGoodsToCartList.do?itemId='+$scope.sku.id+'&num='+$scope.num,{'withCredentials':true}).success(function (response) {
            if(response.success){
                //添加商品到购物车成功
                alert("添加商品到购物车成功");
                //跳转到购物车页面
                location.href="http://localhost:9108/cart.html";
            }else {
                alert(response.message);
            }
        })
    }

})