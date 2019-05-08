//
//  RocCameraViewController.m
//  视频处理
//
//  Created by roc on 2017/12/26.
//  Copyright © 2017年 lee. All rights reserved.
//

#import "RocCameraViewController.h"
#import <AVFoundation/AVFoundation.h>
#import <AssetsLibrary/AssetsLibrary.h>
#import <CoreMotion/CoreMotion.h>
#import "RocButton.h"


@interface RocCameraViewController ()<RocButtonDelegate>

@property(nonatomic,assign)BOOL canRecordVideo;//是否能录像

@property(nonatomic,strong)AVCaptureSession *session;

@property(nonatomic,strong)AVCaptureDeviceInput *vedioInput;

@property(nonatomic,strong)AVCaptureDevice *device;

@property(nonatomic,strong)AVCaptureStillImageOutput *stillImageOutPut;

@property(nonatomic,strong)AVCaptureVideoPreviewLayer *proviewLayer;

@property(nonatomic,strong)UIImageView *imageV;  //拍照后展示的imageview

@property (nonatomic)UIView *focusView;//聚焦的动画

@property(nonatomic,strong)CMMotionManager  *motionManager; //获取手机方向

@property(nonatomic,assign)BOOL isUsingFrontFacingCamera;//前置后置相机
@end

@implementation RocCameraViewController

+(instancetype)createRocCameraVCWithCanRecordVideo:(BOOL)canRecordVideo{
    
    RocCameraViewController *cammera = [[RocCameraViewController alloc]init];
    cammera.canRecordVideo = canRecordVideo;
    return cammera;
    
}

-(void)viewWillAppear:(BOOL)animated{
    
    [super viewWillAppear:animated];
    
    BOOL isAuthoriza = [self getAuthorization];
    if (isAuthoriza) { //取得相机的权限
        
        [self initCapturePicture];
        [self initUI];
        
    }else{//未取得相机的权限
        
        [self initUI];
        [self showAlertView];
        //[self noAuthorizationUI];
        return;
    }
    
    if (self.session) {
        [self.session startRunning];
    }
}

-(void)viewDidDisappear:(BOOL)animated{
    
    [super viewDidDisappear:animated];
    if (self.session) {
        [self.session stopRunning];
    }
    
    [self.motionManager stopAccelerometerUpdates];
}
- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
}

//判断是否获得的相机的权限
-(BOOL)getAuthorization{
    
    AVAuthorizationStatus authStatus = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];
    
    if (authStatus == AVAuthorizationStatusRestricted || authStatus ==AVAuthorizationStatusDenied){ //无权限
        
        return false;
        
    }else{  //有权限
        return true;
    }
    
}

//初始化拍照功能
-(void)initCapturePicture{
    
    self.device = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
    
    _session = [[AVCaptureSession alloc]init];
    NSError *error;
    AVCaptureDevice *device = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
    
    //    [device lockForConfiguration:nil];
    //    [device setFlashMode:AVCaptureFlashModeAuto];
    //    [device unlockForConfiguration];
    
    _vedioInput = [[AVCaptureDeviceInput alloc]initWithDevice:device error:&error];
    if (error) {
        NSLog(@"%@",error);
    }
    
    _stillImageOutPut = [[AVCaptureStillImageOutput alloc]init];
    
    NSDictionary *outputSettings =[ [NSDictionary alloc]initWithObjectsAndKeys:AVVideoCodecJPEG,AVVideoCodecKey, nil];
    
    [_stillImageOutPut setOutputSettings:outputSettings];
    
    if ([_session canAddInput:_vedioInput]) {
        [_session addInput:_vedioInput];
    }
    if ([_session canAddOutput:_stillImageOutPut]) {
        [_session addOutput:_stillImageOutPut];
    }
    
    _proviewLayer = [[AVCaptureVideoPreviewLayer alloc]initWithSession:_session];
    [_proviewLayer setVideoGravity:AVLayerVideoGravityResizeAspect];
    _proviewLayer.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height);
    [self.view.layer addSublayer:_proviewLayer];
    
    _isUsingFrontFacingCamera = NO;
}

