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

package com.tencent.kuikly.demo.pages.app.feed

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.demo.pages.app.common.AppNineGrid

internal class AppRetWeetView: ComposeView<AppRetWeetViewAttr, AppRetWeetViewEvent>() {
    
    override fun createEvent(): AppRetWeetViewEvent {
        return AppRetWeetViewEvent()
    }

    override fun createAttr(): AppRetWeetViewAttr {
        return AppRetWeetViewAttr()
    }

    private fun getDisplayContent(): String {
        var displayContent = ""
        if (!attr.zfNick.isNullOrEmpty()
            && !attr.zfUserId.isNullOrEmpty()
            && !attr.zfContent.isNullOrEmpty()) {
            displayContent = "[@${attr.zfNick}:${attr.zfUserId}]:${attr.zfContent}"
        }
        return displayContent
    }

    override fun body(): ViewBuilder {
        val ctx = this
        val displayContent = getDisplayContent()
        return {
            if (ctx.attr.containZf) {
                attr {
                    padding(12.0f)
                    marginTop(5.0f)
                    backgroundColor(Color(0xffF7F7F7))
                }
                if (displayContent.isNotEmpty()) {
                    AppFeedContent {
                        attr {
                            content = displayContent
                        }
                    }
                    ctx.attr.zfVideoUrl?.let {
                        if (it.isNotEmpty()) {
                            AppFeedVedio {
                                attr {
                                    vedioUrl = it
                                }
                            }
                        }
                    }
                    ctx.attr.zfPicUrl?.let {
                        if (it.isNotEmpty()) {
                            AppNineGrid {
                                attr {
                                    picUrls = it
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

internal class AppRetWeetViewAttr : ComposeAttr() {
    var containZf: Boolean = false
    var zfNick: String? = null
    var zfUserId: String? = null
    var zfContent: String? = null
    var zfVideoUrl: String? = null
    var zfPicUrl: List<String>? = null
}

internal class AppRetWeetViewEvent : ComposeEvent()

internal fun ViewContainer<*, *>.AppRetWeet(init: AppRetWeetView.() -> Unit) {
    addChild(AppRetWeetView(), init)
}