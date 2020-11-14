 //商品控制层 
app.controller('goodsController' ,function($scope,$location,$controller   ,goodsService,uploadService,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
		//捕获传递过来的要修改的商品编号
	var id=	$location.search()['id'];
	//判断id如果没有值，结束读取
		if(id==null){
			//结束当前方法执行
			return;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				//单独处理商品介绍，富文本编辑器的设置
				editor.html($scope.entity.goodsDesc.introduction);
				//把读取到商品配图的json字符串转换为json对象
                $scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
                //把读取到商品扩展属性，从json字符串转换为json对象
                $scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);
			   //转换商品扩展属性里面对应选中规格和规格选项 从json字符串转换为json对象
                $scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
			   //把sku集合，其中规格属性，转换json对象

				//遍历sku集合
				for(var i=0;i<$scope.entity.itemList.length;i++){
					//提起规格属性，转换为json对象
                    $scope.entity.itemList[i].spec=JSON.parse($scope.entity.itemList[i].spec);
				}

			}
		);				
	}

	//判断指定规格选项是否被选中
	//参数1：规格名称  参数2：规格选项名称
	$scope.checkAttributeValue=function(specName,optionName){
          //[{"attributeValue":["移动4G"],"attributeName":"网络"},{"attributeValue":["32G"],"attributeName":"机身内存"}]
	var obj=	$scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',specName);
		//判断指定规格名称是否存在
		if(obj!=null){
			//指定规格存在，要继续判断，对应规格选项是否存在
		  if(obj.attributeValue.indexOf(optionName)>=0){
			return true;
		  }else {
		 	return false;
		  }
		}else {
			//指定规格名称不存在
			return false;
		}
	}
	
	//保存 
	$scope.save=function(){
        //提取富文本编辑的内容，设置到 商品介绍属性
        $scope.entity.goodsDesc.introduction=editor.html();
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					alert("商品保存成功");
					//保存成功，返回商品管理列表
					location.href="goods.html";


				}else{
					alert(response.message);
				}
			}		
		);				
	}

	//新增商品方法
	$scope.add=function(){
		//提取富文本编辑的内容，设置到 商品介绍属性
		$scope.entity.goodsDesc.introduction=editor.html();
		goodsService.add($scope.entity).success(function (response) {
			if(response.success){
             alert("新增商品成功");
             //清理entity对象数据清理
				$scope.entity={"goods":{},"goodsDesc":{"itemImages":[],"specificationItems":[]}};
				//清理富文本编辑器内容
				editor.html('');
			}
        })
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
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
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}


	//上传处理方法
	$scope.upload=function () {
		uploadService.upload().success(function (response) {
			if(response.success){
				$scope.image_entity.url=response.message;
			}else {
				alert(response.message);
			}
        }).error(function () {
			alert("上传图片失败");
        })
    }

    //定义图片存储的数据数组
	$scope.entity={"goods":{},"goodsDesc":{"itemImages":[],"specificationItems":[]}};

	//点击图片上传窗口里面保存按钮，调用方法，把上传的图片对象，保存到图片数组
	$scope.add_image_entity=function () {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }

    //点击图片列表的 删除 按钮，移除对应图片
	$scope.remove_image_entity=function (index) {
		$scope.entity.goodsDesc.itemImages.splice(index,1);
    }

    //读取一级分类数据方法
	$scope.selectItemCat1List=function () {
		itemCatService.findByParentId(0).success(function (response) {
			$scope.itemCat1List=response;
        })
    }

    //监控一级分类编号，看是否发生了变化，获取该一级分类id对应二级分类数据
	$scope.$watch('entity.goods.category1Id',function (newValue, oldValue) {
		//判断newValue是否存在，
		if(newValue){
			itemCatService.findByParentId(newValue).success(function (response) {
				$scope.itemCat2List=response;
            })
		}
    })

	//监控二级分类编号，看是否发生变化，获取对应二级分类id所属的三级分类
	$scope.$watch('entity.goods.category2Id',function (aaa, bbb) {
		if(aaa){
			itemCatService.findByParentId(aaa).success(function (response) {
				$scope.itemCat3List=response;
            })
		}
    })
	//监控三级分类编号，看是否发生变化，获取对应三级分类id 对应分类对象
	$scope.$watch('entity.goods.category3Id',function (newValue, oldValue) {
		if(newValue){
			itemCatService.findOne(newValue).success(function (response) {
			$scope.entity.goods.typeTemplateId=response.typeId;
            })
		}
    })
	//监控模板id，如果发生变化，就去调用后端，获取该模板id对应模板信息
	$scope.$watch('entity.goods.typeTemplateId',function (newValue, oldValue) {
		if(newValue){
			typeTemplateService.findOne(newValue).success(function (response) {
				$scope.typeTemplate=response;
				//需要把模板对象包含的品牌json字符串转换为json对象
                $scope.typeTemplate.brandIds=JSON.parse($scope.typeTemplate.brandIds);
               //为了避免冲突，判断id是否有值
               if($location.search()['id']==null) {
                   //把模板对象所包含的扩展属性json字符串转换为json对象
                   $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
               }
            })
            //根据模板编号，获取对应规格和规格选项数据集合
            typeTemplateService.findSpecList(newValue).success(function (response) {
               $scope.specList= response;
            })
		}
    })

    //当用户点击规格选项，取消点击 调用本方法
    //参数1：事件源对象 参数2：规格名称  参数3：选项值
    $scope.updateSpecAttribute=function ($event,name,value) {

	    //1、判断指定规格名称，是否存在json数据
     var obj=   $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',name);

     //2、判断obj对象是否为空
        if(obj!=null){
            //用户点击操作规格数据已经存在
            //如果用户是选中规格选项，把选中规格选项存入规格选项数组
            if($event.target.checked){
                obj.attributeValue.push(value);
            }else {

                //如果用户取消勾选，把对应规格选项移除
                obj.attributeValue.splice(obj.attributeValue.indexOf(value),1);
                //移除规格选项，需要判断规格选项数组是否等于0，如果等于0，要移除整个规格对象节点
                if(obj.attributeValue.length==0){
                    $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(obj),1);
                }
            }

        }else {
            //第一次选择 指定规格
            $scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
        }
    }

    //当用户点击指定规格选项，调用，创建sku列表
	$scope.createItemList=function () {
		//1、定义一个数组，作为sku列表存储数组
		$scope.entity.itemList=[{"spec":{},"price":0.0,"num":0,"status":"0","isDefault":"0"}];

		//2、依据用户选中规格选项
		var items=$scope.entity.goodsDesc.specificationItems;
		//打印输出变量
		console.log("输出变量:items:")
		console.log(items);
		//:[{"attributeName":"网络","attributeValue":["移动4G","联通4G","电信4G"]},{"attributeName":"机身内存","attributeValue":["128G","64G"]}]
		//3、循环遍历用户选中规格选项数组
		for(var i=0;i<items.length;i++){
			//获取到其中单个节点数据 {"attributeName":"网络","attributeValue":["移动4G","联通4G","电信4G"]}


			//调用扩充sku列表数据方法
		$scope.entity.itemList=addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}

    }

    //编写扩充sku列表数据方法
	//参数1：原有sku集合  参数2：规格名称  参数3：规格选项集合
    addColumn=function (list,attributeName,attributeValue) {
        //创建一个集合存储扩充后sku列表
		var newList=[];

         //1、遍历原有sku集合
		//[{"spec":{},"price":0.0,"num":0,"status":"0","isDefault":"0"}]
		for (var i=0;i<list.length;i++){
			//{"spec":{},"price":0.0,"num":0,"status":"0","isDefault":"0"}
			//读取当前行的内容，定义到变量oldRow
			var oldRow=list[i];
			//遍历规格选项集合
			for(var j=0;j<attributeValue.length;j++){

				//采用深克隆技术，复制oldRow的内容到newRow,内容完全相同，内存指向是不同的地址
				var newRow=JSON.parse(JSON.stringify(oldRow));

				//{"spec":{"网络":"移动4G",},"price":0.0,"num":0,"status":"0","isDefault":"0"}
                //执行扩充规格的操作
				newRow.spec[attributeName]=attributeValue[j];
				//把扩充后的存储新集合
				newList.push(newRow);
			}
		}

		return newList;
    }

    //定义商品状态 数组
	$scope.status=['待审核','审核通过','审核未通过','关闭'];

	//定义一个存储全部分类数据数组
	$scope.itemCatList=[];

	//编写获取全部分类数据方法，写入到全部分类数组
	$scope.findItemCatList=function () {
		itemCatService.findAll().success(function (response) {
			//结果过来的分类数据，数据项比较多，不需要这么多数据
			//遍历分类数组，提取分类编号、分类名称，存放到分类数组
			for(var i=0;i<response.length;i++){
				//单个节点就是一个分类对象
				$scope.itemCatList[response[i].id]=response[i].name;
			}
        })
    }

    //跳转到修改页面方法
	$scope.toEdit=function (id) {
		location.href="goods_edit.html#?id="+id;
    }

    
});	