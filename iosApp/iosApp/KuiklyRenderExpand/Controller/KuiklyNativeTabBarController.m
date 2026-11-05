/*
 * Tencent is pleased to support the open source community by making KuiklyUI
 * available.
 * Copyright (C) 2025 Tencent. All rights reserved.
 * Licensed under the License of KuiklyUI;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://github.com/Tencent-TDS/KuiklyUI/blob/main/LICENSE
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#import "KuiklyNativeTabBarController.h"
#import "KuiklyRenderViewController.h"
#import "UINavigationController+FDFullscreenPopGesture.h"

NSString *const KuiklyNativeTabBarPagerEventTabSelected = @"NativeTabBarSelected";
NSString *const KuiklyNativeTabBarPagerEventTabBarSafeArea = @"NativeTabBarSafeAreaInsets";

static NSString *const kKuiklyHostPageName = @"NativeTabBarHost";

/// 手势先问 Tab。命中在 Tab 上就留下，其余穿透给 Kuikly。
@interface KuiklyNativeTabBarShellView : UIView
@property (nonatomic, weak) UIView *kuiklyContentView;
@property (nonatomic, weak) UIView *tabBarHostView;
@property (nonatomic, weak) UITabBar *systemTabBar;
@end

@implementation KuiklyNativeTabBarShellView

- (BOOL)p_hitIsTabChrome:(UIView *)hit host:(UIView *)host {
    if (hit == nil || hit == host) {
        return NO;
    }
    UIView *view = hit;
    while (view != nil && view != host) {
        if (view == self.systemTabBar || [view isKindOfClass:[UITabBar class]]) {
            return YES;
        }
        view = view.superview;
    }
    CGRect frame = [host convertRect:hit.bounds fromView:hit];
    BOOL fillsHost = CGRectGetWidth(frame) >= CGRectGetWidth(host.bounds) * 0.85
                     && CGRectGetHeight(frame) >= CGRectGetHeight(host.bounds) * 0.85;
    return !fillsHost;
}

- (UIView *)p_kuiklyHitAtPoint:(CGPoint)point event:(UIEvent *)event {
    UIView *kuiklyView = self.kuiklyContentView;
    if (kuiklyView == nil || kuiklyView.hidden || kuiklyView.alpha <= 0.01) {
        return nil;
    }
    CGPoint inKuikly = [self convertPoint:point toView:kuiklyView];
    return [kuiklyView hitTest:inKuikly withEvent:event];
}

- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event {
    if (!self.userInteractionEnabled || self.hidden || self.alpha <= 0.01) {
        return nil;
    }
    if (![self pointInside:point withEvent:event]) {
        return nil;
    }

    UIView *tabHost = self.tabBarHostView;
    if (tabHost && !tabHost.hidden && tabHost.alpha > 0.01 && tabHost.userInteractionEnabled) {
        CGPoint inHost = [self convertPoint:point toView:tabHost];
        UIView *tabHit = [tabHost hitTest:inHost withEvent:event];
        if ([self p_hitIsTabChrome:tabHit host:tabHost]) {
            return tabHit;
        }
    }

    UITabBar *tabBar = self.systemTabBar;
    if (tabBar && !tabBar.hidden && tabBar.alpha > 0.01 && tabBar.userInteractionEnabled) {
        CGPoint inTabBar = [self convertPoint:point toView:tabBar];
        if ([tabBar pointInside:inTabBar withEvent:event]) {
            UIView *tabHit = [tabBar hitTest:inTabBar withEvent:event];
            if (tabHit) {
                return tabHit;
            }
        }
    }

    return [self p_kuiklyHitAtPoint:point event:event];
}

@end

@interface KuiklyNativeTabBarPlaceholderViewController : UIViewController
@end

@implementation KuiklyNativeTabBarPlaceholderViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = UIColor.clearColor;
    self.view.userInteractionEnabled = NO;
}

@end

@interface KuiklyNativeTabBarController () <UITabBarControllerDelegate>
@property (nonatomic, strong) UITabBarController *tabBarController;
@property (nonatomic, strong) KuiklyRenderViewController *kuiklyHost;
@property (nonatomic, assign) BOOL suppressTabBarDelegatePagerEvent;
@property (nonatomic, assign) BOOL pendingPaddingPush;
@property (nonatomic, assign) BOOL isRotating;
@end

@implementation KuiklyNativeTabBarController

- (void)loadView {
    self.view = [[KuiklyNativeTabBarShellView alloc] initWithFrame:UIScreen.mainScreen.bounds];
    self.view.backgroundColor = [UIColor systemBackgroundColor];
}

- (instancetype)init {
    self = [super initWithNibName:nil bundle:nil];
    if (self) {
        _kuiklyHost =
            [[KuiklyRenderViewController alloc] initWithPageName:kKuiklyHostPageName pageData:@{}];
        _kuiklyHost.fd_prefersNavigationBarHidden = YES;

        _tabBarController = [[UITabBarController alloc] init];
        _tabBarController.delegate = self;
        _tabBarController.viewControllers = @[
            [self p_makePlaceholderWithTitle:@"首页"
                                   imageName:@"house.fill"
                                         tag:0],
            [self p_makePlaceholderWithTitle:@"发现"
                                   imageName:@"magnifyingglass"
                                         tag:1],
            [self p_makePlaceholderWithTitle:@"我的"
                                   imageName:@"person.fill"
                                         tag:2],
        ];
        [self p_configureTabBarChromeOnBar:_tabBarController.tabBar];
    }
    return self;
}

+ (UIImage *)p_tabImageNamed:(NSString *)name {
    UIImage *image = [UIImage systemImageNamed:name];
    if (image) {
        return image;
    }
    UIGraphicsBeginImageContextWithOptions(CGSizeMake(1.0, 1.0), NO, 0.0);
    UIImage *placeholder = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return placeholder ?: [UIImage new];
}

- (UIViewController *)p_makePlaceholderWithTitle:(NSString *)title
                                       imageName:(NSString *)imageName
                                             tag:(NSInteger)tag {
    KuiklyNativeTabBarPlaceholderViewController *vc =
        [[KuiklyNativeTabBarPlaceholderViewController alloc] init];
    vc.tabBarItem = [[UITabBarItem alloc] initWithTitle:title
                                                    image:[KuiklyNativeTabBarController p_tabImageNamed:imageName]
                                                      tag:tag];
    return vc;
}

- (void)p_configureTabBarChromeOnBar:(UITabBar *)tabBar {
    tabBar.tintColor = [UIColor systemBlueColor];
    tabBar.unselectedItemTintColor = [UIColor labelColor];
    tabBar.translucent = YES;
    tabBar.backgroundColor = UIColor.clearColor;
    tabBar.backgroundImage = [UIImage new];
    tabBar.shadowImage = [UIImage new];
    tabBar.itemPositioning = UITabBarItemPositioningFill;

    if (@available(iOS 15.0, *)) {
        UITabBarAppearance *appearance = [[UITabBarAppearance alloc] init];
        [appearance configureWithTransparentBackground];
        appearance.backgroundColor = UIColor.clearColor;
        appearance.shadowColor = UIColor.clearColor;
        appearance.backgroundEffect = nil;

        UITabBarItemAppearance *itemAppearance = [[UITabBarItemAppearance alloc] init];
        itemAppearance.normal.iconColor = [UIColor labelColor];
        itemAppearance.normal.titleTextAttributes =
            @{NSForegroundColorAttributeName : [UIColor secondaryLabelColor]};
        itemAppearance.selected.iconColor = [UIColor systemBlueColor];
        itemAppearance.selected.titleTextAttributes =
            @{NSForegroundColorAttributeName : [UIColor systemBlueColor]};
        appearance.stackedLayoutAppearance = itemAppearance;
        appearance.inlineLayoutAppearance = itemAppearance;
        appearance.compactInlineLayoutAppearance = itemAppearance;

        tabBar.standardAppearance = appearance;
        tabBar.scrollEdgeAppearance = appearance;
    }
}

- (void)viewDidLoad {
    [super viewDidLoad];

    [self addChildViewController:self.kuiklyHost];
    self.kuiklyHost.view.frame = self.view.bounds;
    self.kuiklyHost.view.autoresizingMask =
        UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    [self.view addSubview:self.kuiklyHost.view];
    [self.kuiklyHost didMoveToParentViewController:self];

    [self addChildViewController:self.tabBarController];
    self.tabBarController.view.frame = self.view.bounds;
    self.tabBarController.view.autoresizingMask =
        UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    self.tabBarController.view.backgroundColor = UIColor.clearColor;
    self.tabBarController.view.userInteractionEnabled = YES;
    [self.view addSubview:self.tabBarController.view];
    [self.tabBarController didMoveToParentViewController:self];
    self.tabBarController.tabBar.userInteractionEnabled = YES;
    [self p_updateShellTargets];
}

- (void)p_updateShellTargets {
    KuiklyNativeTabBarShellView *shell = (KuiklyNativeTabBarShellView *)self.view;
    if (![shell isKindOfClass:[KuiklyNativeTabBarShellView class]]) {
        return;
    }
    shell.kuiklyContentView = self.kuiklyHost.view;
    shell.tabBarHostView = self.tabBarController.view;
    shell.systemTabBar = self.tabBarController.tabBar;
}

- (void)syncTabBarSelectedIndexFromKuikly:(NSInteger)index {
    if (!self.isViewLoaded || !self.tabBarController.isViewLoaded) {
        return;
    }
    if (index < 0 || index >= (NSInteger)self.tabBarController.viewControllers.count) {
        return;
    }
    if ((NSInteger)self.tabBarController.selectedIndex == index) {
        return;
    }
    self.suppressTabBarDelegatePagerEvent = YES;
    self.tabBarController.selectedIndex = (NSUInteger)index;
    self.suppressTabBarDelegatePagerEvent = NO;
}

- (void)p_logChildSafeAreasWithReason:(NSString *)reason {
    if (!self.isViewLoaded || !self.kuiklyHost.isViewLoaded || !self.tabBarController.isViewLoaded) {
        return;
    }

    UIEdgeInsets shellViewSafe = UIEdgeInsetsZero;
    UIEdgeInsets shellAdditional = UIEdgeInsetsZero;
    if (@available(iOS 11.0, *)) {
        shellViewSafe = self.view.safeAreaInsets;
        shellAdditional = self.additionalSafeAreaInsets;
    }

    UIEdgeInsets kuiklyViewSafe = UIEdgeInsetsZero;
    UIEdgeInsets kuiklyAdditional = UIEdgeInsetsZero;
    if (@available(iOS 11.0, *)) {
        kuiklyViewSafe = self.kuiklyHost.view.safeAreaInsets;
        kuiklyAdditional = self.kuiklyHost.additionalSafeAreaInsets;
    }

    UIEdgeInsets tabViewSafe = UIEdgeInsetsZero;
    UIEdgeInsets tabAdditional = UIEdgeInsetsZero;
    if (@available(iOS 11.0, *)) {
        tabViewSafe = self.tabBarController.view.safeAreaInsets;
        tabAdditional = self.tabBarController.additionalSafeAreaInsets;
    }

    UITabBar *tabBar = self.tabBarController.tabBar;
    NSLog(
        @"[NativeTabBar][SafeArea][%@] shell.view {top:%.1f left:%.1f bottom:%.1f right:%.1f} "
        @"shell.additional {top:%.1f left:%.1f bottom:%.1f right:%.1f} bounds:%@",
        reason,
        shellViewSafe.top,
        shellViewSafe.left,
        shellViewSafe.bottom,
        shellViewSafe.right,
        shellAdditional.top,
        shellAdditional.left,
        shellAdditional.bottom,
        shellAdditional.right,
        NSStringFromCGRect(self.view.bounds));
    NSLog(
        @"[NativeTabBar][SafeArea][%@] KuiklyRenderViewController.view {top:%.1f left:%.1f bottom:%.1f "
        @"right:%.1f} additional {top:%.1f left:%.1f bottom:%.1f right:%.1f} frame:%@",
        reason,
        kuiklyViewSafe.top,
        kuiklyViewSafe.left,
        kuiklyViewSafe.bottom,
        kuiklyViewSafe.right,
        kuiklyAdditional.top,
        kuiklyAdditional.left,
        kuiklyAdditional.bottom,
        kuiklyAdditional.right,
        NSStringFromCGRect(self.kuiklyHost.view.frame));
    NSLog(
        @"[NativeTabBar][SafeArea][%@] UITabBarController.view {top:%.1f left:%.1f bottom:%.1f right:%.1f} "
        @"additional {top:%.1f left:%.1f bottom:%.1f right:%.1f} tabBar.frame:%@ selectedIndex:%lu",
        reason,
        tabViewSafe.top,
        tabViewSafe.left,
        tabViewSafe.bottom,
        tabViewSafe.right,
        tabAdditional.top,
        tabAdditional.left,
        tabAdditional.bottom,
        tabAdditional.right,
        NSStringFromCGRect(tabBar.frame),
        (unsigned long)self.tabBarController.selectedIndex);
}

/// 列表 padding 直接用当前 Tab 子 VC 的 safeAreaInsets（系统已含底栏 / 竖轨）。
- (UIEdgeInsets)p_listPaddingSafeAreaInsets {
    UIViewController *contentVC = self.tabBarController.selectedViewController;
    if (contentVC == nil) {
        return UIEdgeInsetsZero;
    }
    if (!contentVC.isViewLoaded) {
        [contentVC loadViewIfNeeded];
    }
    [self.tabBarController.view layoutIfNeeded];
    [contentVC.view layoutIfNeeded];
    if (@available(iOS 11.0, *)) {
        return contentVC.view.safeAreaInsets;
    }
    return UIEdgeInsetsZero;
}

- (void)p_schedulePaddingPush:(NSString *)reason {
    if (self.pendingPaddingPush) {
        return;
    }
    self.pendingPaddingPush = YES;
    __weak typeof(self) weakSelf = self;
    dispatch_async(dispatch_get_main_queue(), ^{
        typeof(self) strongSelf = weakSelf;
        if (!strongSelf) {
            return;
        }
        strongSelf.pendingPaddingPush = NO;
        [strongSelf p_pushTabBarControllerSafeAreaToKuikly:reason];
    });
}

- (void)p_pushTabBarControllerSafeAreaToKuikly:(NSString *)reason {
    if (self.isRotating) {
        NSLog(@"[NativeTabBar][PushInsets][%@] skip rotating", reason);
        return;
    }
    if (!self.kuiklyHost.isViewLoaded || !self.tabBarController.isViewLoaded) {
        NSLog(@"[NativeTabBar][PushInsets][%@] skip hostLoaded:%d tbcLoaded:%d",
              reason,
              self.kuiklyHost.isViewLoaded,
              self.tabBarController.isViewLoaded);
        return;
    }

    [self.view layoutIfNeeded];
    [self.tabBarController.view layoutIfNeeded];

    UIEdgeInsets tabViewSafe = [self p_listPaddingSafeAreaInsets];
    UIEdgeInsets shellSafe = UIEdgeInsetsZero;
    if (@available(iOS 11.0, *)) {
        shellSafe = self.view.safeAreaInsets;
    }
    UIViewController *contentVC = self.tabBarController.selectedViewController;
    UITabBar *tabBar = self.tabBarController.tabBar;
    static NSInteger pushSeq = 0;
    pushSeq += 1;
    NSLog(@"[NativeTabBar][PushInsets][%ld][%@] bounds:%@ tabContent {l:%.1f b:%.1f r:%.1f} shell {l:%.1f b:%.1f r:%.1f} tabBar:%@ contentInWindow:%d",
          (long)pushSeq,
          reason,
          NSStringFromCGRect(self.view.bounds),
          tabViewSafe.left,
          tabViewSafe.bottom,
          tabViewSafe.right,
          shellSafe.left,
          shellSafe.bottom,
          shellSafe.right,
          NSStringFromCGRect(tabBar.frame),
          contentVC.view.window != nil);
    [self.kuiklyHost sendPagerEvent:KuiklyNativeTabBarPagerEventTabBarSafeArea
                               data:@{
                                   @"top": @(tabViewSafe.top),
                                   @"left": @(tabViewSafe.left),
                                   @"bottom": @(tabViewSafe.bottom),
                                   @"right": @(tabViewSafe.right),
                               }];
}

- (void)viewSafeAreaInsetsDidChange {
    [super viewSafeAreaInsetsDidChange];
    [self p_pushTabBarControllerSafeAreaToKuikly:@"safeAreaChanged"];
    [self p_logChildSafeAreasWithReason:@"viewSafeAreaInsetsDidChange"];
}

- (void)viewDidLayoutSubviews {
    [super viewDidLayoutSubviews];

    CGRect bounds = self.view.bounds;
    self.kuiklyHost.view.frame = bounds;
    self.tabBarController.view.frame = bounds;

    self.kuiklyHost.additionalSafeAreaInsets = UIEdgeInsetsZero;
    [self p_updateShellTargets];
    [self p_pushTabBarControllerSafeAreaToKuikly:@"layout"];
    [self p_schedulePaddingPush:@"layout+1"];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    if (self.navigationController != nil) {
        self.navigationItem.hidesBackButton = YES;
        self.navigationItem.leftBarButtonItems = @[];
        self.navigationItem.leftItemsSupplementBackButton = NO;
    }
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    [self p_pushTabBarControllerSafeAreaToKuikly:@"appear"];
    [self p_schedulePaddingPush:@"appear+1"];
    [self p_logChildSafeAreasWithReason:@"viewDidAppear"];
}

- (void)viewWillTransitionToSize:(CGSize)size
       withTransitionCoordinator:(id<UIViewControllerTransitionCoordinator>)coordinator {
    [super viewWillTransitionToSize:size withTransitionCoordinator:coordinator];
    self.isRotating = YES;
    [coordinator animateAlongsideTransition:^(__unused id<UIViewControllerTransitionCoordinatorContext> context) {
        [self.view setNeedsLayout];
        [self.view layoutIfNeeded];
    }
                                 completion:^(__unused id<UIViewControllerTransitionCoordinatorContext> context) {
        self.isRotating = NO;
        [self p_pushTabBarControllerSafeAreaToKuikly:@"transition"];
        [self p_schedulePaddingPush:@"transition+1"];
        [self p_logChildSafeAreasWithReason:@"transitionComplete"];
    }];
}

#pragma mark - UITabBarControllerDelegate

- (void)tabBarController:(UITabBarController *)tabBarController
 didSelectViewController:(UIViewController *)viewController {
    if (self.suppressTabBarDelegatePagerEvent) {
        return;
    }
    NSInteger index = [tabBarController.viewControllers indexOfObject:viewController];
    if (index == NSNotFound) {
        return;
    }
    [self.kuiklyHost sendPagerEvent:KuiklyNativeTabBarPagerEventTabSelected
                               data:@{@"index": @(index)}];
    [self p_logChildSafeAreasWithReason:[NSString stringWithFormat:@"tabSelect_%ld", (long)index]];
}

@end
