//
//  CCGameKitHelper.m
//
//  Created by Alexander Blunck on 27.02.12.
//  Copyright (c) 2013 Alexander Blunck | Ablfx
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy
//  of this software and associated documentation files (the "Software"), to deal
//  in the Software without restriction, including without limitation the rights
//  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//  copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:
//
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//  THE SOFTWARE.

#import <CommonCrypto/CommonCryptor.h>
#import "CCGameKitHelper.h"
#import "ShareWrapper.h"

#define IS_MIN_IOS6 ([[[UIDevice currentDevice] systemVersion] floatValue] >= 6.0f)

@interface CCGameKitHelper () <GKLeaderboardViewControllerDelegate, GKAchievementViewControllerDelegate>
@end

@implementation CCGameKitHelper

#pragma mark - Singleton
+(id) sharedHelper
{
    static CCGameKitHelper *sharedHelper = nil;
    static dispatch_once_t once = 0;
    dispatch_once(&once, ^{sharedHelper = [[self alloc] init];});
    return sharedHelper;
}



#pragma mark - Initializer
-(id) init
{
    self = [super init];
    if(self) {
        [self setSecretKey:@"REPLACE WITH YOUR SECURE KEY"];
    }
    return self;
}



#pragma mark - Authenticate
-(void) authenticatePlayer
{
    GKLocalPlayer *player = [GKLocalPlayer localPlayer];
    
    void (^authBlock)(UIViewController *, NSError *) = ^(UIViewController *viewController, NSError *error) {
        
        if (viewController)
        {
            [[self topViewController] presentViewController:viewController animated:YES completion:nil];
        }
        
        if ([[GKLocalPlayer localPlayer] isAuthenticated])
        {
            self.authenticated = YES;
            if (ABGAMEKITHELPER_LOGGING) NSLog(@"CCGameKitHelper: Player successfully authenticated.");
            //Report possible cached scores / achievements
            [self reportCachedAchievements];
            [self reportCachedScores];
            //cocos2d::GameServices::getInstance()->onSignInSucceeded();
            [ShareWrapper onShareResult:self withRet:kShareFail withMsg:@"Login Failed"];
        }
        
        if (error)
        {
            self.authenticated = NO;
            if (ABGAMEKITHELPER_LOGGING) NSLog(@"CCGameKitHelper: ERROR -> Player didn't authenticate");
            //cocos2d::GameServices::getInstance()->onSignInFailed();
        }
    };
    
    //iOS 6.x +
    if (IS_MIN_IOS6)
    {
        [player setAuthenticateHandler:authBlock];
    }
    //iOS 5.0
    else
    {
        [player authenticateWithCompletionHandler:^(NSError *error)
        {
            authBlock(nil, error);
        }];
    }
}

#pragma mark - Leaderboard
-(void) reportScore:(long long)aScore forLeaderboard:(NSString*)leaderboardId
{
    GKScore *score = [[GKScore alloc] initWithCategory:leaderboardId];
    score.value = aScore;
    
    [score reportScoreWithCompletionHandler:^(NSError *error) {
        if (!error)
        {
            if (ABGAMEKITHELPER_LOGGING) NSLog(@"CCGameKitHelper: Reported score (%lli) to %@ successfully.", score.value, leaderboardId);
        }
        else
        {
            [self cacheScore:score];
            if (ABGAMEKITHELPER_LOGGING) NSLog(@"CCGameKitHelper: ERROR -> Reporting score (%lli) to %@ failed, caching...", score.value, leaderboardId);
        }
    }];
}

-(void) showLeaderboard:(NSString*)leaderboardId
{
    GKLeaderboardViewController *viewController = [GKLeaderboardViewController new];
    viewController.leaderboardDelegate = self;
    if (leaderboardId)
    {
        viewController.category = leaderboardId;
    }
    
    [[self topViewController] presentViewController:viewController animated:YES completion:nil];
}



