/********* request.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import "AFNetworking.h"
@interface request : CDVPlugin {
    // Member variables go here.
}

- (void)coolMethod:(CDVInvokedUrlCommand*)command;
- (void)post:(CDVInvokedUrlCommand*)command;
- (void)get:(CDVInvokedUrlCommand*)command;
- (void)download:(CDVInvokedUrlCommand*)command;
- (void)uploadimage:(CDVInvokedUrlCommand*)command;
@end

@implementation request

- (void)coolMethod:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* echo = [command.arguments objectAtIndex:0];
    
    if (echo != nil && [echo length] > 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:echo];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}
- (void)post:(CDVInvokedUrlCommand*)command{
    NSDictionary *dic = command.arguments[0];
    NSString *urlstr = dic[@"url"];
    NSDictionary *parameters =dic[@"parameters"];
    if (urlstr.length<=0) {
        [self fail:@"参数有误" commd:command];
    }
    NSString * path = [[NSBundle mainBundle] pathForResource:@"test.xichain.com.cn" ofType:@"cer"];
    //
    NSData * cerData = [NSData dataWithContentsOfFile:path];
    //
    NSSet * dataSet = [NSSet setWithObject:cerData];
    //AFNetworking验证证书的object,AFSSLPinningModeCertificate参数决定了验证证书的方式
    AFSecurityPolicy * securityPolicy = [AFSecurityPolicy policyWithPinningMode:AFSSLPinningModeCertificate withPinnedCertificates:dataSet];
    //是否可以使用自建证书（不花钱的）
    securityPolicy.allowInvalidCertificates=YES;
    //是否验证域名（一般不验证）
    securityPolicy.validatesDomainName=NO;
    
    AFHTTPSessionManager *manager = [AFHTTPSessionManager manager];
    [manager setSecurityPolicy:securityPolicy];
    //一但用了这个返回的那个responseObject就是NSData，如果不用就是简单的
    manager.responseSerializer = [AFHTTPResponseSerializer serializer];
    manager.requestSerializer = [AFJSONRequestSerializer serializer];
    manager.responseSerializer.acceptableContentTypes = [NSSet setWithObjects:@"application/json", @"text/json", @"text/javascript", @"text/html",@"image/jpeg",@"text/plain", nil];
    
    [manager POST:urlstr parameters:parameters progress:nil success:
     ^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
         // NSString * str  =[[NSString alloc] initWithData:responseObject encoding:NSUTF8StringEncoding];
         NSDictionary * response= [NSJSONSerialization JSONObjectWithData:responseObject options:0 error:nil];
         NSLog(@"post请求成功--%@",response);
         [self success:response commd:command];
         
     } failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
         NSLog(@"请求失败--%@",error);
         [self fail:error.domain commd:command];
     }];
    
}

-(void)success:(NSDictionary *)dic commd:(CDVInvokedUrlCommand*)command{
    CDVPluginResult* pluginResult= [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dic];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}
-(void)fail:(NSString *)dic commd:(CDVInvokedUrlCommand*)command{
    CDVPluginResult* pluginResult= [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:dic];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)get:(CDVInvokedUrlCommand*)command{
    NSDictionary *dic = command.arguments[0];
    NSString *urlstr = dic[@"url"];
    if (urlstr.length<=0) {
        [self fail:@"参数有误" commd:command];
    }
    NSDictionary *parameters =dic[@"parameters"];
    
    AFHTTPSessionManager *manager = [AFHTTPSessionManager manager];
    //一但用了这个返回的那个responseObject就是NSData，如果不用就是简单的
    manager.responseSerializer = [AFHTTPResponseSerializer serializer];
    manager.requestSerializer = [AFJSONRequestSerializer serializer];
    manager.responseSerializer.acceptableContentTypes = [NSSet setWithObjects:@"application/json", @"text/json", @"text/javascript", @"text/html",@"image/jpeg",@"text/plain", nil];
    
    [manager GET:urlstr parameters:parameters progress:nil success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
        NSDictionary * response= [NSJSONSerialization JSONObjectWithData:[responseObject dataUsingEncoding:NSUTF8StringEncoding] options:0 error:nil];
        NSLog(@"get请求成功--%@",response);
        [self success:response commd:command];
    } failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
        NSLog(@"请求失败--%@",error);
        [self fail:error.domain commd:command];
    }];
}

- (void)download:(CDVInvokedUrlCommand*)command {
    NSDictionary *dic = command.arguments[0];
    NSString *urlstr = dic[@"url"];
    // 1.创建网络管理者
    // AFHTTPSessionManager 基于NSURLSession
    AFHTTPSessionManager *manager = [AFHTTPSessionManager manager];
    //@"http://120.25.226.186:32812/resources/videos/minion_02.mp4"
    // 2.利用网络管理者下载数据
    NSURLRequest *request = [NSURLRequest requestWithURL:[NSURL URLWithString:urlstr]];
    /*
     destination
     - targetPath: 系统给我们自动写入的文件路径
     - block的返回值, 要求返回一个URL, 返回的这个URL就是剪切的位置的路径
     completionHandler
     - url :destination返回的URL == block的返回的路径
     */
    /*
     @property int64_t totalUnitCount;  需要下载文件的总大小
     @property int64_t completedUnitCount; 当前已经下载的大小
     */
    // NSProgress *progress = nil;
    NSURLSessionDownloadTask *downTask = [manager downloadTaskWithRequest:request progress:^(NSProgress *progres){
        NSLog(@"%f",1.0 * progres.completedUnitCount / progres.totalUnitCount);
        NSString *strr =[NSString stringWithFormat:@"%.4f",1.0 * progres.completedUnitCount / progres.totalUnitCount];
        NSString *completed =[NSString stringWithFormat:@"%.4f",1.0 *progres.completedUnitCount];
        NSString *total =[NSString stringWithFormat:@"%.4f",1.0 *progres.totalUnitCount];
        CDVPluginResult* pluginResult= [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:@{@"code":@"",@"progress":strr,@"completed":completed,@"total":total,@"cachepath":@""}];
        [pluginResult setKeepCallbackAsBool:true];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        //[self success:@{@"code":@"",@"progress":strr,@"cachepath":@""} commd:command];
    } destination:^NSURL *(NSURL *targetPath, NSURLResponse *response) {
        
        NSString *cachesPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject];
        NSString *path = [cachesPath stringByAppendingPathComponent:[NSString stringWithFormat:@"download/%@",response.suggestedFilename]];
        return [NSURL fileURLWithPath:path];
        NSLog(@"%@",path);
    } completionHandler:^(NSURLResponse *response, NSURL *filePath, NSError *error) {
        if (error) {
            [self fail:error.domain commd:command];
        }else{
            [self success:@{@"code":@"200",@"progress":@"",@"completed":@"",@"total":@"",@"cachepath":filePath.absoluteString} commd:command];
            NSLog(@"%@",filePath.absoluteString);
        }
    }];
    
    // 3.启动任务
    [downTask resume];
    
}