//初始化UI界面
-(void)initUI{
    
    //拍照的按钮
    RocButton *button = [RocButton createButtonWithFrame:CGRectMake(0, self.view.frame.size.height-100, self.view.frame.size.width, 60) canRecordVideo:self.canRecordVideo];
    button.delegate = self;
    [self.view addSubview:button];
    
    
    //取消的按钮
    UIView *canelBtn = [self drawXbutton:CGRectMake(self.view.frame.size.width-40, 30, 20, 20)];
    [self.view addSubview:canelBtn];
    
    //摄像头切换的按钮
    UIButton *switchBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    switchBtn.frame = CGRectMake(20, 30, 30, 30);
    [switchBtn setBackgroundImage:[UIImage imageNamed:@"switchCamera@2X.png"] forState:UIControlStateNormal];
    [switchBtn addTarget:self action:@selector(switchCamera:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:switchBtn];
    
    //聚焦的动画
    _focusView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, 80, 80)];
    _focusView.layer.borderWidth = 1.0;
    _focusView.layer.borderColor =[UIColor whiteColor].CGColor;
    _focusView.backgroundColor = [UIColor clearColor];
    [self.view addSubview:_focusView];
    _focusView.hidden = YES;
    
    //初始化全局管理对象
    self.motionManager = [[CMMotionManager alloc] init];
    //如果CMMotionManager的支持获取陀螺仪数据
    if (self.motionManager.deviceMotionAvailable) {
        [self.motionManager startAccelerometerUpdates];
    }else{
        NSLog(@"该设备不支持获取陀螺仪数据！");
    }
    
    
    //相机屏幕的点击事件
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(screenTouch:)];
    [self.view addGestureRecognizer:tap];
    
}
//画X按钮
-(UIView *)drawXbutton:(CGRect)frame{
    UIView *view = [[UIView alloc]initWithFrame:frame];
    view.backgroundColor = [UIColor clearColor];
    view.layer.cornerRadius = view.frame.size.width/2;
    view.layer.masksToBounds = YES;
    
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(backClick)];
    [view addGestureRecognizer:tap];
    
    //线的路径
    UIBezierPath *linePath = [UIBezierPath bezierPath];
    // 起点
    [linePath moveToPoint:CGPointMake(0, 0)];
    // 其他点
    [linePath addLineToPoint:CGPointMake(view.frame.size.width, view.frame.size.height)];
    
    
    CAShapeLayer *lineLayer = [CAShapeLayer layer];
    
    lineLayer.lineWidth = 2;
    lineLayer.strokeColor = [UIColor whiteColor].CGColor;
    lineLayer.path = linePath.CGPath;
    lineLayer.fillColor = nil; // 默认为blackColor
    
    //线的路径
    UIBezierPath *linePath1 = [UIBezierPath bezierPath];
    // 起点
    [linePath1 moveToPoint:CGPointMake(view.frame.size.width, 0)];
    // 其他点
    [linePath1 addLineToPoint:CGPointMake(0, view.frame.size.height)];
    
    
    CAShapeLayer *lineLayer1 = [CAShapeLayer layer];
    
    lineLayer1.lineWidth = 2;
    lineLayer1.strokeColor = [UIColor whiteColor].CGColor;
    lineLayer1.path = linePath1.CGPath;
    lineLayer1.fillColor = nil; // 默认为blackColor
    
    [view.layer addSublayer:lineLayer1];
    [view.layer addSublayer:lineLayer];
    return view;
}
//相机转换按钮
-(void)switchCamera:(UIButton *)sender{
    AVCaptureDevicePosition desiredPosition;
    if (_isUsingFrontFacingCamera){
        desiredPosition = AVCaptureDevicePositionBack;
    }else{
        desiredPosition = AVCaptureDevicePositionFront;
    }
    
    for (AVCaptureDevice *d in [AVCaptureDevice devicesWithMediaType:AVMediaTypeVideo]) {
        if ([d position] == desiredPosition) {
            [_proviewLayer.session beginConfiguration];
            AVCaptureDeviceInput *input = [AVCaptureDeviceInput deviceInputWithDevice:d error:nil];
            for (AVCaptureInput *oldInput in _proviewLayer.session.inputs) {
                [[_proviewLayer session] removeInput:oldInput];
            }
            [_proviewLayer.session addInput:input];
            [_proviewLayer.session commitConfiguration];
            break;
        }
    }
    
    _isUsingFrontFacingCamera = !_isUsingFrontFacingCamera;
    
}
//没有获得相机权限的UI界面图
-(void)noAuthorizationUI{
    self.view.backgroundColor = [UIColor whiteColor];
    UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
    button.frame = CGRectMake(0, 0, self.view.frame.size.width, 60);
    [button setTitle:@"相机权限未开启,点击开启" forState: UIControlStateNormal];
    button.center = self.view.center;
    [button setTitleColor:[UIColor blueColor] forState:UIControlStateNormal];
    [self.view addSubview:button];
    [button addTarget:self action:@selector(getAuthoriza) forControlEvents:UIControlEventTouchUpInside];
    
    //取消的按钮
    UIView *canelBtn = [self drawXbutton:CGRectMake(self.view.frame.size.width-40, 30, 20, 20)];
    [self.view addSubview:canelBtn];
    
    [self showAlertView];
    
}
//获取授权的按钮
-(void)getAuthoriza{
    
    [self showAlertView];
}

