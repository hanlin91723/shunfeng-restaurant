<template>
  <div>
		<mt-header fixed title="注册">
	    <mt-button slot="right" @click="toLogin" style='font-size:0.48rem'>登录</mt-button>
		</mt-header>
	  <div class="content">
			<form action="" ref='reg-password'>
				<div class="account-num">
		  		<p class="text">手机号<span>{{phoneNumberTips}}</span></p>
		  		<input type='tel' placeholder="请输入手机号" maxlength="11" v-model="phoneNumber" />
				</div>
				<div class="account-num">
		  		<p class="text">密码<span>{{firstPasswordTips}}</span></p>
		  		<input :type="firstType" placeholder="请输入8位以上的数字或字母" maxlength="16" v-model="password1" />
		  		<div :class="[firstStatus ? 'icon-eye-close' : 'icon-eye-open', 'icon']" @click="changeFirstStatus"></div>
		  	</div>
		  	<div class="account-num">
		  		<p class="text">重复密码<span>{{secondPasswordTips}}</span></p>
		  		<input :type="secondType" placeholder="请输入8位以上的数字或字母" maxlength="16" v-model="password2" />
		  		<div :class="[secondStatus ? 'icon-eye-close' : 'icon-eye-open', 'icon']" @click="changeSecondStatus"></div>
		  	</div>
				<div class="account-num">
		  		<p class="text">昵称<span>{{nicknameTips}}</span></p>
		  		<input type="text" placeholder="请输入昵称" maxlength="10" v-model="nickname" />
		  	</div>
		  	<div class="account-num">
		  		<p class="text">年龄<span>{{ageTips}}</span></p>
		  		<input type="text" placeholder="请输入年龄" maxlength="3" v-model="age" />
		  	</div>
			</form>
			<div class="button">
				<mt-button type="primary" size="large" @click.native="submit">确定</mt-button>
			</div>
		</div>
  </div>
</template>

<script>
	import { Toast, Indicator } from 'mint-ui'
	import { telReg, pwdReg, codeReg, secondpswReg, axiosPost, ageReg, nicknameReg } from "../../common/publicJs/apputil.js"
	export default {
		name: 'Register',
		data () {
	    return {
	    	phoneNumber:'',//手机号
	    	password1:'',//第一个密码
				password2:'',//第二个密码
				nickname:'',//昵称
	    	age:'',//年龄
	    	firstType:'password',//第一个密码输入框类型
	    	secondType:'password',//第二个密码输入框类型
	    	phoneNumberTips:'',//手机号提示
	    	firstPasswordTips:'',//第一个密码提示
				secondPasswordTips:'',//第二个密码提示
				nicknameTips:'',//昵称提示
	    	ageTips:'',//年龄提示
	    	firstStatus:true,//切换第一个密码输入框类型
	    	secondStatus:true//切换第二个密码输入框类型
	    }
		},
  	methods:{
  		toLogin(){
				this.$router.replace({path:'/login-password'});
			},
  		//切换密码输入框类型
  		changeFirstStatus(){
  			this.firstStatus = !this.firstStatus;
  			this.firstType = this.firstStatus ? 'password' : 'text';
  		},
  		changeSecondStatus(){
  			this.secondStatus = !this.secondStatus;
  			this.secondType = this.secondStatus ? 'password' : 'text';
  		},
	  	//点击确定
	  	submit(){
	  		store.remove('userId');//删除存储userId
	  		let _this = this;
	  		let checkPhoneResult = telReg(_this.phoneNumber);//验证手机号格式
	  		let checkPwd1Result = pwdReg(_this.password1);//验证第一个密码格式
				let checkPwd2Result = secondpswReg(_this.password1, _this.password2);//验证第二个密码格式
				let checkNicknameResult = nicknameReg(_this.nickname);//验证昵称格式
				let checkAgeResult = ageReg(_this.age);//验证年龄格式
	  		_this.phoneNumberTips = checkPhoneResult == true ? '' : checkPhoneResult;//校验手机号格式
	  		_this.firstPasswordTips = checkPwd1Result == true ? '' : checkPwd1Result;//校验第一个密码格式
				_this.secondPasswordTips = checkPwd2Result == true ? '' : checkPwd2Result;//校验第二个密码格式
				_this.nicknameTips = checkNicknameResult == true ? '' : checkNicknameResult;//校验昵称格式
				_this.ageTips = checkAgeResult == true ? '' : checkAgeResult;//校验年龄格式
	  		//手机号、密码格式均正确
	  		if(checkPhoneResult == checkPwd1Result == checkPwd2Result == checkAgeResult == checkNicknameResult == true){
	  			let param = {
						loginName:_this.phoneNumber,
						pwd:_this.password1,
						nickname:_this.nickname,
						age:_this.age
					};
					Indicator.open({
						text: '注册中...',
						spinnerType: 'fading-circle'
					});
					let times=setTimeout(function(){
						axiosPost('regist', param).then(function(data){
							Indicator.close();
							Toast({
								message:'注册成功',
								duration:1500
							});
							//待Toast关闭后跳转
							let closeToast = setTimeout(function(){
								clearTimeout(closeToast);
								_this.$router.replace({path:'/login-password'});
							},1500);
		      	}).catch(function(err){
		      		Indicator.close();
							Toast({
								message:err,
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
		.button
			padding:0.4rem
</style>
