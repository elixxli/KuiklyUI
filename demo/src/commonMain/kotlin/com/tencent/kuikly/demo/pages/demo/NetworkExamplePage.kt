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
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.module.NetworkModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.demo.pages.base.BasePager

@Page("NetworkExamplePage")
internal class NetworkExamplePage: BasePager() {

    override fun created() {
        super.created()
        val url = "https://jsonplaceholder.typicode.com/posts"
        val params = JSONObject().apply { 
            
        }
        acquireModule<NetworkModule>(NetworkModule.MODULE_NAME).requestGet(url, params) { data, success, errorMsg, response ->
            KLog.i("2", "statusCode: ${response.statusCode} headers: ${response.headerFields}")
        }
    }

    override fun body(): ViewBuilder {
        return {

        }
    }
}