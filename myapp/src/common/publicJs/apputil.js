import Vue from 'vue'
import axios from 'axios'
import qs from 'qs'
import { Store } from 'vuex';

//axios配置

//全局的 axios 默认值:指定将被用在各个请求的配置默认值
// 添加请求拦截器
axios.interceptors.request.use(function (config) {
    return config
}, function (error) {
    return Promise.reject(error)
});
// 添加响应拦截器
axios.interceptors.response.use(function (response) {
    return response;
}, function (error) {
    return Promise.reject(error)
});

let cancelArr = [];
export { cancelArr }

//后台get请求
export function axiosGet(url, param){
	let axiosTemp = axios.create({
		baseURL: process.env.API_ROOT,
		timeout: 10000,
		cancelToken : new axios.CancelToken(cancel => {
			cancelArr.push({cancel})
		}),
		headers:{
			'Content-Type': 'application/json;charset=UTF-8',
			'headerUserId': store.session.get('userId') || ''
		}
	});
	return new Promise(function(resolve, reject){
		axiosTemp.get(url, {
			params:param
		}).then(response => {
			let res = response.data;
			if(res.status == 200){
				resolve(res.data);
			}else{
				reject(res.msg);
			}
		}).catch(error => {
			let err = error.message.indexOf('timeout') != -1 ? '请求超时' : '通讯异常';
			reject(err);
		});
	});	
}
//后台post请求
export function axiosPost(url, params){
	let axiosTemp = axios.create({
		baseURL: process.env.API_ROOT,
		timeout: 10000,
		cancelToken : new axios.CancelToken(cancel => {
			cancelArr.push({cancel})
		}),
		headers:{
			'Content-Type': 'application/json;charset=UTF-8',
			'headerUserId': store.session.get('userId') || ''
		}
	});
	return new Promise(function(resolve, reject){
		axiosTemp.post(url, params).then(response => {
			let res = response.data;
			if(res.status == 200){
				resolve(res.data);
			}else{
				reject(res.msg);
			}
		}).catch(error => {
			let err = error.message.indexOf('timeout') != -1 ? '请求超时' : '通讯异常';
			reject(err);
		});
	});	
}

//基于hammer.js 触摸屏事件
function vueTouch(el, type, binding){  
    this.el = el;  
    this.type = type;  
    this.binding = binding;  
    //直接调用  hammer.js方法
    var hammertime = new Hammer(this.el);  
    hammertime.on(this.type, this.binding.value);  
};  

//触摸点击事件
export const tap = Vue.directive("tap",{  
    bind:function(el,binding){  
        new vueTouch(el,"tap",binding);  
    }  
});  
//左滑事件
export const swipeleft = Vue.directive("swipeleft",{  
    bind:function(el,binding){  
        new vueTouch(el,"swipeleft",binding);  
    }  
});  
//右滑事件 
export const swiperight = Vue.directive("swiperight",{  
    bind:function(el,binding){  
        new vueTouch(el,"swiperight",binding);  
    }  
});  
//长按事件  
export const press = Vue.directive("press",{  
    bind:function(el,binding){  
        new vueTouch(el,"press",binding);  
    }  
});

//数值小于10时前面加零校验
function inspectionTime(num){
	num = num < 10 ? '0' + num : num;
	return num;
}

//转换时间格式为YYYY-MM-DD hh:mm:ss
function convertTimeFormat(date, time){
	let Y = inspectionTime(date.getFullYear());
  	let M = inspectionTime(date.getMonth() + 1);
	let D = inspectionTime(date.getDate());
	let s = inspectionTime(date.getSeconds());
	if(time){
		return Y + '-' + M + '-' + D + ' ' + time + ':' + s;
	}else{
		let h = inspectionTime(date.getHours());
		let m = inspectionTime(date.getMinutes());
		return Y + '-' + M + '-' + D + ' ' + h + ':' + m + ':' + s;
	}
}

//获取YYYY-MM-DD hh:mm:ss格式的当前北京时间
export function getNowTime(time){
	let timezone = 8; //目标时区时间，东八区
	let offset_GMT = new Date().getTimezoneOffset(); // 本地时间和格林威治的时间差，单位为分钟
	let nowDate = new Date().getTime(); // 本地时间距 1970 年 1 月 1 日午夜（GMT 时间）之间的毫秒数
	let date = new Date(nowDate + offset_GMT * 60 * 1000 + timezone * 60 * 60 * 1000);
	return convertTimeFormat(date, time);
}

//比较两个日期大小
function compare(date1, date2) {
	let dates1 = new Date(date1);
	let dates2 = new Date(date2);
	if (dates1 > dates2) {
		return true
	} else {
		return false
	}
}

//解决小数相乘进制bug
export function accMul(arg1, arg2){
	let m = 0,
	s1 = arg1.toString(),
	s2 = arg2.toString();
    try{
    	m += s1.split(".")[1].length
    }catch(e){
    	
    }
    try{
    	m += s2.split(".")[1].length
    }catch(e){
    	
    }
    return Number(s1.replace(".","")) * Number(s2.replace(".","")) / Math.pow(10,m);
}

//获取图片
export function getImage(imageId){
	return imageSever + getClientToken() +'&userId='+imageId;
}

//禁止长按图片出现放大镜
//window.onload=function(){
//  document.documentElement.style.webkitTouchCallout='none';
//};


//账号规则验证
export function telReg(accountNum){
	if(accountNum){
		let RegExp = /^[1][3,4,5,6,7,8,9][0-9]{9}$/;
		if(RegExp.test(accountNum)){
			return true
		}else{
			return "手机号格式错误";
		}
	}else{
		return "手机号不能为空";
	}
}
//年龄验证
export function nicknameReg(nickname){
	if(nickname){
		return true
	}else{
		return "年龄不能为空";
	}
}
//年龄验证
export function ageReg(age){
	if(age){
		let RegExp = /^[0-9]{0,3}$/;
		if(RegExp.test(age) && age < 200 ){
  			return true
  		}else{
  			return "年龄格式错误";
  		}
	}else{
		return "年龄不能为空";
	}
}
//密码规则验证
export function pwdReg(password){
	if(password){
		let week = /^[a-zA-Z]{8,16}$/;//弱:8位 纯字母
		let weeks = /^[0-9]{8,16}$/;//弱:8位 纯数字
	    let middle = /^(?!\d+$)(?![a-zA-Z]+$)[\dA-Za-z]{8,16}$/;//中:8-16位 数字+字母
	    let strongest = /^(?=.*((?=[\x21-\x7e]+)[^A-Za-z0-9]))(?=.*[a-zA-Z])(?=.*[0-9])[^\u4e00-\u9fa5]{8,16}$/;//强:8-16位 数字+字母+字符
  		if(week.test(password) || weeks.test(password) || middle.test(password) || strongest.test(password)){
  			return true
  		}else{
  			return "密码不符合要求";
  		}
	}else{
		return "密码不能为空";
	}
}
//二次密码规则验证
export function secondpswReg(password1, password2){
	if(password2){
  		if(password1 == password2){
  			return true
  		}else{
  			return "密码不一致";
  		}
  	}else{
  		return "密码不能为空";
  	}
}