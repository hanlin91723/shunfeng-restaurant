//
//  RocCamer.h
//  HelloCordova
//
//  Created by roc on 2017/12/20.
//

#import <Cordova/CDVPlugin.h>
#import "RocCameraViewController.h"

@interface XiCamera : CDVPlugin<RocCameraDelegate>

@property(nonatomic,strong)RocCameraViewController *cameraVC;


- (void)showCamera:(CDVInvokedUrlCommand*)command;

@end

