 //类型模板控制层 
app.controller('typeTemplateController' ,function($scope,$controller   ,typeTemplateService,brandService,specificationService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		typeTemplateService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		typeTemplateService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		typeTemplateService.findOne(id).success(
			function(response){
				$scope.entity= response;
				//把获取到的品牌下拉菜单数据，转换为json对象
                $scope.entity.brandIds=JSON.parse($scope.entity.brandIds);
                //把获取到规格下拉菜单数据，转换为json对象
                $scope.entity.specIds=JSON.parse($scope.entity.specIds);
                //把自定义扩展 属性，转换为json对象
                $scope.entity.customAttributeItems=JSON.parse($scope.entity.customAttributeItems);
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=typeTemplateService.update( $scope.entity ); //修改  
		}else{
			serviceObject=typeTemplateService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		typeTemplateService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		typeTemplateService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//准备品牌下拉菜单数据
	$scope.brandList={data:[{id:1,text:'小米'},{id:2,text:'华为'},{id:3,text:'中兴'}]};


	//编写获取品牌下拉菜单数据方法
	$scope.findBrandList=function () {
		brandService.selectOptionlist().success(function (response) {
			$scope.brandList={data:response};
        })
    }

    //准备规格下拉菜单数据
	$scope.specList={data:[{id:1,text:'内存'},{id:2,text:'尺寸'},{id:3,text:'颜色'}]};

	//获取规格下拉菜单数据方法
	$scope.findSpecList=function () {
		specificationService.selectOptionlist().success(function (response) {
			$scope.specList={data:response};
        })
    }

    //定义模板对象
	$scope.entity={customAttributeItems:[]};
    
    //点击 新增扩展属性 按钮，新增一行扩展属性
	$scope.addTableRow=function () {
       $scope.entity.customAttributeItems.push({});
    }

    //点击 删除 按钮 移除对应扩展属性行
	$scope.removeTableRow=function (index) {
		$scope.entity.customAttributeItems.splice(index,1);
    }

});	