//
//  CCStoreKitHelper.m
//

#import "CCStoreKitHelper.h"
#include "CCInAppBilling.h"

#define IS_MIN_IOS6 ([[[UIDevice currentDevice] systemVersion] floatValue] >= 6.0f)

@interface CCStoreKitHelper () <SKProductsRequestDelegate>
@end

@implementation CCStoreKitHelper

#pragma mark - Singleton
+(id) sharedHelper
{
    static CCStoreKitHelper *sharedHelper = nil;
    static dispatch_once_t once = 0;
    dispatch_once(&once, ^{sharedHelper = [[self alloc] init];});
    return sharedHelper;
}

#pragma mark - Initializer
-(id) init
{
    self = [super init];
    if (self)
    {
        _products = nil;
        _productsRequest = nil;
        _completionHandler = nil;
        _productIdentifiers = nil;
        _purchasedProductIdentifiers = nil;
    }
    return self;
}

- (void) addProductIdentifiers:(NSSet *)productIdentifiers {
 
    // Store product identifiers
    if(_productIdentifiers == nil) {
        _productIdentifiers = [NSMutableSet set];
    }
    [_productIdentifiers unionSet:productIdentifiers];
}

- (void) requestProductsWithCompletionHandler:(RequestProductsCompletionHandler)completionHandler {
 
    // 1
    _completionHandler = [completionHandler copy];
 
    // 2
    _productsRequest = [[SKProductsRequest alloc] initWithProductIdentifiers:_productIdentifiers];
    _productsRequest.delegate = self;
    [_productsRequest start];
}

- (void) requestProducts {
// Check for previously purchased products
    _purchasedProductIdentifiers = [NSMutableSet set];
    for (NSString * productIdentifier in _productIdentifiers) {
        BOOL productPurchased = [[NSUserDefaults standardUserDefaults] boolForKey:productIdentifier];
        if (productPurchased) {
            [_purchasedProductIdentifiers addObject:productIdentifier];
            NSLog(@"Previously purchased: %@", productIdentifier);
        } else {
            NSLog(@"Not purchased: %@", productIdentifier);
        }
    }

    [self requestProductsWithCompletionHandler:^(BOOL success, NSArray *products) {
        if (success) {
            _products = products;
        } else {
            NSLog(@"Handle error requesting products");
        }
    }];
}

-(void) inAppPurchase:(NSString*)name
{
    bool found = false;
    for (SKProduct * product in _products) {
        if(name == product.productIdentifier) {
          [self buyProduct:product];
          found = true;
            break;
        }
    }
    if(!found) {
        NSLog(@"Product not found");
        cocos2d::InAppBilling::getInstance()->onPurchaseFailed();
    }
    return;
}

#pragma mark - SKProductsRequestDelegate
 
- (void) productsRequest:(SKProductsRequest *)request didReceiveResponse:(SKProductsResponse *)response {
 
    NSLog(@"Loaded list of products...");
    _productsRequest = nil;
 
    NSArray * skProducts = response.products;
    for (SKProduct * skProduct in skProducts) {
        NSLog(@"Found product: %@ %@ %0.2f",
              skProduct.productIdentifier,
              skProduct.localizedTitle,
              skProduct.price.floatValue);
    }
 
    _completionHandler(YES, skProducts);
    _completionHandler = nil;
 
}
 
- (void)request:(SKRequest *)request didFailWithError:(NSError *)error {
 
    NSLog(@"Failed to load list of products.");
    _productsRequest = nil;
 
    _completionHandler(NO, nil);
    _completionHandler = nil;
 
}

- (BOOL)productPurchased:(NSString *)productIdentifier {
    return [_purchasedProductIdentifiers containsObject:productIdentifier];
}
 
- (void)buyProduct:(SKProduct *)product {
 
    NSLog(@"Buying %@...", product.productIdentifier);
 
    SKPayment * payment = [SKPayment paymentWithProduct:product];
    [[SKPaymentQueue defaultQueue] addPayment:payment];
 
}

- (void)restoreCompletedTransactions {
    [[SKPaymentQueue defaultQueue] restoreCompletedTransactions];
}

NSArray *_products;
SKProductsRequest * _productsRequest;
RequestProductsCompletionHandler _completionHandler;
NSMutableSet * _productIdentifiers;
NSMutableSet * _purchasedProductIdentifiers;


@end
