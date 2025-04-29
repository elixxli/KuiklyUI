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

package com.tencent.kuikly.demo.pages.demo

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.module.NotifyModule
import com.tencent.kuikly.core.module.SharedPreferencesModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.demo.pages.base.BasePager
import com.tencent.kuikly.demo.pages.base.Utils
import com.tencent.kuikly.demo.pages.demo.base.NavBar
import com.tencent.kuikly.core.reactive.handler.*
@Page("ViewDemoPage")
internal class ViewDemoPage: BasePager() {

    var borderHeight: Float by observable(2f)
    var animated: Boolean by observable(false)
    var flag0: Boolean by observable(true)
    var flag1: Boolean by observable(false)
    var flag: Int = 0

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
              //  backgroundColor(Color(0xFF3c6cbdL))

                backgroundColor(Color.WHITE)
            }
            // 背景图
//            Image {
//                attr {
//                    absolutePosition(0f, 0f, 0f, 0f)
//                    src("https://sqimg.qq.com/qq_product_operations/kan/images/viola/viola_bg.jpg")
//                }
//            }
            // navBar
            NavBar {
                attr {
                    title = "ViewDemo"
                }
            }

            View {
                attr {
                    height(500f)
                    backgroundColor(Color.RED)
                    ctx.flag++
                    if (ctx.flag0) {
                        backgroundColor(Color.YELLOW)
                        if (ctx.flag1) {
                            backgroundColor(Color.BLUE)
                        }
                    }
                }

                View {
                    attr {
                        positionAbsolute()
                        top(Percentage(50f))
                        left(Percentage(50f))
                        transform(Translate(-0.5f, -0.5f))
                        width(200f)
                        height(200f)
                        backgroundColor(Color.BLUE)
                    }

                }
            }



//            View {
//                attr {
//                    margin(50f)
//                    boxShadow(BoxShadow(0f, 0f, 5f,
//                        Color(0,0,0,0.6f)
//                    ))
//                }
//                View {
//                    attr {
//
//                        marginTop(700f)
//                        borderRadius(5f)
//                        size(100f, 300f)
//                        backgroundColor(Color.WHITE)
//                    }
//
//                    event {
//                        didAppear {
//                            Utils.logToNative(pagerId, "lifeCycle： didAppear")
//                        }
//                        didDisappear {
//                            Utils.logToNative(pagerId, "lifeCycle： didDisappear")
//                        }
//                        willAppear {
//                            Utils.logToNative(pagerId, "lifeCycle： willAppear")
//                        }
//                        willDisappear {
//                            Utils.logToNative(pagerId, "lifeCycle： willDisappear")
//                        }
//                    }
//                }
//            }


//            RichText {
//                attr {
//                    margin(20f)
//
//                    if (animated) {
//                        Utils.bridgeModule(pagerId).toast("222")
//                        transform(Scale(3.6f, 3.6f))
//                    } else {
//                        transform(Scale(1f, 1f))
//                    }
//                    animation(Animation.linear(1f), animated)
//                }
//
//                Span {
//                    attr {
//                        fontSize(20f)
//                        color(Color.RED)
//                        text("我是第一段")
//                    }
//                }
//                Span {
//                    attr {
//                        fontSize(10f)
//                        color(Color.BLUE)
//                        text("我是第二段")
//                    }
//                }
//            }
        }
    }

    override fun createEvent(): ComposeEvent {
        return ComposeEvent()
    }

    override fun created() {
        super.created()


        setTimeout(pagerId, 1000) {
            if (flag != 1) {
                throw RuntimeException("error")
            }
            flag1 = true
            if (flag != 2) {
                throw RuntimeException("error")
            }
            setTimeout(pagerId, 1000) {
                flag0 = false
                if (flag != 3) {
                    throw RuntimeException("error")
                }
                setTimeout(pagerId, 1000) {
                    flag1 = false
                    if (flag != 3) {
                        throw RuntimeException("error")
                    }

                }
            }



        }

        val timeStamp = Utils.bridgeModule(pagerId).currentTimeStamp()
        val formatString = Utils.bridgeModule(pagerId).dateFormatter(timeStamp, "MM月dd日HH:mm")
        Utils.logToNative(pagerId, "date format:" + formatString)


        // notify module
        val eventName = "TestEventName"
        val eventRef = getPager().acquireModule<NotifyModule>(NotifyModule.MODULE_NAME).addNotify(eventName) {
            Utils.bridgeModule(pagerId).toast("收到事件：" + it.toString())
        }

        setTimeout(pagerId, 2000) {
            val data = JSONObject()
            data.put("key", "value")
            getPager().acquireModule<NotifyModule>(NotifyModule.MODULE_NAME).postNotify(eventName, data) // 能收到
            setTimeout(1 * 1000) {
                getPager().acquireModule<NotifyModule>(NotifyModule.MODULE_NAME).removeNotify(eventName,eventRef)
                getPager().acquireModule<NotifyModule>(NotifyModule.MODULE_NAME).postNotify(eventName, data) // 不能收到
            }
        }

        // cache module
        val cacheKey = "testKey"
        getPager().acquireModule<SharedPreferencesModule>(SharedPreferencesModule.MODULE_NAME).setItem(cacheKey, "我是存的值")

        val cacheValue = getPager().acquireModule<SharedPreferencesModule>(SharedPreferencesModule.MODULE_NAME).getItem(cacheKey)

        Utils.logToNative(pagerId, "cacheValue:" + cacheValue)



    }

    override fun viewDidLayout() {
        super.viewDidLayout()
        animated = true
        setTimeout(pagerId, 1000) {
            borderHeight = 10f
        }
    }


}