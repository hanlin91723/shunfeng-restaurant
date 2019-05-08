cordova.define("cordova-plugin-signData.signData", function(require, exports, module) { cordova.define("cordova-plugin-signData.signData", function(require, exports, module) {

var exec = require('cordova/exec');
var signFunc = function(){};
exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'signData', 'coolMethod', [arg0]);
};

//arg0 格式 userid  "signdata由请求得到",
signFunc.prototype.signtodata = function(userid,accountid,psd,signdata, success, error) {
               exec(success, error, 'signData', 'signToData', [{"userid":userid,"accountid":accountid,"psd":psd,"signdata":signdata}]);
};
//创建钱包文件
signFunc.prototype.buildWallet = function(arg0, success, error) {
    exec(success, error, 'signData', 'buildWallet', [arg0]);
};
//设置密码
//userid  用户id
//oldPsd老密码 第一次设置可为空
//newPsd新密码
signFunc.prototype.changeWalletPsd = function(userid,oldPsd,newPsd, success, error) {
               exec(success, error, 'signData', 'changeWalletPsd', [{"userid":userid,"oldPsd":oldPsd,"newPsd":newPsd}]);
};
//获取钱包内容
signFunc.prototype.getwallet = function(userid,success, error) {
               exec(success, error, 'signData', 'getwallet', [{"userid":userid}]);
};
//导出并备份钱包
  signFunc.prototype.exportWallet = function(userid,password,success,error) {
                 exec(success, error, 'signData', 'exportWallet', [{"userid":userid,"password":password}]);
  };
  //检验密码的正确性
  signFunc.prototype.checkeWalletPassword = function(userid,password,success,error) {
                 exec(success, error, 'signData', 'checkeWalletPassword', [{"userid":userid,"password":password}]);
  };
//导入钱包
signFunc.prototype.importWallet = function(userid,password,wallpath,success,error) {
               exec(success, error, 'signData', 'importWallet', [{"userid":userid,"password":password,"wallpath":wallpath}]);
};
//选择钱包文件
signFunc.prototype.selectWallet = function(userid,success,error) {
               exec(success, error, 'signData', 'selectWallet', [{"userid":userid}]);
};

signFunc.prototype.getCSR = function(accounId,success,error) {
               exec(success, error, 'signData', 'getCSR', [{"accountId":accounId}]);
};
var SIGNFUNC = new signFunc();
module.exports = SIGNFUNC; 

});

});
