<template>
	<div class="content">
		<ul class="header">
			<li :class="{'active' : tabTxt == 'recommend'}" @click="select('recommend')">强力推荐</li>
			<li :class="{'active' : tabTxt == 'food'}" @click="select('food')">全部菜品</li>
			<li :class="{'active' : tabTxt == 'drink'}" @click="select('drink')">酒水饮料</li>
		</ul>
		<ul class="list">
			<list v-for="(goodsInfo, index) in goodsList" :goodsInfo="goodsInfo" :flag="tabTxt" :status="status" :key="index"></list>
		</ul>
		<div class="button">
			<mt-button type="primary" size="large" @click.native="submit">提交订单</mt-button>
		</div>
		<menus :router="router"></menus>
	</div>
</template>
<script>
	import { Toast } from 'mint-ui'
	import Menus from "../blocks/menus"
	import List from "../blocks/list"
	import { getNowTime, axiosGet } from "../../common/publicJs/apputil.js"
	export default{
		name:'StoreDefault',
		data(){
			return{
				tabTxt:'recommend',
				status:true,
				router:'storeDefault',//页面传值
				goodsList:[]
			}
		},
		created(){
			let list = store.session.get('list') || [];
			axiosGet('dish/list', {}).then(data => {
				if(list.length != 0){
					for(let i of list){
						let index;
						let info = data.find((item, arrIndex) => {
							index = arrIndex;
							return item.id == i.id;
						});
						data[index].num = i.num;
					}
				}
				this.goodsList = data;
			}).catch(function(err){
				Toast({
					message:err,
					duration:1500
				});
			});
		},
		methods:{
			select(tab){
				this.tabTxt = tab;
				this.status = tab == 'recommend' ? true : false;
			},
			//跳转至修改个人信息页面
			submit(){
				if(store.session.get('list').length == 0){
					Toast({
						message:'至少需要选择一个菜品',
						duration:1500
					});
					return;
				}
				this.$router.push({path:'/order-details'});
			}
		},
		components:{
			Menus, List
		}
	}
</script>

<style scoped="scoped" lang="stylus" rel="stylesheet/stylus">
	@import "~common/style/icon"
	@import "~common/style/base"
	.content
		width:100%
		// min-height:100vh
		// background:#f2f2f2
		padding: 1.4rem 0 3.16rem
	.header
		position:fixed
		top:0
		z-index:1
		display:flex
		justify-content:space-around
		align-items:center
		width:100%
		height:1.4rem
		font-size:0.5rem
		color:#fff
		background:#26a2ff
		li
			line-height:0.8rem
			border-bottom:2px solid transparent
		.active
			border-bottom:2px solid #fff
	.button
		position:fixed
		bottom:1.75rem
		width:100%
		.mint-button
			border-radius:0
</style>