//
//  sign.cpp
//  openSSL
//
//  Created by corn on 2017/12/19.
//  Copyright © 2017年 roc. All rights reserved.
//

//#include "Test.hpp"

#include <openssl/pem.h>
#include <openssl/evp.h>
#include <openssl/ec.h>
#include <iostream>
#include "stdio.h"
#include "string.h"
#include <openssl/sha.h>
#include <openssl/ecdsa.h>
//#include "curl/curl.h"
#include "stdlib.h"
#include "sstream"
//#include <json/json.h>
//#include "algorithm"

using namespace std;
static string signdata;
static string uuid;
//static int flag = 0;

string ss1;
string ss2;

int ByteToBase64(unsigned char *in, unsigned char *out , int num) {
    int len;
    
    //printf("%s \n", in);
    len=EVP_EncodeBlock(out, in, num);
    //printf("encode byte is %s  %d\n", out, len);
    return len;
}

int Base64ToByte(unsigned char * in, unsigned char* out, int num) {
    int len;
    len=EVP_DecodeBlock(out, in, num);
    printf("decode byte is--->>> %s \n--->>> %d", out, len);
    return len;
    
}

void signchange(const unsigned char* in, const char * pem) {
    
    const EC_GROUP* group;
    int len;
    BIO *b=NULL;
    b = BIO_new(BIO_s_mem());
    len=BIO_write(b, pem, (int)strlen(pem));
    EC_KEY *eckey = EC_KEY_new();
    PEM_read_bio_ECPrivateKey(b, &eckey, NULL, NULL);
    BIO_free(b);
    
    
    
    group = EC_GROUP_new_by_curve_name(NID_X9_62_prime256v1);
    
    
    
    int n = (int)strlen((const char*)in);
    printf("the data len is %d \n", n);
    unsigned char digest[32];
    SHA256(in, n, digest);
    
    printf("digest is %s \n", in);
    
    EC_KEY_set_group(eckey, group);
    
    ECDSA_SIG *sig;
    
    
    char *sig1;
    char *sig2;
    
    
    
    while(true) {
        sig = ECDSA_do_sign(in, 32, eckey);
        
        BN_CTX *ctx = BN_CTX_new();
        BN_CTX_start(ctx);
        BIGNUM *order = BN_CTX_get(ctx);
        BIGNUM *halforder = BN_CTX_get(ctx);
        EC_GROUP_get_order(group, order, ctx);
        BN_rshift1(halforder, order);
        
        
        BIGNUM *r = BN_new();
        BIGNUM *s = BN_new();
        // ECDSA_SIG_get0((const ECDSA_SIG *)sig, (const BIGNUM **)(&r), (const BIGNUM **)(&s));
        r = sig -> r;
        s = sig -> s;
        
        sig1 = BN_bn2dec(r);
        sig2 = BN_bn2dec(s);
        printf("orderer is %s \n", BN_bn2dec(halforder));
        if (BN_cmp(sig->s, halforder) < 0) {
            break;
        }
        ECDSA_SIG_free(sig);
        BN_CTX_end(ctx);
        BN_CTX_free(ctx);
    }
    
    
    
    
    printf("dec_r is %s \n", sig1);
    printf("dec_s is %s \n", sig2);
    
    stringstream ssm;
    ssm << sig1;
    ss1 = ssm.str();
    stringstream ssm2;
    ssm2 << sig2;
    ss2 = ssm2.str();
    
    EC_KEY_free(eckey);
    
    
    // fclose(fp);
}


