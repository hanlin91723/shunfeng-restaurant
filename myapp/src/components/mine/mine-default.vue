<template>
	<div class="content">
		<div class="header">
			<div class="txt-wrap">
				<p>昵称：{{nickname}}</p>
				<p>年龄：{{age}}岁</p>
			</div>
			<button class="signIn" @click="signIn">修改个人信息</button>
		</div>
		<ul class="list">
			<li>
				<i class="icon icon-balance"><span class="path1"></span><span class="path2"></span><span class="path3"></span></i>
				<span>消费金额</span>
				<span>&yen;{{consumption}}</span>
			</li>
			<li>
				<i class="icon icon-distribution"></i>
				<span>会员等级</span>
				<span>{{level}}会员</span>
			</li>
			<li>
				<i class="icon icon-gift"></i>
				<span>可享优惠</span>
				<span>{{discount}}</span>
			</li>
		</ul>
		<div class="logout" @click="logout">退出登录</div>
		<menus :router="router"></menus>
	</div>
</template>
<script>
	import { Toast } from 'mint-ui'
	import Menus from "../blocks/menus"
	import { axiosGet } from "../../common/publicJs/apputil.js"
	export default{
		name:'MineDefault',
		data(){
			return{
				nickname:'',//用户昵称
				age:'',//年龄
				router:'mineDefault',//页面传值
				consumption:'',//消费金额
				level:'',//会员等级
				discount:''//可享优惠
			}
		},
		created(){
			this.getUserInfo();
		},
		methods:{
			//个人信息查询
			getUserInfo(){
				axiosGet('user/details', {}).then((data) => {
					this.nickname = data.nickname;
					this.age = data.age;
					this.consumption = data.amount;
					this.level = data.rankName;
					this.discount = data.discount;
				}).catch(function(err){
					Toast({
						message:err,
						duration:1500
					});
				});
			},
			//点击退出登录
			logout(){
				store.session.remove('userId');
				Toast({
					message:'退出成功',
					duration:1500
				});
				let times = setTimeout(() => {
					this.$router.replace({path:'/login-password'});
					clearTimeout(times);
				},1500);
			},
			//跳转至修改个人信息页面
			signIn(){
				this.$router.push({path:'/nickname-signature'});
			}
		},
		components:{
			Menus
		}
	}
</script>

<style scoped="scoped" lang="stylus" rel="stylesheet/stylus">
	@import "~common/style/icon"
	@import "~common/style/base"
	.content
		position:absolute
		width:100%
		min-height:100vh
		background:#f2f2f2
	.header
		display:flex
		justify-content:space-between
		align-items:center
		padding: 0.6rem 0 0.6rem 1rem
		background-image: linear-gradient(45deg, #26a2ff, #4846cc)
		.img-wrap
			display:table-cell
			margin-right:0.3rem
			vertical-align:middle
			font-size:0
			img
				width:2rem
				height:2rem
				border-radius: 50%
				object-fit:cover
		.txt-wrap
			width:6.4rem
			font-size: 0.4rem
			color: #fff
			p
				height:0.8rem
				overflow:hidden
				text-overflow:ellipsis
				white-space:nowrap
				line-height:0.8rem
				span
					display:inline-block
					padding:0.05rem 0.16rem
					margin-right:0.2rem
					height:0.6rem
					line-height:0.6rem
					font-size:0.3rem
					color:#116aee
					background:#fff
					border-radius:0.14rem
		.signIn
			display:block
			padding:0 0.3rem 
			font-size:0.36rem
			text-align:center
			line-height:0.84rem
			color:#fff
			background:#1574f4
			border-radius:0.42rem 0 0 0.42rem
		.hasSignIn
			width:2.7rem
			background:#c5c5c9
	ul.asset
		display:flex
		flex-wrap:nowrap
		justify-content:space-around
		background:#fff
		text-align:center
		li
			width:5.7rem
			height:3.1rem
			padding:0.5rem 0 0.2rem
			background:#fff
			font-size:0.4rem
			line-height:0.8rem
			color:#999
			i:before
				color:#333
			i
				font-size:0.7rem
			p
				line-height:0.4rem
			h5
				height:1.2rem
				line-height:1.2rem
				font-size:0.6rem
				font-weight:400
				color:#333
	ul.list
		margin: 0.3rem auto
		width: 100%
		padding:0 0.5rem
		list-style: none
		background: #fff
		li.last
			border-bottom: none
		li
			padding:0.5rem 0.2rem
			background: #fff
			border-bottom: 1px solid #f2f2f2
			font-size: 0.5rem
			color: #333
			span
				padding-left: 0.4rem
			i
				display: inline-block
				width:0.6rem
				font-size: 0.6rem
				vertical-align: text-top
				span
					padding: 0
			span:last-child
				font-size: 0.48rem
				float: right
				font-style: normal
				color: #999
				line-height: 0.7rem
	.logout
		width:11.4rem
		height:1.5rem
		margin:1rem auto
		font-size:0.5rem
		line-height:1.5rem
		text-align:center
		color:#fff
		background:#ccc
		border-radius:0.1rem
</style>