#pragma mark - Achievements
-(void) reportAchievement:(NSString*)achievementId percentComplete:(NSNumber*)percentComplete
{
    float percent = [percentComplete floatValue];
    if (percent > 100.0f) percent = 100.0f;
    
    //Mark achievement as completed locally
    if (percent == 100)
    {
        [self saveBool:YES key:achievementId];
    }
    
    GKAchievement *achievement = [[GKAchievement alloc] initWithIdentifier:achievementId];
    
    if (achievement)
    {
        achievement.percentComplete = percent;
        
        [achievement reportAchievementWithCompletionHandler:^(NSError *error) {
            if (!error)
            {
                if (ABGAMEKITHELPER_LOGGING) NSLog(@"CCGameKitHelper: Achievement (%@) with %f%% progress reported", achievement.identifier, achievement.percentComplete);
            }
            else
            {
                [self cacheAchievement:achievement];
                if (ABGAMEKITHELPER_LOGGING) NSLog(@"CCGameKitHelper: ERROR -> Reporting achievement (%@) with %f%% progress failed, caching...", achievement.identifier, achievement.percentComplete);
            }
        }];
    }
}

-(void) showAchievements
{
    GKAchievementViewController *viewController = [GKAchievementViewController new];
    viewController.achievementDelegate = self;
    
    [[self topViewController] presentViewController:viewController animated:YES completion:nil];
}

-(void) resetAchievements
{
    [GKAchievement resetAchievementsWithCompletionHandler:^(NSError *error) {
        if (!error)
        {
            if (ABGAMEKITHELPER_LOGGING) NSLog(@"CCGameKitHelper: Achievements reset successfully.");
        }
        else
        {
            if (ABGAMEKITHELPER_LOGGING) NSLog(@"CCGameKitHelper: Failed to reset achievements.");
        }
    }];
}



#pragma mark - Notifications
-(void) showNotification:(NSString*)title message:(NSString*)message identifier:(NSString*)achievementId
{
    //Show notification only if it hasn't been achieved yet
    if (![self boolForKey:achievementId])
    {
        [GKNotificationBanner showBannerWithTitle:title message:message completionHandler:nil];
    }
}



#pragma mark - Caching
#pragma mark - Caching Scores
-(void) cacheScore:(GKScore*)aScore
{
    //Retrieve cached scores
    NSMutableArray *scores = [self objectForKey:@"cachedScores"];
    
    //Add new score to array
    [scores addObject:aScore];
    
    //Save scores to persistant storage
    [self saveObject:scores key:@"cachedScores"];
}

-(void) reportCachedScores
{
    //Retrieve cached scores
    NSMutableArray *scores = [self objectForKey:@"cachedScores"];
    
    if (scores.count == 0) {
        return;
    }
    
    if (ABGAMEKITHELPER_LOGGING) NSLog(@"CCGameKitHelper: Attempting to report %lu cached scores...", (unsigned long)scores.count);
    
    //iOS 6.x+
    if (IS_MIN_IOS6)
    {
        [GKScore reportScores:scores withCompletionHandler:^(NSError *error) {
            if (!error)
            {
                [self removeAllCachedScores];
                if (ABGAMEKITHELPER_LOGGING) NSLog(@"CCGameKitHelper: Reported %lu cached score(s) successfully.", (unsigned long)scores.count);
            }
            else
            {
                if (ABGAMEKITHELPER_LOGGING) NSLog(@"CCGameKitHelper: ERROR -> Failed to report %lu cached score(s).", (unsigned long)scores.count);
            }
        }];
    }
    //iOS 5.1
    else
    {
        for (GKScore *score in scores)
        {
            [score reportScoreWithCompletionHandler:^(NSError *error) {
                if (!error)
                {
                    [self removeCachedScore:score];
                    if (ABGAMEKITHELPER_LOGGING) NSLog(@"CCGameKitHelper: Reported cached score (%@ : %lli) successfully.", score.category, score.value);
                }
                else
                {
                    if (ABGAMEKITHELPER_LOGGING) NSLog(@"CCGameKitHelper: ERROR -> Failed to report cached score (%@ : %lli).", score.category, score.value);
                }
            }];
        }
    }
}

-(void) removeCachedScore:(GKScore*)score
{
    NSMutableArray *scores = [self objectForKey:@"cachedScores"];
    [scores removeObject:score];
    [self saveObject:scores key:@"cachedScores"];
}

