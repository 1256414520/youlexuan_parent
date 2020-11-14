app.service('cartService',function ($http) {

    //读取购物车数据方法
    this.findCartList=function () {
      return  $http.get('/cart/findCartList.do');
    }

    //添加商品到购物车
    this.addGoodsToCartList=function (itemId, num) {
     return   $http.get('/cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num);
    }
    //计算购物车合计方法
    this.sum=function (cartList) {
        //定义一个对象存储合计数据：合计金额、合计购买数量
        var totalValue={"totalFee":0.0,"totalNum":0};
        //遍历购物车集合
        for(var i=0;i<cartList.length;i++){
            //购物车对象
          var cart=  cartList[i];
          //继续遍历购物的购物明细集合
            for(var j=0;j<cart.orderItemList.length;j++){
              var orderItem=  cart.orderItemList[j];
              //从购物明细读取，购买数量
                totalValue.totalNum +=orderItem.num;
                //从购物明细读取各个商品合计金额，累加
                totalValue.totalFee+=orderItem.totalFee;
            }
        }

        return totalValue;
    }

    //读取当前登录用户的地址集合
    this.findAddressList=function () {
      return  $http.get('/address/findListByUserId.do');
    }

    //保存订单方法
    this.submitOrder=function (order) {
      return  $http.post('/order/add.do',order);
    }
})