- (void)uploadimage:(CDVInvokedUrlCommand*)command {
    NSDictionary *dic = command.arguments[0];
    NSString *urlstr = dic[@"url"];
    NSDictionary *parameters =dic[@"parameters"];
    NSArray*filePaths = dic[@"filePaths"];
    
    if (urlstr.length<=0 || filePaths.count <=0) {
        [self fail:@"参数有误" commd:command];
    }
    
    
    NSString * path = [[NSBundle mainBundle] pathForResource:@"test.xichain.com.cn" ofType:@"cer"];
    //
    NSData * cerData = [NSData dataWithContentsOfFile:path];
    //
    NSSet * dataSet = [NSSet setWithObject:cerData];
    //AFNetworking验证证书的object,AFSSLPinningModeCertificate参数决定了验证证书的方式
    AFSecurityPolicy * securityPolicy = [AFSecurityPolicy policyWithPinningMode:AFSSLPinningModeCertificate withPinnedCertificates:dataSet];
    //是否可以使用自建证书（不花钱的）
    securityPolicy.allowInvalidCertificates=YES;
    //是否验证域名（一般不验证）
    securityPolicy.validatesDomainName=NO;
    
    AFHTTPSessionManager *manager = [AFHTTPSessionManager manager];
    //[manager setSecurityPolicy:securityPolicy];
    manager.responseSerializer = [AFHTTPResponseSerializer serializer];
    //manager.responseSerializer.acceptableContentTypes = [NSSet setWithObjects:@"application/json", @"text/json", @"text/javascript", @"text/html",@"image/jpeg",@"text/plain", nil];
    [manager POST:urlstr parameters:parameters constructingBodyWithBlock:^(id<AFMultipartFormData>  _Nonnull formData) {
        //上传
        /*
         此方法参数
         1. 要上传的文件路径
         2. 后台处理文件的字段,若没有可为空
         3. 要保存在服务器上的[文件名]
         4. 上传文件的[mimeType]
         application/octet-stream为通用型
         */
        /* */
        int i = 0;
        for (NSString *filePath in filePaths) {
            UIImage *image = [UIImage imageWithContentsOfFile:filePath];
            NSData *imagedata = UIImageJPEGRepresentation(image, 1.0);
            NSString *picName = [NSString stringWithFormat:@"%d%@.png",i +1,[self generateUuidString]];
            [formData appendPartWithFileData:imagedata name:@"picFile" fileName:picName mimeType:@"image/png"];
            i++;
        }
        
    } progress:^(NSProgress * _Nonnull uploadProgress) {
        NSString *strr =[NSString stringWithFormat:@"%.4f",1.0 * uploadProgress.completedUnitCount / uploadProgress.totalUnitCount];
        NSString *completed =[NSString stringWithFormat:@"%.4f",1.0 *uploadProgress.completedUnitCount];
        NSString *total =[NSString stringWithFormat:@"%.4f",1.0 *uploadProgress.totalUnitCount];
        
        CDVPluginResult* pluginResult= [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:@{@"code":@"",@"progress":strr,@"completed":completed,@"total":total,@"cachepath":@""}];
        [pluginResult setKeepCallbackAsBool:true];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    } success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
        // NSData *data = [NSData dataWithData:responseObject];
        NSDictionary * response= [NSJSONSerialization JSONObjectWithData:responseObject options:0 error:nil];
        NSLog(@"updata请求成功--%@",response);
        NSLog(@"updata请求成功--%@",response[@"returnMsg"]);
        [self success:response commd:command];
        
    } failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
        [self fail:error.domain commd:command];
    }];
}
-(NSString *)generateUuidString{
    // create a new UUID which you own
    CFUUIDRef uuid = CFUUIDCreate(kCFAllocatorDefault);
    
    // create a new CFStringRef (toll-free bridged to NSString)
    // that you own
    NSString *uuidString = (NSString *)CFBridgingRelease(CFUUIDCreateString(kCFAllocatorDefault, uuid));
    
    CFRelease(uuid);
    
    return uuidString;
}
@end