//弹出alertview
-(void)showAlertView{
    UIAlertController *alertVC = [UIAlertController alertControllerWithTitle:@"相机权限未开启" message:@"相机权限未开启，请进入系统【设置】>【隐私】>【相机】中打开开关,开启相机功能" preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *ok = [UIAlertAction actionWithTitle:@"立即开启" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        
#ifdef __IPHONE_8_0
        //跳入当前App设置界面,
        [[UIApplication sharedApplication]openURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString]];
#else
        //适配iOS7 ,跳入系统设置界面
        [[UIApplication sharedApplication]openURL:[NSURL URLWithString:@"prefs:General&path=Reset"]];
#endif
        
    }];
    __weak __typeof(self) weakSelf = self;
    UIAlertAction *canel = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
        
        [weakSelf dismissViewControllerAnimated:YES completion:nil];
    }];
    [alertVC addAction:ok];
    [alertVC addAction:canel];
    [self presentViewController:alertVC animated:YES completion:nil];
}

//返回按钮的点击事件
-(void)backClick{
    
    [self dismissViewControllerAnimated:YES completion:nil];
}
#pragma mark -- 屏幕的点击事件--用于聚焦
-(void)screenTouch:(UIGestureRecognizer *)sender{
    
    CGPoint point = [sender locationInView:self.view];
    NSLog(@" = = = %@",NSStringFromCGPoint(point));
    [self focusAtPoint:point];
    
}

#pragma mark --聚焦的坐标
-(void)focusAtPoint:(CGPoint)point{
    
    CGSize size = self.view.bounds.size;
    CGPoint focusPoint = CGPointMake(point.y /size.height ,1-point.x/size.width);
    
    NSError *error;
    if ([self.device lockForConfiguration:&error]) {
        //对焦模式和对焦点
        if ([self.device isFocusModeSupported:AVCaptureFocusModeAutoFocus]) {
            [self.device setFocusPointOfInterest:focusPoint];
            [self.device setFocusMode:AVCaptureFocusModeAutoFocus];
        }
        //曝光模式和曝光点
        if ([self.device isExposureModeSupported:AVCaptureExposureModeAutoExpose ]) {
            [self.device setExposurePointOfInterest:focusPoint];
            [self.device setExposureMode:AVCaptureExposureModeAutoExpose];
        }
        
        [self.device unlockForConfiguration];
        
        
        //设置对焦动画
        _focusView.center = point;
        _focusView.hidden = NO;
        [UIView animateWithDuration:0.3 animations:^{
            _focusView.transform = CGAffineTransformMakeScale(1.25, 1.25);
        }completion:^(BOOL finished) {
            [UIView animateWithDuration:0.5 animations:^{
                _focusView.transform = CGAffineTransformIdentity;
            } completion:^(BOOL finished) {
                _focusView.hidden = YES;
            }];
        }];
    }
}
#pragma mark -- rocButton的代理
//RocButton单击事件的回调
-(void)singleClick:(UITapGestureRecognizer *)sender{
    
    //拍照
    AVCaptureConnection *stillImageConnection = [_stillImageOutPut connectionWithMediaType:AVMediaTypeVideo];
    
    //    UIDeviceOrientation curDeviceOrientation = [[UIDevice currentDevice]orientation];
    
    AVCaptureVideoOrientation avcaptureVideoOrientation = [self getOrigatationForDeviceOrigitation];
    
    [stillImageConnection setVideoOrientation:avcaptureVideoOrientation];
    [stillImageConnection setVideoScaleAndCropFactor:1];
    
    [_stillImageOutPut captureStillImageAsynchronouslyFromConnection:stillImageConnection completionHandler:^(CMSampleBufferRef imageDataSampleBuffer, NSError *error) {
        
        NSData *jpegData =[AVCaptureStillImageOutput jpegStillImageNSDataRepresentation:imageDataSampleBuffer];
        //        CFDictionaryRef attachment = CMCopyDictionaryOfAttachments(kCFAllocatorDefault, imageDataSampleBuffer, kCMAttachmentMode_ShouldPropagate);
        
        
        self.imageV.layer.masksToBounds = YES;
        
        self.imageV.image = [UIImage imageWithData:jpegData];
        [self.session stopRunning];
        
        [self.view addSubview:self.imageV];
        
    }];
    
}
//RocButton的长按开始回调，当self.canRecordVideo为YES的时候走此回调
-(void)longPressStateBegan:(UILongPressGestureRecognizer *)sender{
    NSLog(@"xxxxxxxx长按开始xxxxx");
}
//RocButton的长按结束的回调，当self.canRecordVideo为YES的时候走此回调
-(void)longPressStateEnded:(UILongPressGestureRecognizer *)sender{
    NSLog(@"xxxxxxx长按结束xxxxxxx");
}

