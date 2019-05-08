/********* signData.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import "AES128Util.h"
#include "Sign.mm"
#include "string"
extern std::string ss1;
extern std::string ss2;
@interface signData : CDVPlugin {
    // Member variables go here.
}

- (void)coolMethod:(CDVInvokedUrlCommand*)command;
- (void)signToData:(CDVInvokedUrlCommand*)command;
- (void)buildWallet:(CDVInvokedUrlCommand*)command;
- (void)changeWalletPsd:(CDVInvokedUrlCommand*)command;
- (void)getwallet:(CDVInvokedUrlCommand*)command;
@end

@implementation signData

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
#pragma mark -创建钱包
- (void)buildWallet:(CDVInvokedUrlCommand*)command{
    CDVPluginResult* pluginResult = nil;
    NSDictionary *dic = command.arguments[0];
    NSString *useridd = dic[@"userId"];
    NSLog(@"钱包%@",dic);
    NSString *account_id = dic[@"account_id"];
    NSString *cert = dic[@"userCert"];
    NSString *key = dic[@"userKey"];
    NSString *serial_num = dic[@"serialNumber"];
    NSString *userid = [AES128Util md5:useridd];
    
    NSMutableDictionary *certdic = [NSMutableDictionary dictionary];
    [certdic setValue:account_id forKey:@"account_id"];
    [certdic setValue:cert forKey:@"cert"];
    [certdic setValue:key forKey:@"key"];
    [certdic setValue:serial_num forKey:@"serial_num"];
    
    NSArray *arr = @[certdic];
    
    NSMutableDictionary *walletDic = [NSMutableDictionary dictionary];
    [walletDic setValue:arr forKey:@"certs"];
    [walletDic setValue:userid forKey:@"userid"];
    [walletDic setValue:@"我的钱包" forKey:@"name"];
    [walletDic setValue:@"" forKey:@"passwd"];
    NSString *check = [self buildCheck:walletDic];
    [walletDic setValue:check forKey:@"check"];
    NSLog(@"创建的钱包---->%@",walletDic);
    BOOL succ = [self buildb2w:walletDic userid:userid];
    if (succ) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"创建成功"];
    }else{
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"创建失败"];
    }
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
    
}
#pragma mark -成功
- (void)success:(NSString *)str command:(CDVInvokedUrlCommand*)command{
    CDVPluginResult*  pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:str];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}
#pragma mark -失败
- (void)falid:(NSString *)str command:(CDVInvokedUrlCommand*)command{
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:str];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

#pragma mark -存本地
-(BOOL)buildb2w:(NSDictionary *)walletDic userid:(NSString*)userid{
    NSFileManager *manager = [NSFileManager defaultManager];
    
    
    NSString *path = [NSHomeDirectory() stringByAppendingPathComponent:[NSString stringWithFormat:@"Documents/%@",userid]];
    BOOL isDir ;
    NSError *parError = nil;
    if ([manager fileExistsAtPath:path isDirectory:&isDir]) {
        if (!isDir){
            [manager createDirectoryAtPath:path withIntermediateDirectories:YES attributes:nil error:&parError];
        }
    }else{
        [manager createDirectoryAtPath:path withIntermediateDirectories:YES attributes:nil error:&parError];
    }
    if(parError){
        return NO;
    }
    NSString *patht =[path stringByAppendingPathComponent:@"xiwallet.json"];
    NSError *parseError = nil;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:walletDic options:NSJSONWritingPrettyPrinted error:&parseError];
    if(parseError){
        
        return NO;
    }
    NSString *jsonstring = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    jsonstring = [jsonstring stringByReplacingOccurrencesOfString:@"\\/" withString:@"/"];
    NSData *data = [jsonstring dataUsingEncoding:NSUTF8StringEncoding];
    //第一个参数是创建的文件的路径，第二个参数是创建的文件的内容，第三个参数是文件的属性。返回值表示创建的结果（成功，失败）
    BOOL succ = [manager createFileAtPath:patht contents:data attributes:nil];
    return succ;
}
#pragma mark -设置／修改密码
- (void)changeWalletPsd:(CDVInvokedUrlCommand*)command{
    NSDictionary *dic = command.arguments[0];
    
    NSString *userid = [AES128Util md5:dic[@"userid"]];
    
    NSString *oldPsd= dic[@"oldPsd"];
    NSString *newPsd= dic[@"newPsd"];
    
    if(newPsd.length<=0 || newPsd.length<8 || newPsd.length >16){
        [self falid:@"密码长度错误" command:command];
        return;
    }
    NSString *path = [NSHomeDirectory() stringByAppendingPathComponent:[NSString stringWithFormat:@"Documents/%@/xiwallet.json",userid]];
    NSError *errr;
    NSString *content=[NSString stringWithContentsOfFile:path encoding:NSUTF8StringEncoding error:&errr];
    if (errr) {
        [self falid:@"打开钱包出错" command:command];
        return;
    }
    
    NSData *jsonData = [content dataUsingEncoding:NSUTF8StringEncoding];
    NSError *err;
    NSDictionary *dicc = [NSJSONSerialization JSONObjectWithData:jsonData options:NSJSONReadingMutableContainers error:&err];
    NSLog(@"钱包%@",dicc);
    //校验check
    BOOL check = [self jiaoYanCheck:dicc];
    if (!check) {
        [self falid:@"钱包校验错误" command:command];
    }
    //校验userid
    NSString *myuserid = dicc[@"userid"];
    if (![userid isEqualToString:myuserid]) {
        [self falid:@"参数错误" command:command];
        return;
    }
    
    NSString *mypsd = dicc[@"passwd"];
    if(oldPsd.length<=0 && mypsd.length>0) {
        //旧密码有误
        [self falid:@"参数错误" command:command];
    }else if(oldPsd.length>0 && mypsd.length<=0) {
        //不可以修改钱包密码
        [self falid:@"当前不可以修改钱包密码" command:command];
    }else if(oldPsd.length<=0 && mypsd.length<=0){
        //设置初始密码
        NSDictionary* walletDic = [self keyEncrypt:dicc pswd:oldPsd newpsd:newPsd];
        NSLog(@"设置初始密码后的钱包---->%@",walletDic);
        BOOL succ = [self buildb2w:walletDic userid:userid];
        if(succ){
            [self success:@"设置密码成功" command:command];
        }else{
            [self falid:@"设置密码失败" command:command];
        }
        
    }else if (oldPsd.length>0 && mypsd.length>0) {
        NSString *pass = [AES128Util md5:oldPsd];
        //旧密码错误
        if (![pass isEqualToString:mypsd]) {
            [self falid:@"旧密码错误" command:command];
            return;
        }
        NSDictionary* walletDic = [self keyEncrypt:dicc pswd:oldPsd newpsd:newPsd];
        NSLog(@"修改密码后的钱包---->%@",walletDic);
        BOOL succ = [self buildb2w:walletDic userid:userid];
        if(succ){
            [self success:@"修改密码成功" command:command];
        }else{
            [self falid:@"修改密码失败" command:command];
        }
    }
    
}
#pragma mark -获取qian b
-(void)getwallet:(CDVInvokedUrlCommand*)command{
    
    NSDictionary *dic = command.arguments[0];
    NSString *userid = [AES128Util md5:dic[@"userid"]];
    NSString *path = [NSHomeDirectory() stringByAppendingPathComponent:[NSString stringWithFormat:@"Documents/%@/xiwallet.json",userid]];
    NSError *errr;
    NSString *content=[NSString stringWithContentsOfFile:path encoding:NSUTF8StringEncoding error:&errr];
    if (errr) {
        [self falid:@"打开钱包出错" command:command];
        return;
    }
    
    NSData *jsonData = [content dataUsingEncoding:NSUTF8StringEncoding];
    NSError *err;
    NSDictionary *dicc = [NSJSONSerialization JSONObjectWithData:jsonData options:NSJSONReadingMutableContainers error:&err];
    if (err) {
        [self falid:@"打开钱包出错" command:command];
        return;
    }
    
    //校验check
    BOOL check = [self jiaoYanCheck:dicc];
    if (!check) {
        [self falid:@"钱包校验错误" command:command];
        return;
    }
    //校验userid
    NSString *myuserid = dicc[@"userid"];
    if (![userid isEqualToString:myuserid]) {
        [self falid:@"参数错误" command:command];
        return;
    }
    NSLog(@"钱包%@",dicc);
    
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dicc];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

#pragma mark -签名
- (void)signToData:(CDVInvokedUrlCommand*)command{
    // NSString *Path = [[NSBundle mainBundle] pathForResource:@"key" ofType:@"pem"];
    // NSLog(@"%@",Path);
    CDVPluginResult* pluginResult = nil;
    @try {
        NSDictionary *dic =command.arguments[0];
        NSString *signdata = dic[@"signdata"];
        NSString *userid =[AES128Util md5:dic[@"userid"]];
        NSString *accountid =dic[@"accountid"];
        NSString *psd =dic[@"psd"];
        if(signdata.length<=0 ||userid.length<=0 ||accountid.length<=0 ||psd.length<=0 ){
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"参数有误"];
            return;
        }
        NSString *path = [NSHomeDirectory() stringByAppendingPathComponent:[NSString stringWithFormat:@"Documents/%@/xiwallet.json",userid]];
        NSError *errr;
        NSString *content=[NSString stringWithContentsOfFile:path encoding:NSUTF8StringEncoding error:&errr];
        if (errr) {
            [self falid:@"打开钱包出错" command:command];
            return;
        }
        
        NSData *jsonData = [content dataUsingEncoding:NSUTF8StringEncoding];
        NSError *err;
        NSDictionary *dicc = [NSJSONSerialization JSONObjectWithData:jsonData options:NSJSONReadingMutableContainers error:&err];
        if (err) {
            [self falid:@"打开钱包出错" command:command];
            return;
        }
        if (psd.length<=0 || ![[AES128Util md5:psd] isEqualToString:dicc[@"passwd"]]) {
            [self falid:@"钱包密码错误" command:command];
            return;
        }
        //校验check
        BOOL check = [self jiaoYanCheck:dicc];
        if (!check) {
            [self falid:@"钱包校验错误" command:command];
            return;
        }
        //校验userid
        NSString *myuserid = dicc[@"userid"];
        if (![userid isEqualToString:myuserid]) {
            [self falid:@"参数错误" command:command];
            return;
        }
        NSArray *certarr = dicc[@"certs"];
        NSString *keystr = nil;
        for (NSDictionary*dicr in certarr) {
            if ([accountid isEqualToString:dicr[@"account_id"]]) {
                keystr =[self keyDecrypt:dicr[@"key"] pswd:psd];
            }
        }
        if (keystr.length<=0) {
            [self falid:@"钱包不可用" command:command];
            return;
        }
        
        NSLog(@"key--->>%@",keystr);
        keystr= [keystr stringByReplacingOccurrencesOfString:@"\\n" withString:@"\n"];
        keystr= [keystr stringByReplacingOccurrencesOfString:@"\\r" withString:@"\r"];
        const char *pempath =(const char *)keystr.UTF8String;
        unsigned char out[5000];
        const unsigned char *in =(const unsigned char *)signdata.UTF8String;
        Base64ToByte((unsigned char *)in, out, 5000);
        
        signchange(out, pempath);
        
        printf("---sig1--->>>>%s \n", ss1.c_str());
        printf("---sig2--->>>>%s \n", ss2.c_str());
        NSString*s1 = [NSString stringWithFormat:@"%s",ss1.c_str()];
        NSString*s2 = [NSString stringWithFormat:@"%s",ss2.c_str()];
        if(s1.length>0&&s2.length>0){
            NSDictionary *dic= @{@"sign1":s1,@"sign2":s2};
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dic];
        }else{
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"签名失败"];
            return;
        }
        
    } @catch (NSException *exception) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    } @finally{
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}
#pragma mark -校验密码
//校验密码 用户输入密码  从钱包中得到的密码
- (BOOL)jiaoYanPassword:(NSString *)password myps:(NSString *)myPassword{
    if ([[AES128Util md5:password] isEqualToString:myPassword]) {
        return YES;
    }
    return NO;
}
#pragma mark -校验check
//校验check 钱包
- (BOOL)jiaoYanCheck:(NSDictionary *)qianbao{
    NSString *check = qianbao[@"check"];
    NSString *mycheck =[self buildCheck:qianbao];
    if ([check isEqualToString:mycheck]) {
        return YES;
    }
    return NO;
}
#pragma mark -私钥加密
//给私钥加密 钱包 老密码 新密码
- (NSDictionary *)keyEncrypt:(NSDictionary *)qianbao pswd:(NSString *)psd newpsd:(NSString *)newpsd{
    NSArray *arr = qianbao[@"certs"];
    NSMutableArray *muarr = [NSMutableArray array];
    NSMutableDictionary *dic = [NSMutableDictionary dictionaryWithDictionary:qianbao];
    for (NSDictionary *dic in arr) {
        //加密私钥
        NSString *str = nil;
        if (psd.length>0) {
            NSString *keyr =  [self keyDecrypt:dic[@"key"] pswd:psd];
            str = [AES128Util AES128Encrypt:keyr key:newpsd];
        }else{
            str = [AES128Util AES128Encrypt:dic[@"key"] key:newpsd];
        }
        NSLog(@"aesstr--->>%@",str);
        NSString *strr = [AES128Util base64StringFromText:str];
        NSLog(@"base64%@",strr);
        NSMutableDictionary *dicc = [NSMutableDictionary dictionaryWithDictionary:dic];
        [dicc setValue:strr forKey:@"key"];
        [muarr addObject:dicc];
    }
    [dic setValue:muarr forKey:@"certs"];
    NSString *passwrd = [AES128Util md5:newpsd];
    [dic setValue:passwrd forKey:@"passwd"];
    NSString *check = [self buildCheck:dic];
    [dic setValue:check forKey:@"check"];
    return dic;
}
#pragma mark -私钥解密
//给私钥解密 KEY 密码
- (NSString *)keyDecrypt:(NSString *)key pswd:(NSString *)psd{
    //解密私钥
    NSString *keystr = [AES128Util textFromBase64String:key];
    
    NSString *decryptStr= [AES128Util AES128Decrypt:keystr key:psd];
    NSLog(@"aes解密--->>%@",decryptStr);
    return decryptStr;
}
#pragma mark -生成check
//生成check 钱包
- (NSString *)buildCheck:(NSDictionary *)qianbao{
    NSString *password =qianbao[@"passwd"];
    NSString *userid =qianbao[@"userid"];
    NSArray *certArr =qianbao[@"certs"];
    NSString *appendingString;
    if(password.length>0){
        appendingString =[password stringByAppendingString:userid];
    }else{
        appendingString = userid;
    }
    //排序
    NSStringCompareOptions comparisonOptions =NSWidthInsensitiveSearch|NSForcedOrderingSearch;
    NSComparator sort = ^(NSDictionary *obj1,NSDictionary *obj2){
        NSRange range = NSMakeRange(0,[obj1[@"serial_num"] length]);
        return [obj1[@"serial_num"] compare:obj2[@"serial_num"] options:comparisonOptions range:range];
    };
    NSArray *resultArray = [certArr sortedArrayUsingComparator:sort];
    for (NSDictionary *dic in resultArray) {
        NSString *str = [NSString stringWithFormat:@"%@%@%@%@",dic[@"account_id"],dic[@"serial_num"],dic[@"cert"],dic[@"key"]];
        appendingString = [appendingString stringByAppendingString:str];
    }
    NSString *check = [AES128Util md5:appendingString];
    return check;
}

@end