-(void) removeAllCachedScores
{
    [self saveObject:[NSMutableArray new] key:@"cachedScores"];
}


#pragma mark - Caching Achievements
-(void) cacheAchievement:(GKAchievement*)achievement
{
    //Retrieve cached achievements
    NSMutableArray *achievements = [self objectForKey:@"cachedAchievements"];
    
    //Add new achievment to array
    [achievements addObject:achievement];
    
    //Save achievement to persistant storage
    [self saveObject:achievements key:@"cachedAchievements"];
}

-(void) reportCachedAchievements
{
    //Retrieve cached achievements
    NSMutableArray *achievements = [self objectForKey:@"cachedAchievements"];
    
    if (achievements.count == 0) {
        return;
    }
    
    if (ABGAMEKITHELPER_LOGGING) NSLog(@"CCGameKitHelper: Attempting to report %lu cached achievements...", (unsigned long)achievements.count);
    
    //iOS 6.x +
    if (IS_MIN_IOS6)
    {
        [GKAchievement reportAchievements:achievements withCompletionHandler:^(NSError *error) {
            if (!error)
            {
                [self removeAllCachedAchievements];
                if (ABGAMEKITHELPER_LOGGING) NSLog(@"CCGameKitHelper: Reported %lu cached achievement(s) successfully.", (unsigned long)achievements.count);
            }
            else
            {
                if (ABGAMEKITHELPER_LOGGING) NSLog(@"CCGameKitHelper: ERROR -> Failed to report %lu cached achievement(s).", (unsigned long)achievements.count);
            }
        }];
    }
    //iOS 5.1
    else
    {
        for (GKAchievement *achievement in achievements)
        {
            [achievement reportAchievementWithCompletionHandler:^(NSError *error) {
                if (!error)
                {
                    [self removeCachedAchievement:achievement];
                    if (ABGAMEKITHELPER_LOGGING) NSLog(@"CCGameKitHelper: Reported cached achievement (%@) successfully.", achievement.identifier);
                }
                else
                {
                    if (ABGAMEKITHELPER_LOGGING) NSLog(@"CCGameKitHelper: ERROR -> Failed to report cached achievement (%@).", achievement.identifier);
                }
            }];
        }
    }
}

-(void) removeCachedAchievement:(GKAchievement*)achievement
{
    NSMutableArray *achievements = [self objectForKey:@"cachedAchievements"];
    [achievements removeObject:achievement];
    [self saveObject:achievements key:@"cachedAchievements"];
}

-(void) removeAllCachedAchievements
{
    [self saveObject:[NSMutableArray new] key:@"cachedAchievements"];
}



#pragma mark - Data Persistance
-(NSString*) filePath
{
    NSString *fileExt = @".abgk";
    NSString *fileName = [NSString stringWithFormat:@"%@%@", [[self appName] lowercaseString], fileExt];
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    NSString *path = [documentsDirectory stringByAppendingPathComponent:fileName];
    return path;
}

-(NSMutableDictionary*) dataDictionary
{
    NSData *binaryFile = [NSData dataWithContentsOfFile:[self filePath]];
    NSMutableDictionary *dictionary = nil;
    
    if (binaryFile == nil)
    {
        dictionary = [NSMutableDictionary dictionary];
    }
    else
    {
        NSData *decryptedData = [self decryptData:binaryFile withKey:self.secretKey];
        dictionary = [NSKeyedUnarchiver unarchiveObjectWithData:decryptedData];
    }
    
    return dictionary;
}

-(void) saveData:(NSData*)data key:(NSString*)key
{
    BOOL fileExists = [[NSFileManager defaultManager] fileExistsAtPath:[self filePath]];
    NSMutableDictionary *tempDic = nil;
    if (fileExists == NO)
    {
        tempDic = [NSMutableDictionary new];
    } else
    {
        tempDic = [self dataDictionary];
    }
    
    [tempDic setObject:data forKey:key];
    
    NSData *dicData = [NSKeyedArchiver archivedDataWithRootObject:tempDic];
    NSData *encryptedData = [self encryptData:dicData withKey:self.secretKey];
    [encryptedData writeToFile:[self filePath] atomically:YES];
}

