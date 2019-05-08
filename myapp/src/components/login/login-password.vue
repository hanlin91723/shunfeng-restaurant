<template>
  <div>
  	<mt-header fixed title="登录">
	    <mt-button slot="right" @click="toRegister" style='font-size:0.48rem'>注册</mt-button>
		</mt-header>
  	<div class="content">
			<form action="" ref='reg-password'>
				<div class="account-num">
		  		<p class="text">手机号<span>{{phoneNumberTips}}</span></p>
		  		<input type='tel' placeholder="请输入手机号" v-model="phoneNumber" />
				</div>
		  	<div class="account-num">
		  		<p class="text">登录密码<span>{{passwordTips}}</span></p>
		  		<input :type="type" placeholder="请输入8位以上的数字或字母" maxlength="16" v-model="password" autocomplete="new-password"/>
		  		<div :class="[status ? 'icon-eye-close' : 'icon-eye-open', 'icon']" @click="changeStatus"></div>
		  	</div>
			</form>
			<div class="button">
				<mt-button type="primary" size="large" @click.native="submit">登录</mt-button>
			</div>
		</div>
  </div>
</template>

<script>
	import { Toast, Indicator } from 'mint-ui'
	import { telReg, pwdReg, axiosPost } from "../../common/publicJs/apputil.js"
	export default {
		name: 'LoginPassword',
		data () {
	    return {
	    	phoneNumber:'',//手机号
	    	password:'',//登录密码
	    	type:'password',//密码输入框类型
	    	phoneNumberTips:'',//手机号提示
	    	passwordTips:'',//登录密码提示
	    	status:true//切换密码输入框类型
	    }
		},
  	created() {
  		this.phoneNumber = store.get('phoneNumber') || '';
    },
  	methods:{
  		toRegister(){
  			this.$router.replace({path:'/register'});
  		},
  		//切换密码输入框类型
  		changeStatus(){
  			this.status = !this.status;
  			this.type = this.status ? 'password' : 'text';
  		},
	  	//点击登录
	  	submit(){
	  		store.remove('userId');//删除存储userId
	  		let _this = this;
	  		let checkPhoneResult = telReg(_this.phoneNumber);//验证手机号格式
	  		let checkPwdResult = pwdReg(_this.password);//验证密码格式
	  		_this.phoneNumberTips = checkPhoneResult == true ? '' : checkPhoneResult;//校验手机号格式
	  		_this.passwordTips = checkPwdResult == true ? '' : checkPwdResult;//校验密码格式
	  		//手机号、密码格式均正确
	  		if(checkPhoneResult == true && checkPwdResult == true){
	  			let param = {
						loginName:_this.phoneNumber,
						pwd:_this.password
					};
					Indicator.open({
					  text: '登录中...',
					  spinnerType: 'fading-circle'
					});
					let times = setTimeout(function(){
						axiosPost('login', param).then(function(data){
							store.set('phoneNumber',_this.phoneNumber);//存储手机号
							store.session.set('userId',data.id);//存储userId
							store.session.set('list',[]);//初始化已选菜品
							Indicator.close();
							Toast({
								message:'登录成功',
								duration:1500
							});
							//待Toast关闭后跳转
							let closeToast = setTimeout(function(){
								clearTimeout(closeToast);
								_this.$router.replace({path:'/store-default'});
							},1500);
		      	}).catch(function(error){
							Indicator.close();
							Toast({
								message:error,
								duration:1500
							});
		       });
	        	clearTimeout(times);
			    },1000);
	  		}
	  	}
  	}
	}
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped="scoped" lang="stylus" rel="stylesheet/stylus">
	@import "~common/style/icon"
	@import "~common/style/form"
	@import "~common/style/base"
	.content
		position:absolute
		width:100%
		top:1.4rem
		bottom:0
		padding:0.4rem 0
		background:#f2f2f2
		form
			padding-top:0.4rem
			background:#fff
			input::-webkit-input-placeholder
				font-size:0.46rem
				color:#333
			.account-num .getCode
				color:#1574f3
				background:#f2f8ff
		.tools
			display:flex
			justify-content:space-around
			width:50%
			margin:auto
			font-size:0.36rem
			color:#666
			p
				width:2rem
			p:first-child
				text-align:right
			.vertical-line
				width:1px
				background:#bbb
		.button
			padding:0.4rem
</style>
