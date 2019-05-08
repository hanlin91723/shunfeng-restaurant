<template>
	<div>
		<ul class="menu">
			<li @click="toStore" :class="router == 'storeDefault' ? 'color' : ''">
				<img src="../../common/image/footer/mall-on.png" v-if="router == 'storeDefault'"/>
				<img src="../../common/image/footer/mall-off.png" v-else/>
				<p>商家店铺</p>
			</li>
			<li @click="toOrderForm" :class="router == 'orderFormDefault' ? 'color' : ''">
				<img src="../../common/image/footer/asset-on.png" v-if="router == 'orderFormDefault'"/>
				<img src="../../common/image/footer/asset-off.png" v-else/>
				<p>历史订单</p>
			</li>
			<li @click="toMine" :class="router == 'mineDefault' ? 'color' : ''">
				<img src="../../common/image/footer/mine-on.png" v-if="router == 'mineDefault'"/>
				<img src="../../common/image/footer/mine-off.png" v-else/>
				<p>个人中心</p>
			</li>
		</ul>
	</div>
</template>

<script>
	import { Toast } from 'mint-ui'
	export default {
		name:'Menus',
		props:['router'],
		methods:{
			//去商家店铺
			toStore(){
				this.$router.push({path:'/store-default'});
			},
			//去历史订单
			toOrderForm(){
				this.$router.push({path:'/goods-list'});
			},
			//去个人中心
			toMine(){
				this.$router.push({path:'/mine-default'});
			}
		}
	}
</script>

<style scoped="scoped" lang="stylus" rel="stylesheet/stylus">
@import "~common/style/base"
@import "~common/style/icon"
	.menu
		position: fixed
		bottom: 0
		z-index: 999
		display:flex
		justify-content:space-around
		width: 100%
		padding:0.3rem
		text-align: center
		color: #999
		box-shadow: 0 0 3px #ddd
		background:#fff
		li.color
			color:#fd930c
		li
			position:relative
			width: 33%
			font-size: 0.34rem
			img
				width:0.6rem
				height:0.6rem
		i
			font-size: 0.6rem
		.camera
			position: absolute
			left: 0
			top:0
			margin:-0.95rem 0 0 -50%
			width: 2.04rem
			border-radius: 50%
	.marsk-fade-enter-active,.marsk-fade-leave-active{
		transition: all .5s
	}
		
	.marsk-fade-enter, .marsk-fade-leave-to{
		opacity: 0
	}
		.marsk
			position:fixed
			top:0
			bottom:0
			right:0
			left:0
			z-index:1000
			background:rgba(0, 0, 0, 0.6)
	.slide-fade-enter-active{
		transition: all .3s ease
	}
	.slide-fade-leave-active{
		transition: all .3s cubic-bezier(1.0, 0.5, 0.8, 1.0)
	}
	.slide-fade-enter, .slide-fade-leave-to{
		transform: translateY(7.48rem)
		opacity: 0
	}
		.action-sheet
			position:fixed
			bottom:0
			left:0
			right:0
			background:#dbdada
			z-index:1001
			text-align:center
			padding-bottom:0.5rem
			.msg
				position:relative
				text-align:left
				line-height:1.46rem
				font-size:0.46rem
				color:#ffffff
				background:#2acf5f
				padding-left:1.2rem
				.icon-tip
					 font-size:0.62rem
					 position:absolute
					 left:0.36rem
					 top:0.42rem
			.icon-contain
				overflow:hidden
				width:6.44rem
				margin:0.72rem auto
				.sub-menu
					float:left
					&:first-child
						margin-right:1.56rem
					.icon-camera,.icon-album
						font-size:1.02rem
						width:2.44rem
						height:2.44rem
						border-radius:2.44rem
						background:#fff
						text-align:center
						line-height:2.44rem
					.icon-album
						font-size:1.2rem
					p
						padding-top:0.4rem
						font-size:0.4rem
				input
					display:none
			.icon-close
				font-size:0.5rem
				color:#999
				display:block
				width:1.24rem
				margin:0 auto
</style>