app.controller('indexController',function ($scope, loginService) {

    //获取当前登录用户名
    $scope.showLoginName=function () {
        loginService.showLoginName().success(function (response) {
            //把读取到当前登录用户名赋值到变量
            $scope.loginName=response.loginName;
        })
    }
})