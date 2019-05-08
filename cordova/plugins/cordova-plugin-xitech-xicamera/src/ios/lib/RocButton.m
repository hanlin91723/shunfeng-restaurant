//
//  RocButton.m
//  视频处理
//
//  Created by roc on 2017/12/25.
//  Copyright © 2017年 lee. All rights reserved.
//

#import "RocButton.h"
@interface RocButton()

@property(nonatomic,strong)UIView *buttonView;  //button

@property(nonatomic,assign)BOOL canRecordVideo; //是否可以录制视频

@end

@implementation RocButton

//初始化类方法
+(instancetype)createButtonWithFrame:(CGRect)frame canRecordVideo:(BOOL)canRecordVideo{
    
    RocButton *button  =[[RocButton alloc]initWithFrame:frame];
    button.canRecordVideo = canRecordVideo;
    
    return button;
}
//初始化方法
- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self initUI];
    }
    return self;
}

//初始化UI界面
-(void)initUI{
    self.backgroundColor = [UIColor clearColor];

    //大视图
    UIView *bgView = [[UIView alloc]initWithFrame:CGRectMake(self.frame.size.width/2-self.frame.size.height/2, 0, self.frame.size.height, self.frame.size.height)];
    bgView.backgroundColor = [UIColor colorWithRed:137/255.0 green:216/255.0 blue:174/255.0 alpha:0.8];
    bgView.layer.cornerRadius = bgView.frame.size.width/2;
    bgView.layer.masksToBounds = NO;
    [self addSubview:bgView];
    
    //小视图
    UIView *smView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, bgView.frame.size.width*0.8, bgView.frame.size.height*0.8)];
    smView.center = bgView.center;
    smView.backgroundColor = [UIColor colorWithRed:48/255.0 green:199/255.0 blue:116/255.0 alpha:1.0];
    smView.layer.cornerRadius = smView.frame.size.width/2;
    smView.layer.masksToBounds = NO;
    [self addSubview:smView];
    
    _buttonView = smView;
    
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(tapClick:)];
    [smView addGestureRecognizer:tap];
    

}

//重写canRecordVideo的set方法
-(void)setCanRecordVideo:(BOOL)canRecordVideo{
    _canRecordVideo = canRecordVideo;
    NSLog(@"%@", _canRecordVideo ?@"YES" : @"No");
    if (_canRecordVideo) {  //可以录制视频，允许长按手势
        UILongPressGestureRecognizer *longPressGr = [[UILongPressGestureRecognizer alloc]initWithTarget:self action:@selector(longPressClick:)];
        [_buttonView addGestureRecognizer:longPressGr];
    }else{                  //不可以录制视频。不允许长按手势

    }
}

//单击事件
-(void)tapClick:(id)sender{
    UITapGestureRecognizer *tap = (UITapGestureRecognizer*)sender;
    if ([_delegate respondsToSelector:@selector(singleClick:)]) {
        [_delegate singleClick:tap];
    }
  
}
//长按事件
-(void)longPressClick:(id)sender{
    
    UILongPressGestureRecognizer *longPress = (UILongPressGestureRecognizer *)sender;
    switch (longPress.state) {
        case UIGestureRecognizerStateBegan://长按开始
        {
            if ([_delegate respondsToSelector:@selector(longPressStateBegan:)]) {
                [_delegate longPressStateBegan:longPress];
            }
        }
            break;
        case UIGestureRecognizerStateEnded://长按结束
        {
            if ([_delegate respondsToSelector:@selector(longPressStateEnded:)]) {
                [_delegate longPressStateEnded:longPress];
            }
        }
            break;
        case UIGestureRecognizerStateChanged://长按变化
        {
            if ([_delegate respondsToSelector:@selector(longPressStateChanged:)]) {
                [_delegate longPressStateChanged:longPress];
            }
        }
            break;
        default:
            break;
    }
}



@end