//获取设备的方向
-(AVCaptureVideoOrientation)getOrigatationForDeviceOrigitation{
    
    AVCaptureVideoOrientation orientationNew = AVCaptureVideoOrientationPortrait;
    if (self.motionManager.magnetometerAvailable) {
        //如果CMMotionManager的陀螺仪数据可用
        CMAccelerometerData* gyroData = self.motionManager.accelerometerData;
        NSLog(@"%@",gyroData);
        
        if (gyroData.acceleration.x >= 0.75) {//home button left
            orientationNew = AVCaptureVideoOrientationLandscapeLeft;
        }
        else if (gyroData.acceleration.x <= -0.75) {//home button right
            orientationNew = AVCaptureVideoOrientationLandscapeRight;
        }
        else if (gyroData.acceleration.y <= -0.75) {
            orientationNew = AVCaptureVideoOrientationPortrait;
        }
        else if (gyroData.acceleration.y >= 0.75) {
            orientationNew = AVCaptureVideoOrientationPortraitUpsideDown;
        }
        else {
            // Consider same as last time
            orientationNew = AVCaptureVideoOrientationPortrait;
        }
        
    }
    
    return orientationNew;
    
}
-(AVCaptureVideoOrientation)avOrigatationForDeviceOrigitation:(UIDeviceOrientation)deviceOrientation{
    NSLog(@"xxxxxxxxxxx======%ld",(long)deviceOrientation);
    
    AVCaptureVideoOrientation result = (AVCaptureVideoOrientation)deviceOrientation;
    if (deviceOrientation == UIDeviceOrientationLandscapeLeft) {
        result = AVCaptureVideoOrientationLandscapeRight;
    }else if (deviceOrientation == UIDeviceOrientationLandscapeRight){
        
        result = AVCaptureVideoOrientationLandscapeLeft;
    }
    result = AVCaptureVideoOrientationPortrait;
    return result;
    
}
//图片展示view的set方法
-(UIImageView *)imageV{
    
    if (!_imageV) {
        
        _imageV = [[UIImageView alloc]initWithFrame:self.proviewLayer.frame];
        _imageV.userInteractionEnabled  = YES;
        _imageV.backgroundColor = [UIColor blackColor];
        _imageV.contentMode = UIViewContentModeScaleAspectFit;
        
        //图片保存按钮
        UIView *saveView = [[UIView alloc]initWithFrame:CGRectMake(self.view.frame.size.width/2+30, self.view.frame.size.height-100, 60, 60)];
        saveView.backgroundColor = [UIColor colorWithRed:48/255.0 green:199/255.0 blue:116/255.0 alpha:1.0];
        saveView.layer.cornerRadius = 30.0;
        saveView.layer.masksToBounds = NO;
        [_imageV addSubview:saveView];
        UITapGestureRecognizer *savaTap = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(saveAlbumClik)];
        [saveView addGestureRecognizer:savaTap];
        
        //线的路径
        UIBezierPath *linePath = [UIBezierPath bezierPath];
        // 起点
        [linePath moveToPoint:CGPointMake(saveView.frame.size.width/4, saveView.frame.size.height/2)];
        // 其他点
        [linePath addLineToPoint:CGPointMake(saveView.frame.size.width/3+8 ,saveView.frame.size.height/2+12)];
        [linePath addLineToPoint:CGPointMake(47, 17)];
        
        CAShapeLayer *lineLayer = [CAShapeLayer layer];
        
        lineLayer.lineWidth = 3;
        lineLayer.strokeColor = [UIColor whiteColor].CGColor;
        lineLayer.path = linePath.CGPath;
        lineLayer.fillColor = nil; // 默认为blackColor
        
        [saveView.layer addSublayer:lineLayer];
        
        
        //图片取消按钮
        UIView *canelView = [[UIView alloc]initWithFrame:CGRectMake(self.view.frame.size.width/2-30-60, self.view.frame.size.height-100, 60, 60)];
        canelView.backgroundColor = [UIColor colorWithRed:48/255.0 green:199/255.0 blue:116/255.0 alpha:1.0];
        canelView.layer.cornerRadius = 30.0;
        canelView.layer.masksToBounds = NO;
        [_imageV addSubview:canelView];
        UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(canelAlbumClik)];
        [canelView addGestureRecognizer:tap];
        // 线的路径
        UIBezierPath *linePath1 = [UIBezierPath bezierPath];
        // 起点
        [linePath1 moveToPoint:CGPointMake(canelView.frame.size.width/3*2, 13)];
        // 其他点
        [linePath1 addLineToPoint:CGPointMake(canelView.frame.size.width/3, canelView.frame.size.height/2)];
        [linePath1 addLineToPoint:CGPointMake(canelView.frame.size.width/3*2, canelView.frame.size.height-13 )];
        
        CAShapeLayer *lineLayer1 = [CAShapeLayer layer];
        
        lineLayer1.lineWidth = 3;
        lineLayer1.strokeColor = [UIColor whiteColor].CGColor;
        lineLayer1.path = linePath1.CGPath;
        lineLayer1.fillColor = nil; // 默认为blackColor
        
        [canelView.layer addSublayer:lineLayer1];
        
        
        //取消的按钮
        UIView *canelBtn = [self drawXbutton:CGRectMake(self.view.frame.size.width-40, 30, 20, 20)];
        [_imageV addSubview:canelBtn];
        
    }
    return _imageV;
}

