app.controller('cartController',function ($scope, cartService) {


    //读取购物车数据方法
    $scope.findCartList=function () {
        cartService.findCartList().success(function (response) {
            $scope.cartList=response;
            //计算合计
           $scope.totalValue= cartService.sum($scope.cartList);
        })
    }

    //添加商品到购物车
    $scope.addGoodsToCartList=function (itemId, num) {
        cartService.addGoodsToCartList(itemId,num).success(function (response) {
            if(response.success){
                //刷新当前购物车
                $scope.findCartList();
            }else {
                alert(response.message);
            }
        })
    }

    //读取当前登录用户地址集合
    $scope.findAddressList=function () {
        cartService.findAddressList().success(function (response) {
            $scope.addressList=response;
            //遍历当前用户地址集合
            for(var i=0;i<$scope.addressList.length;i++){
                //比对地址是否默认选项，如果是默认，就把默认地址对象设置到选中地址
                if($scope.addressList[i].isDefault=='1'){
                    $scope.address=$scope.addressList[i];
                    break;
                }
            }
        })
    }

    //选择配送地址，调用方法
    $scope.selectAddress=function (address) {
        $scope.address=address;
    }

    //判断那个地址被选中
    $scope.isSelectEd=function (address) {
        if($scope.address==address){
            return true;
        }else {
            return false;
        }
    }

    //定义一个订单对象，设置付款方式属性 默认1 扫码支付   2 货到付款
    $scope.order={"paymentType":"1"};

    //用户点击选择付款方式，调用方法
    $scope.selectPayType=function (payType) {
        $scope.order.paymentType=payType;
    }
    
    //提交保存订单方法
    $scope.submitOrder=function () {
        //把记录用户选中的配送地址信息转换到订单对象的配送信息
        $scope.order.receiverAreaName=$scope.address.address;
        $scope.order.receiver=$scope.address.contact;
        $scope.order.receiverMobile=$scope.address.mobile;

        //调用保存订单方法
        cartService.submitOrder($scope.order).success(function (response) {
            if(response.success){
                alert("保存订单成功");
                //判断支付方式是否是扫码支付
                if($scope.order.paymentType=='1'){
                    //跳转到扫码页面
                    location.href="pay.html";
                }else {
                    //货到付款，直接跳转到交易成功页面
                    location.href="paysuccess.html";
                }

            }else {
                alert(response.message);
            }
        })
    }

})