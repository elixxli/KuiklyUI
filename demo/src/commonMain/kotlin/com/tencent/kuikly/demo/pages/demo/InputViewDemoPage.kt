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
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.kuikly.core.views.*
import com.tencent.kuikly.demo.pages.base.BasePager
import com.tencent.kuikly.demo.pages.base.Utils
import com.tencent.kuikly.demo.pages.demo.base.NavBar
import com.tencent.kuikly.core.reactive.handler.*
@Page("InputViewDemoPage")
internal class InputViewDemoPage : BasePager() {
    lateinit var inputRef : ViewRef<InputView>
    var keyboardHeight : Float by observable(0f)
    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                backgroundColor(Color(0xFF3c6cbdL))

            }
            // 背景图
            Image {
                attr {
                    absolutePosition(0f, 0f, 0f, 0f)
                    src("https://sqimg.qq.com/qq_product_operations/kan/images/viola/viola_bg.jpg")
                }
            }
            // navBar
            NavBar {
                attr {
                    title = "Input组件Demo"
                }
            }

            View {
                attr {
                    flex(1f)
                }

//                View {
//                    attr {
//                        height(200f)
//                        backgroundColor(Color.YELLOW)
//                    }
//                }
//                View {
//                    attr {
//                        height(200f)
//                        backgroundColor(Color.RED)
//                    }
//                }

                List {
                    attr {
                        height(200f)
                        backgroundColor(Color.BLUE)
                        flex(1f)
                    }
                    View {
                        attr {
                            height(200f)
                            backgroundColor(Color.BLACK)
                        }
                    }

                    View {
                        attr {
                            height(200f)
                            backgroundColor(Color.GREEN)
                        }
                    }

                    Input {

                        ref {
                            ctx.inputRef = it
                        }

                        attr {
                            margin(20f)
                            maxTextLength(20)
                            height(200f)
                            fontSize(30f)
                            fontWeightBold()

                          //  keyboardTypeNumber()
                           // textAlignCenter()
                            returnKeyTypeNext()
                            placeholder("我是placehooder")
                            placeholderColor(Color.YELLOW)

                            color(Color.BLACK)
                            autofocus(true)
                            backgroundColor(Color.RED)

                            transform(Translate(0f, -ctx.keyboardHeight / 200f))
                            animation(Animation.easeIn(0.3f), ctx.keyboardHeight)
                        }

                        event {
                            textDidChange {
                                Utils.logToNative(pagerId, "test_textDidChange" + it.toString())
                            }

                            inputBlur {
                                Utils.logToNative(pagerId, "test_inputBlur" + it.toString())
                            }

                            inputFocus {
                                Utils.logToNative(pagerId, "test_inputFocus" + it.toString())
                            }

                            keyboardHeightChange {
                                val height = it.height
                                Utils.logToNative(pagerId, "test_keyboardHeightChange" + it.toString())
                                ctx.keyboardHeight = height
                            }
                        }
                    }

                    View {
                        attr {
                            height(200f)
                            backgroundColor(Color.BLACK)

                            allCenter()
                        }
                    }

                    View {
                        attr {
                            height(200f)
                            backgroundColor(Color.GREEN)
                        }
                    }

                }


            }
        }
    }

    override fun createEvent(): ComposeEvent {
        return ComposeEvent()
    }

    override fun viewDidLoad() {
        super.viewDidLoad()
        setTimeout(pagerId, 5000) {

            val inputView = inputRef.view!!
            inputView.setText("")
            inputView.blur()


        }
    }
}