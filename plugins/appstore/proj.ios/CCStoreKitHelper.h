//
//  CCStoreKitHelper.h
//

#import <Foundation/Foundation.h>
#import <StoreKit/StoreKit.h>
UIKIT_EXTERN NSString *const IAPHelperProductPurchasedNotification;

typedef void (^RequestProductsCompletionHandler)(BOOL success, NSArray * products);

@interface CCStoreKitHelper : NSObject

/**
 * Always access class through this singleton
 * Call it once on application start to authenticate local player
 */
+ (id) sharedHelper;

- (void) addProductIdentifiers:(NSSet *)productIdentifiers;
- (void) requestProducts;
- (void) inAppPurchase:(NSString*)name;
- (void) buyProduct:(SKProduct *)product;
- (BOOL) productPurchased:(NSString *)productIdentifier;
- (void)restoreCompletedTransactions;


@property (nonatomic, assign, getter = isAuthenticated) BOOL authenticated;

@end
