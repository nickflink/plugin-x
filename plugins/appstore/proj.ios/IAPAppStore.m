/****************************************************************************
 Copyright (c) 2013 cocos2d-x.org
 
 http://www.cocos2d-x.org
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ****************************************************************************/

#import "IAPAppStore.h"

#define OUTPUT_LOG(...)     if (self.debug) NSLog(__VA_ARGS__);

@implementation IAPAppStore

@synthesize mIAPInfo;
//@synthesize mState;

@synthesize debug = __debug;

- (void) configDeveloperInfo : (NSMutableDictionary*) cpInfo
{
    NSLog(@"IAPAppStore::configDeveloperInfo");
}

- (void) payForProduct: (NSMutableDictionary*) IAPInfo
{
    NSLog(@"IAPAppStore::payForProduct");
    self.mIAPInfo = IAPInfo;
    NSLog(@"< IAPAppStore::IAP");
}

- (void) setDebugMode: (BOOL) debug
{
    NSLog(@"IAPAppStore::setDebugMode");
    self.debug = debug;
}

- (NSString*) getSDKVersion
{
    NSLog(@"IAPAppStore::getSDKVersion");
    return @"20130607";
}

- (NSString*) getPluginVersion
{
    NSLog(@"IAPAppStore::getPluginVersion");
    return @"0.2.0";
}

- (UIViewController *)getCurrentRootViewController {
    NSLog(@"IAPAppStore::getCurrentRootViewController");
    UIViewController *result = nil;
    return result;
}

- (void)showAlert:(NSString *)message
           result:(id)result
            error:(NSError *)error {
    NSLog(@"IAPAppStore::showAlert");
    NSString *alertMsg;
    NSString *alertTitle;
    UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:alertTitle
                                                        message:alertMsg
                                                       delegate:nil
                                              cancelButtonTitle:@"OK"
                                              otherButtonTitles:nil];
    [alertView show];
}

@end
