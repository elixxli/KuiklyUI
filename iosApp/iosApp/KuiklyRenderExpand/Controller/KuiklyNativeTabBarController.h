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

NS_ASSUME_NONNULL_BEGIN

FOUNDATION_EXPORT NSString *const KuiklyNativeTabBarPagerEventTabSelected;
FOUNDATION_EXPORT NSString *const KuiklyNativeTabBarPagerEventTabBarSafeArea;

@interface KuiklyNativeTabBarController : UIViewController

/// Kuikly PageList 横滑切页时同步系统 TabBar 选中态。
- (void)syncTabBarSelectedIndexFromKuikly:(NSInteger)index;

@end

NS_ASSUME_NONNULL_END
