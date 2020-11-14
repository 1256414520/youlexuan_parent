app.service('seckillGoodsService',function ($http) {
    //读取全部秒杀商品
    this.findList=function () {
      return  $http.get('/seckillGoods/findList.do');
    }

    //读取指定编号秒杀商品详情信息
    this.findOne=function (id) {
      return  $http.get('/seckillGoods/findOneFromRedis.do?id='+id);
    }
    
    //秒杀下单请求方法
    this.submitOrder=function (seckillId) {

      return  $http.get('/seckillOrder/submitOrder.do?seckillId='+seckillId);
    }
})