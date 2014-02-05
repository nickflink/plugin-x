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
#import "AnalyticsTestFlight.h"
#import "TestFlight.h"

#define OUTPUT_LOG(...)     if (self.debug) NSLog(__VA_ARGS__);

@implementation AnalyticsTestFlight

@synthesize debug = __debug;

- (void) startSession: (NSString*) appKey
{
    [TestFlight takeOff:appKey];
}

- (void) stopSession
{
    OUTPUT_LOG(@"TestFlight stopSession NOT used");
}

- (void) setSessionContinueMillis: (long) millis
{
    OUTPUT_LOG(@"TestFlight setSessionContinueMillis(%ld) NOT used", millis);
}

- (void) setCaptureUncaughtException: (BOOL) isEnabled
{
    OUTPUT_LOG(@"TestFlight setCaptureUncaughtException in flurry not available on iOS");
}

- (void) setDebugMode: (BOOL) isDebugMode
{
    OUTPUT_LOG(@"TestFlight setDebugMode invoked(%d)", isDebugMode);
    self.debug = isDebugMode;
}

- (void) logError: (NSString*) errorId withMsg:(NSString*) message
{
    OUTPUT_LOG(@"TestFlight logError(%@, %@) NOT used", errorId, message);
}

- (void) logEvent: (NSString*) eventId
{
    OUTPUT_LOG(@"TestFlight logEvent(%@) NOT used", eventId);
}

- (void) logEvent: (NSString*) eventId withParam:(NSMutableDictionary*) paramMap
{
    OUTPUT_LOG(@"TestFlight logEventWithParams(%@, %@) NOT used", eventId, [paramMap debugDescription]);
}

- (void) logTimedEventBegin: (NSString*) eventId
{
    OUTPUT_LOG(@"TestFlight logTimedEventBegin(%@) NOT used", eventId);
}

- (void) logTimedEventEnd: (NSString*) eventId
{
    OUTPUT_LOG(@"TestFlight logTimedEventEnd(%@) NOT used", eventId);
}

- (NSString*) getSDKVersion
{
    return @"2.2.1";
}

- (NSString*) getPluginVersion
{
    return @"0.0.1";
}

@end
