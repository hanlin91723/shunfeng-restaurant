cordova.define("cordova-plugin-request.request", function(require, exports, module) {

var exec = require('cordova/exec');
exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'request', 'coolMethod', [arg0]);
};
exports.post = function (url,parameters, success, error) {
    exec(success, error, 'request', 'post', [{"url":url,"parameters":parameters}]);
};
exports.get = function (url,parameters, success, error) {
    exec(success, error, 'request', 'get', [{"url":url,"parameters":parameters}]);
};
 exports.download = function (url, success, error) {
     exec(success, error, 'request', 'download', [{"url":url}]);
 };
 /*
  * 获取系统版本号
  *
  */
 exports.getVersionCode = function (arg0,success,error) {
      exec(success, error, 'request', 'getVersionCode',[{"versionCode":arg0}]);
  };
  /*
  * 获取系统版本名称
  *
  */
 exports.getVersionName= function (arg0,success,error) {
      exec(success,error,'request', 'getVersionName',[{"versionName":arg0}]);
  };
   /*
    * 更新版本
    *
    */
   exports.updateAppVersion= function (url,versionSize,success,error) {
        exec(success,error,'request', 'updateAppVersion',[{"url":url,"versionSize":versionSize}]);
    };
/*
 *cordova.plugins.request.Updata();
 * url
 * parameters 参数map
 * filepaths 上传文件的路径list
 */
exports.uploadimage = function(url,parameters,filepaths,isoriginal,success,error) {
               exec(success, error, 'request', 'uploadimage', [{"url":url,"parameters":parameters,"filePaths":filepaths,"isoriginal":isoriginal}]);
};



});
