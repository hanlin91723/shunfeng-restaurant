#!/bin/bash

echo "begin to prepare cordova"
mkdir -p cordova/www/src
mkdir -p cordova/www/src/common/js
cp -rf myapp/src/common/js/* cordova/www/src/common/js/ 

#
cp -rf cordova/www cordova-ios/
cd cordova-ios
cordova prepare ios
echo "finish to prepare cordova"
cd ..
