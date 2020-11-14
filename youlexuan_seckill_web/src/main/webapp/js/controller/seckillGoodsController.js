app.controller('seckillGoodsController',function ($scope,$location,$interval ,seckillGoodsService) {

    //读取当前全部秒杀商品
    $scope.findList=function () {
        seckillGoodsService.findList().success(function (response) {
            $scope.list=response;
        })
    }

    //读取指定编号秒杀商品信息
    $scope.findOne=function () {

        seckillGoodsService.findOne($location.search()['id']).success(function (response) {
            $scope.entity=response;
            //获取秒杀结束时间
            //$scope.entity.endTime)
            $scope.allsecond=(new Date($scope.entity.endTime).getTime()-new Date().getTime())/1000;

            var time=$interval(function () {
                if($scope.allsecond>0){
                    --$scope.allsecond;
                   $scope.timeStr= convertTimeString($scope.allsecond);
                }else {
                    //取消定时器执行
                    $interval.cancel(time);
                    alert("秒杀结束");
                }
            },1000)
        })
    }

    //转换秒数为格式化的 1天：17:58：65
    convertTimeString=function(allsecond){
        //计算当前总秒数包含整数天
     var day=  Math.floor(allsecond/(60*60*24));

     //计算去除整数部分的天所包含的秒数后，剩余秒数所包含的小时整数部分
      var hours=Math.floor((allsecond-day*60*60*24)/(60*60));

      //计算去除天、小时所把汗秒数后，剩余秒数所包含的分钟部分
        var mintus=Math.floor((allsecond-day*60*60*24-hours*60*60)/60);
        //计算最后剩余秒数
        var second=Math.floor((allsecond-day*60*60*24-hours*60*60-mintus*60));
     var str="";

     if(day>0){
         str=day+"天";
     }

     return str+" "+hours+":"+mintus+":"+second;
    }

    //点击商品配图，跳转到商品详情页
    $scope.toItem=function (id) {
        location.href="seckill-item.html#?id="+id;
    }

    //一个倒计时效果
 /*   $scope.second=10;
    //参数1：要执行函数  参数2：间隔的时间 单位毫秒  参数3：总共执行次数
   var time= $interval(function () {
        if($scope.second>0) {
            $scope.second = $scope.second - 1;
        }else {

            //取消定时器执行
            $interval.cancel(time);
            alert("倒计时结束");
        }
    },1000);*/
 
 //秒杀下单
    $scope.submitOrder=function () {
        seckillGoodsService.submitOrder($scope.entity.id).success(function (response) {
            if(response.success){
                alert("秒杀下单成功");
                //下单成功跳转到扫码支付页面
                location.href="pay.html";
            }else {
                //判断用户未登录，跳转登录页面
                if(response.message=='用户未登录'){
                    //跳转到登录页面
                    location.href="login.html";
                }else {
                    alert(response.message);
                }
            }
        })
    }
})