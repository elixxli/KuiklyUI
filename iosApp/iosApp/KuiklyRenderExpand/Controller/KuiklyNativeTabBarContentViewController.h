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

#import <UIKit/UIKit.h>

@class KuiklyRenderViewController;

NS_ASSUME_NONNULL_BEGIN

/// 仅负责挂载一个 [KuiklyRenderViewController]，避免 TabBarController 直接 child Kuikly VC 的生命周期问题。
@interface KuiklyNativeTabBarContentViewController : UIViewController

@property (nonatomic, copy) NSString *pageName;
@property (nonatomic, copy, nullable) NSDictionary *pageData;

@property (nonatomic, strong, readonly) KuiklyRenderViewController *renderViewController;

- (void)sendNativeTabSelectedIndex:(NSInteger)index;

/// 容器 bounds 变化后刷新内嵌 Kuikly（含 rootViewSizeDidChanged）。
- (void)syncHostedKuiklyLayoutIfNeeded;

@end

NS_ASSUME_NONNULL_END