-(NSData*) dataForKey:(NSString*)key
{
    NSMutableDictionary *tempDic = [self dataDictionary];
    NSData *loadedData = [tempDic objectForKey:key];
    
    if (loadedData)
    {
        return loadedData;
    }
    
    return nil;
}

-(void) saveObject:(id<NSCoding>)object key:(NSString*)key
{
    NSData *data = [NSKeyedArchiver archivedDataWithRootObject:object];
    [self saveData:data key:key];
}

-(id) objectForKey:(NSString*)key
{
    NSData *data = [self dataForKey:key];
    if (data)
    {
        return [NSKeyedUnarchiver unarchiveObjectWithData:data];
    }
    return nil;
}

-(void) saveBool:(BOOL)boolean key:(NSString*)key
{
    NSNumber *number = [NSNumber numberWithBool:boolean];
    NSData *data = [NSKeyedArchiver archivedDataWithRootObject:number];
    [self saveData:data key:key];
}

-(BOOL) boolForKey:(NSString*)key
{
    NSData *data = [self dataForKey:key];
    if (data)
    {
        return [[NSKeyedUnarchiver unarchiveObjectWithData:data] boolValue];
    }
    return NO;
}



#pragma mark - Helper
-(NSString*) appName
{
    NSString *bundlePath = [[[NSBundle mainBundle] bundleURL] lastPathComponent];
    return [[bundlePath stringByDeletingPathExtension] lowercaseString];
}

-(UIViewController*) topViewController
{
    UIViewController *topController = [UIApplication sharedApplication].keyWindow.rootViewController;
    while (topController.presentedViewController)
    {
        topController = topController.presentedViewController;
    }
    return topController;
}

-(NSData*) makeCryptedVersionOfData:(NSData*)data withKeyData:(const void*)keyData ofLength:(NSUInteger) keyLength decrypt:(bool)decrypt
{
	int keySize = kCCKeySizeAES256;
    char key[kCCKeySizeAES256];
	bzero(key, sizeof(key));
	memcpy(key, keyData, keyLength > keySize ? keySize : keyLength);
    
	size_t bufferSize = [data length] + kCCBlockSizeAES128;
	void* buffer = malloc(bufferSize);
    
	size_t dataUsed;
    
	CCCryptorStatus status = CCCrypt(decrypt ? kCCDecrypt : kCCEncrypt,
									 kCCAlgorithmAES128,
									 kCCOptionPKCS7Padding | kCCOptionECBMode,
									 key, keySize,
									 NULL,
									 [data bytes], [data length],
									 buffer, bufferSize,
									 &dataUsed);
    
	switch(status)
	{
		case kCCSuccess:
			return [NSData dataWithBytesNoCopy:buffer length:dataUsed];
		default:
			if (ABGAMEKITHELPER_LOGGING) NSLog(@"CCGameKitHelper: ERROR -> Failed to encrypt!");
	}
    
	free(buffer);
	return nil;
}

- (NSData*) encryptData:(NSData*)data withKey:(NSString*)key
{
    NSData *keyData = [key dataUsingEncoding:NSUTF8StringEncoding];
	return [self makeCryptedVersionOfData:data withKeyData:[keyData bytes] ofLength:[keyData length] decrypt:false];
}

- (NSData*) decryptData:(NSData*)data withKey:(NSString*)key
{
	NSData *keyData = [key dataUsingEncoding:NSUTF8StringEncoding];
    return [self makeCryptedVersionOfData:data withKeyData:[keyData bytes] ofLength:[keyData length] decrypt:true];
}



#pragma mark - GKLeaderboardViewControllerDelegate
-(void) leaderboardViewControllerDidFinish:(GKLeaderboardViewController*)viewController
{
    [viewController dismissViewControllerAnimated:YES completion:nil];
}



#pragma mark - GKAchievementViewControllerDelegate
-(void) achievementViewControllerDidFinish:(GKAchievementViewController *)viewController
{
    [viewController dismissViewControllerAnimated:YES completion:nil];
}

@end
