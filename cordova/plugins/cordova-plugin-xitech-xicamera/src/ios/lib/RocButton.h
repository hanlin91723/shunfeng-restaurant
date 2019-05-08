//
//  RocButton.h
//  视频处理
//
//  Created by roc on 2017/12/25.
//  Copyright © 2017年 lee. All rights reserved.
//

#import <UIKit/UIKit.h>
@protocol RocButtonDelegate<NSObject>

//按钮单击
-(void)singleClick:(UITapGestureRecognizer *)sender;

@optional
//长按开始
-(void)longPressStateBegan:(UILongPressGestureRecognizer *)sender;
//长按变化
-(void)longPressStateChanged:(UILongPressGestureRecognizer *)sender;
//长按结束
-(void)longPressStateEnded:(UILongPressGestureRecognizer *)sender;
@end

@interface RocButton : UIView

/**
 *  @brief 初始化相机拍照按钮
 *  @param frame 按钮的尺寸，宽度必须为屏幕的宽
 *  @param canRecordVideo 是不是能够录制视频，YES能录制视频，NO不能够录制视频
 *  @return RocButton 对象
 */
+(instancetype)createButtonWithFrame:(CGRect)frame canRecordVideo:(BOOL)canRecordVideo;

@property(nonatomic,weak)id<RocButtonDelegate>delegate;

@end
