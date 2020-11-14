app.controller('payController',function ($scope,$location,payService) {

    //预下单请求方法
    $scope.createNative=function () {
        payService.createNative().success(function (response) {
            //获取预下单金额
          $scope.money=  (response.total_fee/100).toFixed(2);
          //获取预下单订单编号
          $scope.out_trade_no=  response.out_trade_no;
          //使用类库，创建二维码对象
            new QRious({
                element: document.getElementById('erweimaimg'),
                size: 300,
                level: 'H',
                value: response.qrcode
            });

            //调用查询指定订单编号的状态
            if($scope.out_trade_no!=null) {
                queryPayStatus($scope.out_trade_no);
            }
        })
    }

    //查询支付状态
    queryPayStatus=function (out_trade_no) {
        payService.queryPayStatus(out_trade_no).success(function (response) {
            if(response.success){
                //支付成功，跳转到支付成功页面
                location.href="paysuccess.html#?money="+$scope.money;
            }else {
                //alert(response.message);
                if(response.message=='超时'){
                    //alert("二维码已经超时，请刷新页面重新生成");
                    document.getElementById('timeout').innerHTML="二维码已过期，刷新页面重新获取二维码。";
                }else {
                    //跳转到支付失败页面
                    location.href="payfail.html";
                }

            }
        })
    }

    //获取静态页面路径传递支付成功金额
    $scope.getMoney=function () {
      return  $location.search()['money'];
    }
})