- (UIImage *) scaleImage:(UIImage *)image toScale:(float)scaleSize {
    UIGraphicsBeginImageContext(CGSizeMake(image.size.width * scaleSize, image.size.height * scaleSize));
    [image drawInRect:CGRectMake(0, 0, image.size.width * scaleSize, image.size.height * scaleSize)];
    UIImage *scaledImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return scaledImage;
}
                                
                                
#pragma mark --照片保存的点击事件
-(void)saveAlbumClik{
    
    UIImage *image =[self fixOrientation:self.imageV.image];
    NSData *imageData = UIImagePNGRepresentation(image);
    
    //
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"YYYYMMddHHmmss"];
    
   
    NSDate *datenow = [NSDate date];
    NSString *currentTimeString = [formatter stringFromDate:datenow];
    
    NSLog(@"currentTimeString =  %@",currentTimeString);
    
    //图片保存在本地
    NSString *tmpDir = NSTemporaryDirectory();
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSString *imagePath = [[tmpDir stringByAppendingString:currentTimeString] stringByAppendingString:@"--ori--image.png"];
    [fileManager createFileAtPath:imagePath contents:imageData attributes:nil];
    
    //保存到系统相册
    UIImageWriteToSavedPhotosAlbum(self.imageV.image, self,  @selector(image:didFinishSavingWithError:contextInfo:), NULL);
    
    //缩略图
    float scaleSize=0.3;
    UIGraphicsBeginImageContext(CGSizeMake(image.size.width * scaleSize, image.size.height * scaleSize));
    [image drawInRect:CGRectMake(0, 0, image.size.width * scaleSize, image.size.height * scaleSize)];
    UIImage *scaledImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    NSData *smallImageData = UIImagePNGRepresentation(scaledImage);
    NSString *smallImagePath = [[tmpDir stringByAppendingString:currentTimeString] stringByAppendingString:@"--small--image.png"];
    [fileManager createFileAtPath:smallImagePath contents:smallImageData attributes:nil];
    
    //回调得到的拍照相片的路径
    if ([_delegate respondsToSelector:@selector(getImagePath:oriLength:smallPath:smallLength:)]) {
        [_delegate getImagePath:imagePath oriLength:[imageData length] smallPath:smallImagePath smallLength:[smallImageData length]];
    }
    
    
    
}
//保存系统相册完成后的回调
- (void)image: (UIImage *) image didFinishSavingWithError: (NSError *) error contextInfo: (void *) contextInfo
{
    NSString *msg = nil ;
    if(error != NULL){
        msg = @"保存图片失败" ;
    }else{
        msg = @"保存图片成功" ;
    }
    
    //    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"提示" message:msg preferredStyle:UIAlertControllerStyleAlert];
    //    [self showViewController:alert sender:nil];
    
    [self.imageV removeFromSuperview];
    [self.session startRunning];
    
    [self dismissViewControllerAnimated:YES completion:nil];
    
}
#pragma mark -- 照片取消的点击事件
-(void)canelAlbumClik{
    
    [self.imageV removeFromSuperview];
    [self.session startRunning];
}

