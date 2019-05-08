import Vue from 'vue'
import Vuex from 'vuex'
Vue.use(Vuex)

const Title= {
	state:{
	  title:'修改密码',
	},
	mutations: {
	    setTitle (state,title) {
	      state.title= title
	    }
	},
	actions: {
	  	setTitle (context,title) {
	    	context.commit('setTitle',title)
	  	}
	},
	getter:{
	
	}
}

export default new Vuex.Store({
	modules: {
	    Title: Title
	}
})

