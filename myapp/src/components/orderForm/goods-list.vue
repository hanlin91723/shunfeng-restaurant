<template>
	<div style="min-height:100vh;background:#f2f2f2;">
		<mt-header fixed title="历史订单"></mt-header>
		<div class="margin-top">
			<mt-loadmore :top-method="loadTop" :bottom-method="loadBottom" :bottom-all-loaded="allLoaded" @top-status-change="handleTopChange" @bottom-status-change="handleBottomChange" :autoFill="autoFill" ref="loadmore">
		       	<div slot="top" class="mint-loadmore-top">
			        <span v-show="topStatus === 'loading'">刷新中...</span>
			    </div>
			    <div slot="bottom" class="mint-loadmore-bottom">
			        <span class="nomore" v-show="bottomStatus === 'loading'">加载中...</span>
			    </div>
				<ul v-if='loadStatus'>
					<order-list class="order-list" v-for="(item, index) in list" :list="item" :key="index"></order-list>
					<li class="tips" v-if="tipsStatus">暂无记录！</li>
					<li class="tips" v-if="!tipsStatus && allLoaded">没有更多了！</li>
				</ul>
				<div class="vue-jsonp-loading" v-else>加载中...</div>
			</mt-loadmore>
		</div>
		<menus :router="router"></menus>
	</div>
</template>

<script>
	import { Toast } from 'mint-ui'
	import Menus from "../blocks/menus"
	import OrderList from "../blocks/order-list"
	import { axiosGet } from "../../common/publicJs/apputil.js"
	export default{
		name:'GoodsList',
		data(){
			return{
		        count:10,//每页条数
				page:0,//当前页
				type:'',//类型
				keyword:'',//关键字
				list:[],//列表
				router:'orderFormDefault',//页面传值
				autoFill:false,//上拉下拉填充
		    	topStatus: '',//自定义上拉样式
		        bottomStatus:'',//自定义下拉样式
		        tipsStatus:false,//提示状态
		        loadStatus:false,//第一次数据加载完成
		        allLoaded:false//数据加载完成
			}
		},
		created(){
			this.loadTop();
		},
		methods:{
			//列表查询
			QueryList(type){
		  		let params = {
				 	pageSize: this.count,
				 	pageNum: this.page
				};
				axiosGet('order/list', params).then(data => {
				 	this.list = this.list.concat(data.orderVOS);
				 	this.tipsStatus = this.list.length == 0 ? true : false;
				 	this.loadStatus = true;
				 	if(data.orderVOS.length == 0){
			    		this.allLoaded = true;
			    	}else{
			    		this.page++;
			    	}
					if(type == 'top'){
						this.$refs.loadmore.onTopLoaded();//刷新重定位
					}else if(type == 'bottom'){
						this.$refs.loadmore.onBottomLoaded();//加载重定位
					}
				}).catch(err => {
					this.loadStatus = true;
			 		Toast({
						message:err,
						duration:1500
					});
				});
			},
			//下拉刷新数据的操作
			loadTop() {
	        	this.list = [];
				this.page = 0;
				this.allLoaded = false;
			  	this.QueryList('top');
		    },
		    //上拉加载更多数据的操作
		    loadBottom(){
		    	this.QueryList('bottom');
		    },
		    //自定义状态
		    handleTopChange(status) {
	       	 	this.topStatus = status;
		    },
		    handleBottomChange(status){
		    	this.bottomStatus = status;
		    }
		},
		components:{
			Menus, OrderList
		}
	}
</script>

<style scoped="scoped" lang="stylus" rel="stylesheet/stylus">
	@import "~common/style/icon"
	@import "~common/style/base"
	.margin-top
		overflow:scroll
		width: 100%
		height:91vh
		padding-top:1.4rem
		background: #f2f2f2
		.vue-jsonp-loading
			text-align: center
			padding: 0.3rem
		.order-list
			margin-top:0.4rem
			li
				padding:0.2rem 0.4rem
				font-size:0.45rem
				color:#333
				background:#fff
			.order-time
				font-weight:700
			.goods-list
				display:flex
				justify-content:space-between
				align-items:center
				padding-left:1.2rem
				.goods-wrap
					display:flex
					align-items:center
					.image
						position:relative
						width:1rem
						height:1rem
						border-radius:0.1rem
						border:1px solid #f2f2f2
					.goods-name
						margin-left:0.2rem
				.goods-price
					display:flex
					justify-content:space-between
					align-items:center
					text-align:right
					.num
						width:1rem
						font-size:0.4rem
						color:#999
					.price
						width:2.8rem
						font-weight:700
			.goods-total
				font-size:0.6rem
				text-align:right
				font-weight:700
		.tips
			background:none
			text-align:center
			font-size: 0.5rem
			line-height:1.6rem
			color: #333				
</style>