#pragma mark --- 图片的修正
-(UIImage *)fixOrientation:(UIImage *)aImage {
    
    // No-op if the orientation is already correct
    if (aImage.imageOrientation == UIImageOrientationUp)
        return aImage;
    
    // We need to calculate the proper transformation to make the image upright.
    // We do it in 2 steps: Rotate if Left/Right/Down, and then flip if Mirrored.
    CGAffineTransform transform = CGAffineTransformIdentity;
    
    switch (aImage.imageOrientation) {
            
        case UIImageOrientationDown:
        case UIImageOrientationDownMirrored:
            transform = CGAffineTransformTranslate(transform, aImage.size.width, aImage.size.height);
            transform = CGAffineTransformRotate(transform, M_PI);
            break;
            
        case UIImageOrientationLeft:
        case UIImageOrientationLeftMirrored:
            transform = CGAffineTransformTranslate(transform, aImage.size.width, 0);
            transform = CGAffineTransformRotate(transform, M_PI_2);
            break;
            
        case UIImageOrientationRight:
        case UIImageOrientationRightMirrored:
            transform = CGAffineTransformTranslate(transform, 0, aImage.size.height);
            transform = CGAffineTransformRotate(transform, -M_PI_2);
            break;
        default:
            break;
    }
    
    switch (aImage.imageOrientation) {
        case UIImageOrientationUpMirrored:
        case UIImageOrientationDownMirrored:
            transform = CGAffineTransformTranslate(transform, aImage.size.width, 0);
            transform = CGAffineTransformScale(transform, -1, 1);
            break;
            
        case UIImageOrientationLeftMirrored:
        case UIImageOrientationRightMirrored:
            transform = CGAffineTransformTranslate(transform, aImage.size.height, 0);
            transform = CGAffineTransformScale(transform, -1, 1);
            break;
        default:
            break;
    }
    
    // Now we draw the underlying CGImage into a new context, applying the transform
    // calculated above.
    CGContextRef ctx = CGBitmapContextCreate(NULL, aImage.size.width, aImage.size.height,
                                             CGImageGetBitsPerComponent(aImage.CGImage), 0,
                                             CGImageGetColorSpace(aImage.CGImage),
                                             CGImageGetBitmapInfo(aImage.CGImage));
    CGContextConcatCTM(ctx, transform);
    switch (aImage.imageOrientation) {
        case UIImageOrientationLeft:
        case UIImageOrientationLeftMirrored:
        case UIImageOrientationRight:
        case UIImageOrientationRightMirrored:
            // Grr...
            CGContextDrawImage(ctx, CGRectMake(0,0,aImage.size.height,aImage.size.width), aImage.CGImage);
            break;
            
        default:
            CGContextDrawImage(ctx, CGRectMake(0,0,aImage.size.width,aImage.size.height), aImage.CGImage);
            break;
    }
    
    // And now we just create a new UIImage from the drawing context
    CGImageRef cgimg = CGBitmapContextCreateImage(ctx);
    UIImage *img = [UIImage imageWithCGImage:cgimg];
    CGContextRelease(ctx);
    CGImageRelease(cgimg);
    return img;
}
#pragma end
@end

