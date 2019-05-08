import Vue from 'vue'
import Router from 'vue-router'
import LoginPassword from '@/components/login/login-password'
import Register from '@/components/login/register'
import MineDefault from '@/components/mine/mine-default'
import NicknameSignature from '@/components/mine/nickname-signature'
import GoodsList from '@/components/orderForm/goods-list'
import StoreDefault from '@/components/store/store-default'
import OrderDetails from '@/components/store/order-details'

Vue.use(Router)
export default new Router({
  routes: [
    {
      path: '/',
      component:LoginPassword
    },
    {
    	path:'/login-password',
    	name:'LoginPassword',
    	component:LoginPassword
    },
    {
    	path:'/register',
    	name:'Register',
    	component:Register
    },
    {
    	path:'/mine-default',
    	name:'MineDefault',
    	component:MineDefault
    },
    {
    	path:'/nickname-signature',
    	name:'NicknameSignature',
    	component:NicknameSignature
		},
	{
		path:'/goods-list',
		name:'GoodsList',
		component:GoodsList
	},
	{
    	path:'/store-default',
    	name:'StoreDefault',
    	component:StoreDefault
	},
	{
    	path:'/order-details',
    	name:'OrderDetails',
    	component:OrderDetails
    }
  ]
})
