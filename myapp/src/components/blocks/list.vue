<template>
	<li v-show="status && goodsInfo.status == status || goodsInfo.flag == flag">
		<div class="image">
			<img :src="goodsInfo.path" class="absolute"/>
		</div>
		<div class="desc">
			<h3 :data-id="goodsInfo.id">{{goodsInfo.name}}</h3>
			<p>{{goodsInfo.descr}}</p>
			<div class="tools">
				<h3 class="price">￥{{goodsInfo.price}}</h3>
				<div>
					<span class="minus" v-tap="minus">-</span>
					<span class="num">{{num}}</span>
					<span class="plus" v-tap="plus">+</span>
				</div>
			</div>
		</div>
	</li>
</template>

<script>
	export default {
		name:'List',
		data() {
			return{
				num:this.goodsInfo.num
			}
		},
		props:['goodsInfo', 'flag', 'status'],
		methods:{
			minus(){
				if(this.num > 0){
					this.num--;
				}
			},
			plus(){
				this.num++;
			}
		},
		watch:{
			num(){
				let list = store.session.get('list') || [];
				let obj = {
					id:this.goodsInfo.id,
					num:this.num,
					path:this.goodsInfo.path,
					name:this.goodsInfo.name,
					price:this.goodsInfo.price
				};
				if(list.length == 0){//未点菜品
					list.push(obj);
				}else{
					let index;
					let info = list.find((item, arrIndex) => {
						index = arrIndex;
						return item.id == obj.id;
					});
					if(info != undefined){//更新已有对应菜品数量
						if(obj.num == 0){//数量为零即删除对应菜品
							list.splice(index, 1);
						}else{//更新对应菜品数量
							list.splice(index, 1, obj);
						}
					}else{//添加菜品
						list.push(obj);
					}
				}
				store.session.set('list', list);
			}
		}
	}
</script>

<style scoped="scoped" lang="stylus" rel="stylesheet/stylus">
@import "~common/style/base"
@import "~common/style/icon"
	li
		overflow:hidden
		padding:0.4rem
		margin-bottom:1px
		background:#fff
		.image
			position:relative
			float:left
			width:2.9rem
			height:2.9rem
			border-radius:0.1rem
			border:1px solid #f2f2f2
			overflow:hidden
		.desc
			float:right
			width: calc(100% - 3.3rem)
			height:2.9rem
			h3
				font-size:0.5rem
				color:#333
				height:0.66rem
				word-break:break-all
				white-space:nowrap
				overflow : hidden
				text-overflow: ellipsis
			p
				height:1.16rem
				margin:0.2rem auto
				display: -webkit-box
				word-break:break-all
				overflow : hidden
				text-overflow: ellipsis
				-webkit-line-clamp: 2
				-webkit-box-orient: vertical
				font-size:0.44rem
				text-align:justify
				color:#999
			.tools
				display:flex
				justify-content:space-between
				align-items:center
				font-size:0.5rem
				text-align:center
				.price
					font-size:0.5rem
					font-weight:700
					color:#f31e1e
				.minus,.plus
					display:inline-block
					width:0.6rem
					height:0.6rem
					line-height:0.6rem
					border-radius:50%
				.minus
					color:#26a2ff
					background:#fff
					border:1px solid #26a2ff
				.plus
					color:#fff
					background:#26a2ff
					border:1px solid #26a2ff
				.num
					display:inline-block
					width:1rem
</style>