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

package com.tencent.kuikly.core.module

import com.tencent.kuikly.core.base.toBoolean
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject


typealias NMResponse = (data: JSONObject, success : Boolean , errorMsg: String) -> Unit

typealias NMAllResponse = (data: JSONObject, success : Boolean , errorMsg: String, response: NetworkResponse) -> Unit

/**
 * 表示网络响应的数据类。
 *
 * @property headerFields 包含响应头字段的JSONObject。
 * @property statusCode 可选的Int，表示响应的状态码。如果此值为null，
 *                      则表示端版本较低，不支持传递状态码。
 */
data class NetworkResponse(val headerFields: JSONObject, val statusCode: Int? = null) { }

/*
 * @brief Http网络请求模块
 */
class NetworkModule : Module() {

    override fun moduleName(): String {
        return MODULE_NAME
    }

    /*
     * @brief get请求
     */
    fun requestGet(url : String, param: JSONObject, responseCallback: NMResponse) {
        requestGet(url, param) { data, success, errorMsg, response ->
            responseCallback(data, success, errorMsg)
        }
    }
    /*
     * @brief get请求(新)
     * 注：responseCallback中带有response.headers回包数据
    */
    fun requestGet(url : String, param: JSONObject, responseCallback: NMAllResponse) {
        httpRequest(url, false, param, null, null, 30, responseCallback)
    }
    /*
     * @brief post请求
     */
    fun requestPost(url : String, param: JSONObject, responseCallback: NMResponse) {
        requestPost(url, param) { data, success, errorMsg, response ->
            responseCallback(data, success, errorMsg)
        }
    }
    /*
    * @brief post请求(新)
    * 注：responseCallback中带有response.headers回包数据
    */
    fun requestPost(url : String, param: JSONObject, responseCallback: NMAllResponse) {
        httpRequest(url, true, param, null, null, 30, responseCallback)
    }
    /**
     * @brief 通用http请求
     * 注：1. headers中可添加"Content-Type": "application/json"
     *    2. 如果接口回包数据类型为非json格式，回包数据字符串会以{data:xxxx}被包装一层，其中xxxx为接口实际回包内容
     */
    fun httpRequest(url : String , isPost: Boolean, param: JSONObject, headers: JSONObject? = null, cookie : String? = null, timeout : Int = 30, responseCallback: NMResponse ) {
        httpRequest(url, isPost, param, headers, cookie, timeout) { data, success, errorMsg, response ->
            responseCallback(data, success, errorMsg)
        }
    }

    /**
     * @brief 通用http请求(新)
     * 注：1. headers中可添加"Content-Type": "application/json"
     *    2. 如果接口回包数据类型为非json格式，回包数据字符串会以{data:xxxx}被包装一层，其中xxxx为接口实际回包内容
     *    3. responseCallback中带有response.headers回包数据
     */
    fun httpRequest(url : String , isPost: Boolean, param: JSONObject, headers: JSONObject? = null, cookie : String? = null, timeout : Int = 30, responseCallback: NMAllResponse ) {
        val params = JSONObject().apply {
            put("url", url)
            put("method", if (isPost) "POST" else "GET")
            put("param", param)
            headers?.also {
                put("headers", it)
            }
            cookie?.also {
                put("cookie", it)
            }
            put("timeout", timeout)
        }
        toNative(
            false,
            METHOD_HTTP_REQUEST,
            params.toString(),
            callback = { res ->
                res?.also {
                    val dataString = it.optString("data","")
                    val dataJSON = try {
                        JSONObject(dataString)
                    } catch (e : Throwable) {
                        JSONObject().apply {
                            put("data", dataString)
                        }
                    }
                    var headers: JSONObject? = null
                    headers = try {
                        JSONObject(it.optString("headers","{}"))
                    } catch (e : Throwable) {
                        null
                    }
                    val success = it.optInt("success").toBoolean()
                    val errorMsg = it.optString("errorMsg")
                    var statusCode: Int? = null
                    if (it.has("statusCode")) {
                        statusCode = it.optInt("statusCode")
                    }
                    val response = NetworkResponse(headers ?: JSONObject(), statusCode)
                    responseCallback(dataJSON, success, errorMsg, response)
                }
            }
        )
    }

    companion object {
        const val MODULE_NAME = ModuleConst.NETWORK
        const val METHOD_HTTP_REQUEST = "httpRequest"
    }

}
