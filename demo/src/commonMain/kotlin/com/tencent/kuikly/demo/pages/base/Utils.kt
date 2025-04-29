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

package com.tencent.kuikly.demo.pages.base

import com.tencent.kuikly.core.base.BaseObject
import com.tencent.kuikly.core.manager.BridgeManager
import com.tencent.kuikly.core.manager.PagerManager
import com.tencent.kuikly.core.module.NotifyModule

internal object Utils : BaseObject() {

     fun bridgeModule(pager: String) : BridgeModule {
         return PagerManager.getPager(pager).acquireModule<BridgeModule>(BridgeModule.MODULE_NAME)
     }

    fun notifyModule(pagerId: String = ""): NotifyModule {
        val pgId = pagerId.ifEmpty {
            BridgeManager.currentPageId
        }
        return PagerManager.getPager(pgId).acquireModule(NotifyModule.MODULE_NAME)
    }

     fun logToNative(pagerId: String, content: String) {
         // logToNaive
         bridgeModule(pagerId).log(content)
     }

    fun currentBridgeModule() : BridgeModule {
        return PagerManager.getPager(BridgeManager.currentPageId).acquireModule<BridgeModule>(BridgeModule.MODULE_NAME)
    }

    fun logToNative(content: String) {
        bridgeModule(BridgeManager.currentPageId).log(content)
    }

    fun logAndTelemetry(content: String, spanContext: String = "") {
        bridgeModule(BridgeManager.currentPageId).logAndTelemetry(spanContext, content)
    }

    fun convertToPriceStr(price: Long): String {
        return (price / 100f).toString()
    }

    fun formatCountNumber(count: Int): String {
        if (count == 0){
            return ""
        }
        return when {
            count < 10000 -> {
                count.toString()
            }
            count in 10000..999999 -> {
                val tenThousand = count / 10000
                val thousand = count / 1000 % 10
                if (thousand == 0) {
                    "${tenThousand}万"
                }
                "$tenThousand.${thousand}万"
            }
            count in 1000000..99999999 -> {
                "${count/10000}万"
            }
            else -> {
                "$${count/100000000}亿"
            }
        }
    }
}

