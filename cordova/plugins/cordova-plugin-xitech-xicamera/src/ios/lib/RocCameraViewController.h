//
//  RocCameraViewController.h
//  视频处理
//
//  Created by roc on 2017/12/26.
//  Copyright © 2017年 lee. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol RocCameraDelegate<NSObject>

@optional
//获取拍照的照片的路径(单张照片)
-(void)getImagePath:(NSString *)imagePath oriLength:(unsigned long)oriLength smallPath:(NSString *)smallPath smallLength:(unsigned long)smallLength;

@end

@interface RocCameraViewController : UIViewController

/**
 *  @brief 初始化相机
 *  @param canRecordVideo 是不是能够录制视频，YES能录制视频，NO不能够录制视频
 *  @return RocCameraViewController 对象
 */

+(instancetype)createRocCameraVCWithCanRecordVideo:(BOOL)canRecordVideo;

- (UIImage *) scaleImage:(UIImage *)image toScale:(float)scaleSize ;
@property(nonatomic,weak)id<RocCameraDelegate>delegate;

@end
