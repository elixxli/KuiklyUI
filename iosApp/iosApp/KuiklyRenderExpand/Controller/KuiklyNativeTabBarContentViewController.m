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

#import "KuiklyNativeTabBarContentViewController.h"
#import "KuiklyRenderViewController.h"
#import "KuiklyNativeTabBarController.h"
#import "UINavigationController+FDFullscreenPopGesture.h"

@interface KuiklyNativeTabBarContentViewController ()
@property (nonatomic, strong) KuiklyRenderViewController *renderViewController;
@end

@implementation KuiklyNativeTabBarContentViewController

- (instancetype)init {
    self = [super init];
    if (self) {
        _pageName = @"NativeTabBarHost";
        _pageData = @{};
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = UIColor.clearColor;

    KuiklyRenderViewController *renderViewController =
        [[KuiklyRenderViewController alloc] initWithPageName:self.pageName pageData:self.pageData ?: @{}];
    renderViewController.fd_prefersNavigationBarHidden = YES;
    self.renderViewController = renderViewController;

    [self addChildViewController:renderViewController];
    renderViewController.view.frame = self.view.bounds;
    renderViewController.view.autoresizingMask =
        UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    [self.view addSubview:renderViewController.view];
    [renderViewController didMoveToParentViewController:self];
}

- (void)viewDidLayoutSubviews {
    [super viewDidLayoutSubviews];
    [self syncHostedKuiklyLayoutIfNeeded];
}

- (void)syncHostedKuiklyLayoutIfNeeded {
    if (!self.renderViewController.isViewLoaded) {
        return;
    }
    self.renderViewController.view.frame = self.view.bounds;
    [self.renderViewController syncKuiklyLayoutToCurrentBounds];
}

- (void)sendNativeTabSelectedIndex:(NSInteger)index {
    [self.renderViewController sendPagerEvent:KuiklyNativeTabBarPagerEventTabSelected
                                       data:@{@"index": @(index)}];
}

@end
