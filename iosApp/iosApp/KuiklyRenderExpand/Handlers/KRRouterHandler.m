/*
 * Tencent is pleased to support the open source community by making KuiklyUI
 * available.
 * Copyright (C) 2025 THL A29 Limited, a Tencent company. All rights reserved.
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


#import "KRRouterHandler.h"
#import "KuiklyRenderViewController.h"
#import "NativeMixKuiklyViewDemoViewController.h"


@implementation KRRouterHandler

+ (void)load {
    [KRRouterModule registerRouterHandler:[self new]];
}


- (void)openPageWithName:(NSString *)pageName pageData:(NSDictionary *)pageData controller:(UIViewController *)controller {
    UIViewController *vc = nil;
    if ([pageName isEqualToString:@"NativeMixKuikly"]) {
        vc = [[NativeMixKuiklyViewDemoViewController alloc] init];
    } else {
        vc = [[KuiklyRenderViewController alloc] initWithPageName:pageName pageData:pageData];
    }
    
    [controller.navigationController pushViewController:vc animated:YES];
}


- (void)closePage:(UIViewController *)controller {
    [controller.navigationController popViewControllerAnimated:YES];
}


@end
