 //用户表控制层 
app.controller('userController' ,function($scope,$controller   ,userService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		userService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		userService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		userService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=userService.update( $scope.entity ); //修改  
		}else{
			serviceObject=userService.add( $scope.entity  );//增加 
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

	//定义用户对象
	$scope.entity={};

	//用户点击注册按钮，调用方法
	$scope.reg=function(){

		//进行前端校验

		//1、校验用户名是否为空
		if($scope.entity.username==null||$scope.entity.username==''){
			alert("用户名不能为空");
			//结束注册
			return;
		}

		//2、校验密码、确认密码都不能为空，而且两个密码要一致
		if($scope.entity.password==null||$scope.entity.password==''){
			alert("密码不能为空");
			return;
		}

		//直接校验重新输入密码和原密码一致相等
		if($scope.entity.password!=$scope.repassword){
			alert("两次输入的密码不同，请重新输入");
			return;
		}
		//简单校验手机号是否输入
		if($scope.entity.phone==null||$scope.entity.phone==''){
			alert("手机号必须输入");
			return;
		}

		//调用用户服务，发出保存请求
		userService.add($scope.entity,$scope.smscode).success(function (response) {
			if(response.success){
				alert("用户注册成功");
			}else {
				alert(response.message);
			}
        })
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		userService.dele( $scope.selectIds ).success(
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
		userService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//发送短信验证码
	$scope.sendsmsCode=function () {

		//判断手机号码是否存在
		if($scope.entity.phone==null||$scope.entity.phone==''){
			alert("手机号码不能为空");
			return;
		}
		userService.sendsmsCode($scope.entity.phone).success(function (response) {
			if(response.success){
				alert(response.message);
			}else {
				alert(response.message);
			}
        })

    }
    
});	