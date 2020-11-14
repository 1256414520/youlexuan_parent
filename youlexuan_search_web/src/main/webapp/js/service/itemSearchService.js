app.service('itemSearchService',function ($http) {

    //查询方法
    this.search=function (searchMap) {
      return  $http.post('itemsearch/search.do',searchMap);
    }
})