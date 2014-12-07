/****************************************************************************
Copyright (c) 2012-2013 cocos2d-x.org

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
#import "AdsFlurry.h"
#import "Flurry.h"
#import "FlurryAds.h"
#import "AdsWrapper.h"

#define OUTPUT_LOG(...)     if (self.debug) NSLog(__VA_ARGS__);

@implementation AdsFlurry

@synthesize debug = __debug;
@synthesize spacesToDisplay = __SpacesToDisplay;

- (void) dealloc
{
    if (self.spacesToDisplay != nil) {
        [self.spacesToDisplay release];
        self.spacesToDisplay = nil;
    }
    [super dealloc];
}

#pragma mark Interfaces for ProtocolAds impl

- (void) configDeveloperInfo: (NSMutableDictionary*) devInfo
{
    NSString* appKey = [devInfo objectForKey:@"FlurryAppKey"];
    if (appKey) {
        [Flurry startSession:appKey];
    }
    [FlurryAds initialize:[AdsWrapper getCurrentRootViewController]];
    [FlurryAds setAdDelegate:self];
}

- (void) fetchAds: (NSMutableDictionary*) info position:(int) pos
{
    NSString* strSpaceID = [info objectForKey:@"FlurryAdsID"];
    if (! strSpaceID || [strSpaceID length] == 0) {
        OUTPUT_LOG(@"Value of 'FlurryAdsID' should not be empty");
        return;
    }
    
    NSString* strSize = [info objectForKey:@"FlurryAdsSize"];
    int size = [strSize intValue];
    if (size != 1 && size != 2 && size != 3) {
        OUTPUT_LOG(@"Value of 'FlurryAdsSize' should be one of '1', '2', '3' ");
        return;
    }
    
    UIViewController* controller = [AdsWrapper getCurrentRootViewController];
    if (controller) {
        UIView* mainView = controller.view;
        if(mainView) {
            //CGRect frame = mainView.bounds;
            CGRect frame = [mainView frame];
            //[FlurryAds fetchAndDisplayAdForSpace:strSpaceID view:controller.view viewController:controller size:size];
            [FlurryAds fetchAdForSpace:strSpaceID frame:frame size:size];
        }

    }
    
    
}

- (void) showAds: (NSMutableDictionary*) info position:(int) pos
{
    NSString* strSpaceID = [info objectForKey:@"FlurryAdsID"];
    if (! strSpaceID || [strSpaceID length] == 0) {
        OUTPUT_LOG(@"Value of 'FlurryAdsID' should not be empty");
        return;
    }

    NSString* strSize = [info objectForKey:@"FlurryAdsSize"];
    int size = [strSize intValue];
    if (size != 1 && size != 2 && size != 3) {
        OUTPUT_LOG(@"Value of 'FlurryAdsSize' should be one of '1', '2', '3' ");
        return;
    }

    if (nil == self.spacesToDisplay) {
        self.spacesToDisplay = [[NSMutableArray alloc] init];
    }
    [self.spacesToDisplay addObject:strSpaceID];

    UIViewController* controller = [AdsWrapper getCurrentRootViewController];
    if (controller) {
        [FlurryAds fetchAndDisplayAdForSpace:strSpaceID view:controller.view viewController:controller size:size];
    }
}

- (void) hideAds: (NSMutableDictionary*) info
{
    NSString* strSpaceID = [info objectForKey:@"FlurryAdsID"];
    if (! strSpaceID || [strSpaceID length] == 0) {
        OUTPUT_LOG(@"Value of 'FlurryAdsID' should not be empty");
        return;
    }

    if (nil != self.spacesToDisplay) {
        [self.spacesToDisplay removeObject:strSpaceID];
    }

    [FlurryAds removeAdFromSpace:strSpaceID];
}

- (void) queryPoints
{
    
}

- (void) spendPoints: (int) points
{
    
}

- (void) setDebugMode: (BOOL) isDebugMode
{
    OUTPUT_LOG(@"Flurry setDebugMode invoked(%d)", isDebugMode);
    self.debug = isDebugMode;
    [Flurry setDebugLogEnabled:isDebugMode];

    if (self.debug) {
        [FlurryAds enableTestAds:YES]; 
    }
}

- (NSString*) getSDKVersion
{
    return @"5.4.0";
}

- (NSString*) getPluginVersion
{
    return @"0.2.0";
}

#pragma mark Interfaces for FlurryAdDelegate impl

- (void) spaceDidReceiveAd:(NSString*)adSpace
{
    [AdsWrapper onAdsResult:self withRet:kAdsReceived  withMsg:adSpace];
}

- (void) spaceDidFailToReceiveAd:(NSString*)adSpace error:(NSError *)error
{
    NSString* strMsg = [[error userInfo] objectForKey:@"NSLocalizedDescription"];
    if (! strMsg) {
        strMsg = @"Failed to receive ads";
    }
    OUTPUT_LOG(@"spaceDidFailToReceiveAd:%@", strMsg);
    [AdsWrapper onAdsResult:self withRet:kUnknownError withMsg:adSpace];
}

- (BOOL) spaceShouldDisplay:(NSString*)adSpace interstitial:(BOOL)interstitial
{
    BOOL shouldDisplay = NO;
    if([self.spacesToDisplay containsObject:adSpace]) {
        [AdsWrapper onAdsResult:self withRet:kAdsShown withMsg:adSpace];
        shouldDisplay = YES;
    }
    return shouldDisplay;
}

- (void) spaceDidRender:(NSString *)space interstitial:(BOOL)interstitial {
    OUTPUT_LOG(@"spaceDidRender:%@ interstital:%s", space, interstitial ? "true" : "false");
}

- (void) spaceDidFailToRender:(NSString *)space error:(NSError *)error {
    NSString* strMsg = [[error userInfo] objectForKey:@"NSLocalizedDescription"];
    if (! strMsg) {
        strMsg = @"Failed to receive ads";
    }
    OUTPUT_LOG(@"spaceDidFailToRender:%@", strMsg);
    [AdsWrapper onAdsResult:self withRet:kUnknownError withMsg:space];
}

- (void) spaceWillDismiss:(NSString *)adSpace interstitial:(BOOL)interstitial
{
    OUTPUT_LOG(@"spaceWillDismiss:%@ interstital:%s", adSpace, interstitial ? "true" : "false");
}

- (void)spaceDidDismiss:(NSString *)adSpace interstitial:(BOOL)interstitial
{
    [AdsWrapper onAdsResult:self withRet:kAdsDismissed withMsg:adSpace];
}

- (void) spaceWillLeaveApplication:(NSString *)adSpace
{
    OUTPUT_LOG(@"spaceWillLeaveApplication:%@", adSpace);
}

- (void) spaceWillExpand:(NSString *)adSpace
{
    OUTPUT_LOG(@"spaceWillExpand:%@", adSpace);
}

- (void) spaceWillCollapse:(NSString *)adSpace
{
    OUTPUT_LOG(@"spaceWillCollapse:%@", adSpace);
}

- (void) spaceDidCollapse:(NSString *)adSpace
{
    OUTPUT_LOG(@"spaceDidCollapse:%@", adSpace);
}

- (void) spaceDidReceiveClick:(NSString*)adSpace
{
    OUTPUT_LOG(@"spaceDidReceiveClick:%@", adSpace);
}

- (void)videoDidFinish:(NSString *)adSpace
{
    [AdsWrapper onAdsResult:self withRet:kPointsSpendSucceed withMsg:adSpace];
}

@end
