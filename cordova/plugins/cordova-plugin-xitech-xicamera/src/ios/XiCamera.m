/********* XiCamera.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import "XiCamera.h"

@interface XiCamera()

@property(nonatomic,strong)NSString * callbackId;

-(NSString *)getSizeDes:(unsigned long)size;

@end

@implementation XiCamera

-(NSString *)getSizeDes:(unsigned long)size
{
    NSString *fileSizeString ;
    if (size < 1024) {
        fileSizeString = [NSString stringWithFormat:@"%lu B",size];
    } else if (size < 1048576) {
        float f=size / 1024.0;
        fileSizeString = [NSString stringWithFormat:@"%.2f KB",f];
    } else if (size < 1073741824) {
        float f=size / 1048576.0;
        fileSizeString = [NSString stringWithFormat:@"%.2f MB",f];
    } else {
        float f=size / 1073741824.0;
        fileSizeString = [NSString stringWithFormat:@"%.2f GB",f];
    }
    return fileSizeString;
}

-(void)showCamera:(CDVInvokedUrlCommand *)command{
    
    RocCameraViewController *cameraVC  = [RocCameraViewController createRocCameraVCWithCanRecordVideo:NO];
    cameraVC.delegate = self;
    UIViewController *tt = [UIApplication sharedApplication].keyWindow.rootViewController;
    [tt presentViewController:cameraVC animated:YES completion:nil];
    
    self.callbackId = command.callbackId;
    
}
-(void)getImagePath:(NSString *)imagePath oriLength:(unsigned long)oriLength smallPath:(NSString *)smallPath smallLength:(unsigned long)smallLength{

    
    NSDictionary *dic= @{@"oldFilePath":imagePath,@"oldFileSize":[self getSizeDes:oriLength],@"tempFilePath":smallPath,@"tempFileSize":[self getSizeDes:smallLength]};
    
    CDVPluginResult  *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dic];
    NSLog(@"camera param :%@",dic);
    
    [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
    
}

@end

