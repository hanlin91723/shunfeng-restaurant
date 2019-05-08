cordova.define("cordova-plugin-xitech-xicamera.XiCamera", function(require, exports, module) {

var exec = require('cordova/exec');

exports.showCamera = function (arg0, success, error) {
    exec(success, error, 'XiCamera', 'showCamera', [arg0]);
};

});


