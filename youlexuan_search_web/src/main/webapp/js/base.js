//创建自定义模块
var app=angular.module('youlexuan',[]);

//自定义一个全局过滤器
app.filter('trustHtml',['$sce',function ($sce) {
    
   return function(data) {
       return $sce.trustAsHtml(data);
    }
}])