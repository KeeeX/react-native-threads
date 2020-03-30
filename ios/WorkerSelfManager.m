#import "ThreadSelfManager.h"
#include <stdlib.h>

@implementation ThreadSelfManager

RCT_EXPORT_MODULE();

@synthesize bridge = _bridge;
@synthesize parentBridge = _parentBridge;
@synthesize threadId = _threadId;

RCT_EXPORT_METHOD(postMessage: (NSString *)message
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  if (self.parentBridge == nil) {
    NSLog(@"No parent bridge defined - abort sending thread message");
    reject(
      @"Error for paren bridge",
      @"No parent bridge defined - abort sending thread message",
      nil
    );
    return;
  }

  NSString *eventName = [NSString stringWithFormat:@"Thread%i", self.threadId];

  [self.parentBridge.eventDispatcher sendAppEventWithName:eventName
                                               body:message];
  resolve(nil);
}

@end
