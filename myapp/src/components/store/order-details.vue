<template>
	<div>
		<mt-header fixed title="订单详情">
			<mt-button slot="left" icon="back" @click="back"></mt-button>
		</mt-header>
		<div class="margin-top">
			<ul class="order-list">
				<li class="goods-list" v-for="(item, index) in list" :key="index" :data-id="item.id">
					<div class="goods-wrap">
						<div class="image">
							<img :src="item.path" class="absolute"/>
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
					<div class="goods-price">{{discount}}</div>
				</li>
				<li class="goods-total">优惠后应付&yen;{{totalPrice}}</li>
			</ul>
		</div>
		<div class="button">
			<mt-button type="primary" size="large" @click.native="submit">立即点餐</mt-button>
			<mt-button class="later" type="primary" size="large" @click.native="openPicker">预约点餐</mt-button>
		</div>
		<van-popup v-model="show" position="bottom">
			<van-datetime-picker v-model="dateVal" type="time" :min-hour="startHour" @cancel="cancel" @confirm="dateConfirm()" />
		</van-popup>
	</div>
</template>

<script>
	import { Toast } from 'mint-ui'
	import { DatetimePicker } from 'vant'
	import { axiosGet, axiosPost, accMul, getNowTime } from "../../common/publicJs/apputil.js"
	export default{
		name:'OrderDetails',
		data(){
			return{
				list:[],
				discount:'',
				totalPrice:0,
				dateVal:'',
				startHour:0,
				show:false
			}
		},
		created(){
			this.list = store.session.get('list');
			let total = 0;
			for (let item of this.list) {
				total += accMul(item.num, item.price);
			}
			//获取优惠信息
			axiosGet('user/details', {}).then((data) => {
				this.discount = data.discount;
				let discount = parseFloat(this.discount);
				if(isNaN(discount)){
					this.totalPrice = total.toFixed(2);
				}else{
					this.totalPrice = accMul(total, '0.' + discount).toFixed(2);
				}
			}).catch(function(err){
				Toast({
					message:err,
					duration:1500
				});
			});
		},
		methods:{
			//返回上一页
			back(){
				this.$router.go(-1);
			},
			//立即点餐
			submit(){
				this.createOrder(getNowTime());
			},
			//预约点餐
			openPicker(){
				this.startHour = new Date().getHours();
				this.show = true;
			},
			dateConfirm(){
				if(this.dateVal == ''){
					this.dateVal = this.startHour + ':00';
				}
				this.createOrder(getNowTime(this.dateVal));
				this.cancel();
			},
			cancel(){
				this.show = false;
			},
			//新建订单
			createOrder(time){
				let timeTemp = time.split(' ')[1];
				let hourTemp = timeTemp.split(':')[0];
				let minuteTemp = timeTemp.split(':')[1];
				let timeNow = new Date();
				let hourNow = timeNow.getHours();
				let minuteNow = timeNow.getMinutes();
				if(hourTemp == hourNow){
					if(minuteTemp < minuteNow){
						Toast({
							message:'预约时间不能小于当前时间',
							duration:1500
						});
						return;
					}
				}
				let discount = parseFloat(this.discount);
				if(isNaN(discount)){
					discount = '10.0';
				}else{
					discount = accMul(10, '0.' + discount).toFixed(1);
				}
				let params = {
					discount:discount,
					payPrice:this.totalPrice,
					dishes:this.list,
					reserveTime:time
				};
				axiosPost('order', params).then(data => {
					store.session.set('list', []);
					Toast({
						message:'点餐成功，请及时到柜台付款',
						duration:1500
					});
					let timer = setTimeout(() => {
						this.$router.replace({path:'/store-default'});
						clearTimeout(timer);
					},1500);
				}).catch(function(err){
					Toast({
						message:err,
						duration:1500
					});
				});
			}
		}
	}
</script>

<style scoped="scoped" lang="stylus" rel="stylesheet/stylus">
	@import "~common/style/icon"
	@import "~common/style/base"
	.margin-top
		width: 100%
		padding-top:1.4rem
		.order-list
			margin:0.4rem auto 1.4rem
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
				margin:0.4rem auto
				font-size:0.6rem
				text-align:right
				font-weight:700
				border-top:1px solid #f2f2f2
		.tips
			background:none
			text-align:center
			font-size: 0.5rem
			line-height:1.6rem
			color: #333	
	.button
		position:fixed
		bottom:0
		display:flex
		justify-content:space-between
		width:100%
		.mint-button
			display:block
			width:50%
			border:1px solid #26a2ff
			box-sizing:border-box
		.later
			color:#26a2ff
			background:#fff
</style>