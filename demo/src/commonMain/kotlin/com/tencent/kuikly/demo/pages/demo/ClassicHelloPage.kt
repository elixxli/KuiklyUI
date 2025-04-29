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

//package com.tencent.kuikly.demo.pages.demo
//
//
//import com.tencent.kuikly.core.annotations.Page
//import com.tencent.kuikly.core.base.Color
//import com.tencent.kuikly.core.base.ComposeEvent
//import com.tencent.kuikly.core.base.ViewBuilder
//import com.tencent.kuikly.demo.pages.base.BasePager
//import com.tencent.kuikly.core.reactive.handler.*
//import com.tencent.kuikly.demo.pages.base.Utils
//import com.tencent.kuikly.demo.pages.base.ktx.setTimeout
//
//@Page("classic_demo")
//internal class ClassicHelloPage : BasePager() {
//
//    var clickCount: Int by observable(0)
//
//
//
//    override fun body(): ViewBuilder {
//        val ctx = this
//        return {
//            attr {
//            }
//
////            ComposeJB({
////                backgroundColor(Color.RED)
////                flex(1f)
////            }) {
////                JBView(modifier = Modifier.backgroundColor(Color.YELLOW)) {
////
////                    if (ctx.clickCount % 2 == 0) {
////                        if (ctx.clickCount % 2 == 1) {
////                            JBText("我是文本别看我",
////                                fontSize = 25f,
////                                color = Color.BLACK,
////                                modifier = Modifier.margin(top = 50f).backgroundColor(Color.GREEN)
////                            )
////                            JBImage("", modifier = Modifier
////                                .backgroundColor(Color.BLUE)
////                                .size(100f, 100f)
////                            )
////                        } else {
////                            JBImage("", modifier = Modifier
////                                .backgroundColor(Color.BLACK)
////                                .size(200f, 2f)
////                                .borderRadius(10f)
////                            )
////                        }
////
////                        if (ctx.clickCount != 3) {
////                            JBText("我是文本别看我2",
////                                fontSize = 25f,
////                                color = Color.BLACK,
////                                modifier = Modifier.margin(top = 50f)
////                            )
////                        } else {
////                            JBText("我是文本别看我34343",
////                                fontSize = 25f,
////                                color = Color.BLUE,
////
////                                modifier = Modifier.margin(top = 50f)
////                            )
////                        }
////
////                        JBText("我是文本老师43",
////                            fontSize = 25f,
////                            color = Color.BLUE,
////
////                            modifier = Modifier.margin(top = 50f)
////                        )
////
////                        if (ctx.clickCount % 2 == 0) {
////                            JBImage("", modifier = Modifier
////                                .backgroundColor(Color.GREEN)
////                                .size(200f, 200f)
////                                .borderRadius(5f)
////                            )
////                        } else {
////                            JBText("我是都是辅导老师减肥36",
////                                fontSize = 25f,
////                                color = Color.BLUE,
////
////                                modifier = Modifier.margin(top = 0f, left = 50f)
////                            )
////                        }
////                    }
////
////                    JBText("我是都是辅导老师减肥35",
////                        fontSize = 25f,
////                        color = Color.BLUE,
////
////                        modifier = Modifier.margin(top = 0f, left = 50f)
////                    )
////
////
//////
//////                    JBText("我是文本别看我3",
//////                        fontSize = 25f,
//////                        color = Color.BLACK,
//////                        modifier = Modifier.margin(top = 50f)
//////                    )
//////
//////                    JBText("我是文本别看我4",
//////                        fontSize = 25f,
//////                        color = Color.BLACK,
//////                        modifier = Modifier.margin(top = 50f)
//////                    )
//////
//////                    JBText("我是文本别看我5",
//////                        fontSize = 25f,
//////                        color = Color.BLACK,
//////                        modifier = Modifier.margin(top = 50f)
//////                    )
////
////                    //com.tencent.kuikly.compose.views.Text()
////                }
//
//            }
//
//        }
//    }
//
//    override fun createEvent(): ComposeEvent {
//        return ComposeEvent()
//    }
//
//    override fun created() {
//
////        setTimeout(2000) {
////            clickCount++
////        }
////        setTimeout(4000) {
////            clickCount++
////        }
////        setTimeout(6000) {
////            clickCount++
////        }
////        super.created()
////        //  esc:<diffResult: inserts: [4, 1] deletes: [2] updates:[]>
////        //     Desc:<diffResult: inserts: [4] deletes: [] updates:[oldIndex:3 newIndex:3]>
////        //  Desc:<diffResult: inserts: [0, 1] deletes: [] updates:[oldIndex:3 newIndex:5, oldIndex:2 newIndex:4]>
////        val oldList = arrayOf<Node>()
////        val newList = arrayOf<Node>(Node("A"))
////        //  Desc:<diffResult: inserts: [2] deletes: [] updates:[1]>
////        val diffResult = DiffUtil.diffWithMinimumDistance(newList, oldList)
////
////        // D C A B C D
////        // Desc:<diffResult: inserts: [0, 1] deletes: [] updates:[4, 5]>
////
////        Utils.logToNative(pagerId, "diffResult: " + diffResult.toString())
//
//    }
//
//
//}
//
//internal class Node(val type: String) : Diffable {
//    override fun isEqualToObject(otherObject: Diffable): Boolean {
//        if (otherObject is Node) {
//            return type == otherObject.type
//        }
//        return false
//    }
//}
