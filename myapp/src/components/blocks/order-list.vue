<template>
	<li>
		<ul class="order-list">
			<li class="order-time">点餐时间：{{list.reserveTime}}</li>
			<li class="goods-list" v-for="(item, index) of list.dishes" :key="index" :data-id="item.id">
				<div class="goods-wrap">
					<div class="image">
						<img :src="item.path" class="absolute" />
					</div>
					<p class="goods-name">{{item.name}}</p>
				</div>
				<div class="goods-price">
					<p class="num">&times;{{item.num}}</p>
					<h3 class="price">&yen;{{item.price}}</h3>
				</div>
			</li>
			<li class="goods-list">
				<div class="goods-wrap">所享优惠</div>
				<div class="goods-price">{{list.discount}}</div>
			</li>
			<li class="goods-total">实付&yen;{{list.payPrice}}</li>
		</ul>
	</li>
</template>

<script>
	export default {
		name:'OrderList',
		props:['list']
	}
</script>

<style scoped="scoped" lang="stylus" rel="stylesheet/stylus">
@import "~common/style/base"
@import "~common/style/icon"
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