// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import router from './router'
import MintUI from 'mint-ui'
import 'mint-ui/lib/style.css'
import Vant from 'vant'
import 'vant/lib/index.css'
import App from './App'
Vue.use(MintUI)
import { Loadmore, Button, Header } from 'mint-ui'
Vue.component(Header.name, Header, Loadmore.name, Loadmore, Button.name, Button)
import { DatetimePicker, Popup } from 'vant'
Vue.use(DatetimePicker).use(Popup)

import rem from './common/publicJs/rem.js'
import { cancelArr } from './common/publicJs/apputil.js'
// 路由切换
router.beforeEach((to, from, next) => {
	cancelArr.forEach((ele,index)=>{
		ele.cancel()
		cancelArr.splice(index,1)
	})
	next()
})


import store from './store/store.js'
Vue.config.productionTip = false
/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,//使用vuex的store
  components: { App },
  template: '<App/>'
})