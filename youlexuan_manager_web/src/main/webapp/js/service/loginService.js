app.service('loginService',function ($http) {

    //获取当前登录用户名
    this.showLoginName=function () {
       return $http.get('/login/showLoginName.do');
    }
})