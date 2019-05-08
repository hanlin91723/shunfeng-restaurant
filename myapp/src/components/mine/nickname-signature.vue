<template>
	<div>
		<mt-header fixed title="昵称与签名">
			<mt-button slot="left" icon="back" @click="back"></mt-button>
			<mt-button slot="right" @click="save" style="font-size: 0.46rem;">保存</mt-button>
		</mt-header>
		<div class="content">
			<form action="" ref='reg-password'>
				<div class="account-num">
					<p class="text">昵称<span>{{nicknameTips}}</span></p>
					<input type="text" placeholder="请输入昵称" maxlength="10" v-model="nickname" />
				</div>
				<div class="account-num">
					<p class="text">年龄<span>{{ageTips}}</span></p>
					<input type="text" placeholder="请输入年龄" maxlength="3" v-model="age" />
				</div>
			</form>
		</div>
	</div>
</template>

<script>
	import { axiosGet, axiosPost, ageReg, nicknameReg } from "../../common/publicJs/apputil.js"
	import { Toast } from 'mint-ui'
	export default{
		name:'NicknameSignature',
		data(){
			return{
		    	nickname:'',//昵称
				age:'',//年龄
				nicknameTips:'',//昵称提示
				ageTips:'',//年龄提示
			}
		},
		created(){
			//个人信息查询
			axiosGet('user/details', {}).then((data) => {
				this.nickname = data.nickname;
				this.age = data.age;
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
			//保存
			save(){
				let checkNicknameResult = nicknameReg(this.nickname);//验证昵称格式
				let checkAgeResult = ageReg(this.age);//验证年龄格式
				this.nicknameTips = checkNicknameResult == true ? '' : checkNicknameResult;//校验昵称格式
				this.ageTips = checkAgeResult == true ? '' : checkAgeResult;//校验年龄格式
				if(checkAgeResult == checkNicknameResult == true){
					let param = {
						nickname:this.nickname,
						age:this.age
					};
					axiosPost('user/details', param).then((data) => {
						Toast({
							message:'修改成功',
							duration:1500
						});
						let timer = setTimeout(() => {
							this.$router.go(-1);
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
	}
</script>

<style scoped="scoped" lang="stylus" rel="stylesheet/stylus">
	@import "~common/style/icon"
	@import "~common/style/base"
	@import "~common/style/form"
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
					
</style>