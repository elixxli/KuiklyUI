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

import com.tencent.kuikly.core.base.toInt
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.manager.PagerManager
import com.tencent.kuikly.core.module.CallbackFn
import com.tencent.kuikly.core.module.Module
import com.tencent.kuikly.core.nvi.serialization.json.JSONArray
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


internal class BridgeModule : Module() {

    override fun moduleName(): String {
        return MODULE_NAME
    }

    fun closePage(
        data: JSONObject? = null,
        callbackFn: CallbackFn? = null, // 关闭页面完成回调，只在iOS平台可用
    ) {
        callNativeMethod(CLOSE_PAGE, data, callbackFn)
    }

    fun log(content: String) {
        val methodArgs = JSONObject()
        methodArgs.put("content", content)
        callNativeMethod(LOG, methodArgs, null)
    }
    fun logAndTelemetry(spanContext: String, content: String) {
        val methodArgs = JSONObject()
        methodArgs.put("content", content)
        methodArgs.put("spanContext", spanContext)
        callNativeMethod(LOG_AND_TELEMETRY, methodArgs, null)
    }
    fun copyToPasteboard(content: String) {
        val methodArgs = JSONObject()
        methodArgs.put("content", content)
        callNativeMethod("copyToPasteboard", methodArgs, null)
    }

    fun showAlert(
        title: String?,
        message: String?,
        leftBtnTitle: String?,
        rightBtnTitle: String?,
        responseCallbackFn: CallbackFn
    ) {
        val methodArgs = JSONObject()
        val buttonArray = JSONArray()
        leftBtnTitle?.also {
            buttonArray.put(it)
        }
        rightBtnTitle?.also {
            buttonArray.put(it)
        }

        methodArgs.put("buttons", buttonArray)
        title?.also {
            methodArgs.put("title", it)
        }
        message?.also {
            methodArgs.put("message", it)
        }
        callNativeMethod("showAlert", methodArgs) {
            responseCallbackFn(it)
        }
    }

    // 拨打电话
    fun callPhone(phoneNumber: String) {
        val methodArgs = JSONObject()
        methodArgs.put("phoneNumber", phoneNumber)
        callNativeMethod("callPhone", methodArgs, null)
    }

    fun toast(content: String) {
        val methodArgs = JSONObject()
        methodArgs.put("content", content)
        callNativeMethod("toast", methodArgs, null)
    }

    fun testArray() {
        //call
     //   listOf()
        val array = arrayOf<Any>("222", createByteArray())
        val res = syncToNativeMethod("testArray",array) {
            if (it is Array<*>) {
                KLog.i("testArray", "callback res:${it[1] is ByteArray } ${(it[1] as ByteArray)[1].toString()}" + it.toString())

            }
            }

        var i = 0

        if (res is Array<*>) {
            i = res.size

            KLog.i("testArray res:", "${res[1] is ByteArray } | ${(res[1] as ByteArray).size }" + res[0].toString())

        }
    }


    fun createByteArray(): ByteArray {
        val size = 10 // 指定 ByteArray 的大小
        val byteArray = ByteArray(size)

        for (i in 0 until size) {
            byteArray[i] = (i * 2).toByte() // 每个元素的值为其索引的两倍
        }

        return byteArray
    }

    fun openPage(url: String, closeCurPage: Boolean = false, closeSamePage: Boolean = false, userData: JSONObject? = null, callbackFn: CallbackFn? = null) {
        val methodArgs = JSONObject()
        methodArgs.put("url", url)
        methodArgs.put("closeCurPage", closeCurPage.toInt())
        methodArgs.put("closeSamePage", closeSamePage.toInt())
        userData?.also {
            methodArgs.put("userData", it)
        }
        callNativeMethod(OPEN_PAGE, methodArgs, callbackFn)
    }

    // 设置状态栏为白色
    fun setWhiteStatusBarStyle() {
        callNativeMethod(SET_STATUS_BAR_WHITE, null, null)
    }
    // 设置状态栏为黑色
    fun setBlackStatusBarStyle() {
        callNativeMethod(SET_STATUS_BAR_BLACK, null, null)
    }

    // 灯塔上报
    fun reportDT(eventCode: String, data: JSONObject) {
        val methodArgs = JSONObject()
        methodArgs.put("eventCode", eventCode)
        methodArgs.put("data", data)
        // methodArgs.put("realtime", 1)
        callNativeMethod(REPORT_DT, methodArgs, null)
    }

    // 实时上报
    fun reportRealTime(eventCode: String, data: JSONObject) {
        val methodArgs = JSONObject()
        methodArgs.put("eventCode", eventCode)
        methodArgs.put("data", data)
        callNativeMethod(REPORT_REALTIME, methodArgs, null)
    }

    // 页面首屏（有内容，来自缓存）耗时上报
    fun reportPageCostTimeForCache() {
        callNativeMethod(REPORT_PAGE_COST_TIME_FOR_CACHE, null, null)
    }

    // 页面首屏（有内容，来自后台）耗时上报
    fun reportPageCostTimeForSuccess() {
        callNativeMethod(REPORT_PAGE_COST_TIME_FOR_SUCCESS, null, null)
    }

    // 页面首屏耗时上报 - 加载失败
    fun reportPageCostTimeForError() {
        callNativeMethod(REPORT_PAGE_COST_TIME_FOR_ERROR, null, null)
    }

    // 异步获取本地服务器时间戳
    fun localServeTime(cb: CallbackFn) {
        callNativeMethod(LOCAL_SERVE_TIME, null, cb)
    }

    // 同步获取msf系统时间戳
    fun serverTimeMillis(): Long {
        return syncCallNativeMethod(SERVER_TIME_MILLIS, null, null).toLong()
    }

    //同步获取本地服务器时间戳
    suspend fun localServeTime(): JSONObject? {
        return suspendCoroutine<JSONObject?> { continuation ->
            localServeTime() {
                continuation.resume(it)
            }
        }
    }

    // 同步获取时间戳（毫秒）
    // 注：一般不用于业务，仅为本地性能耗时测试
    fun currentTimeStamp(): Long {
        val timestamp = syncCallNativeMethod(CURRENT_TIMESTAMP, null, null)
        if (timestamp.isNotEmpty()) {
            return timestamp.toLong()
        } else {
            return 0
        }
    }

    // 同步获取日期格式化
    fun dateFormatter(timeStamp: Long, format: String): String {
        val params = JSONObject()
        params.put("timeStamp", timeStamp)
        params.put("format", format)
        return syncCallNativeMethod(DATE_FORMATTER, params, null)
    }

    /**
     * 根据 [key] 获取本地缓存的数据, 异步返回
     */
    fun fetchCachedFromNative(key: String, callbackFn: CallbackFn) {
        val param = JSONObject().apply {
            put("key", key)
        }
        callNativeMethod("fetchCachedFromNative", param) {
            callbackFn(it)
        }
    }

    /**
     * 根据 [key] 获取本地缓存的数据, 同步返回
     */
    fun getCachedFromNative(key: String): String {
        val param = JSONObject().apply {
            put("key", key)
        }
        return syncCallNativeMethod("getCachedFromNative", param, null)
    }

    /**
     * 向 native 写入 [key] 对应的缓存
     */
    fun setCachedToNative(key: String, value: String, callbackFn: CallbackFn? = null) {
        val param = JSONObject().apply {
            put("key", key)
            put("value", value)
        }
        callNativeMethod("setCachedToNative", param) {
            callbackFn?.invoke(it)
        }
    }

    fun getCurrentAccount(): String {
        return syncCallNativeMethod(GET_CURRENT_ACCOUNT, JSONObject(), null)
    }

    fun loadRemoteConfig(params: RemoteConfig): String {
        return syncCallNativeMethod(REMOTE_CONFIG, params.encode(), null)
    }

    fun showSignJumpAlert(params: JSONObject): String {
        return syncCallNativeMethod(SIGN_ALERT, params, null)
    }

    fun closeKeyboard(data: JSONObject? = null, callbackFn: CallbackFn? = null): String {
        return syncCallNativeMethod(CLOSE_KEYBOARD, data, callbackFn)
    }

    fun humanVerification(params: JSONObject, callbackFn: CallbackFn? = null): String {
        return syncCallNativeMethod(HUMAN_VERIFICATION, params, callbackFn)
    }

    fun urlEncode(string: String): String {
        val params = JSONObject()
        params.put("string", string)
        return syncCallNativeMethod(URL_ENCODE, params, null)
    }

    fun urlDecode(string: String): String {
        val params = JSONObject()
        params.put("string", string)
        return syncCallNativeMethod(URL_DECODE, params, null)
    }

    /**
     * 端确保缓存和线程同步
     * */
    fun preloadPB(pbClassName: String, pbData: String, token: String) {
        val data = JSONObject()
        data.put("pbClassName", pbClassName)
        data.put("pbData", pbData)
        data.put(KEY_FEED_PB_TOKEN, token)
        callNativeMethod(PRELOAD_PB, data, null)
    }

    /**
     * 页面退出时删除pb缓存
     * */
    fun cleanPB(tokenArray: ArrayList<String>) {
        if (tokenArray.size <= 0) {
            return
        }
        val data = JSONObject()
        val array = JSONArray()
        for (token in tokenArray) {
            array.put(token)
        }
        data.put("tokenArray", array)
        callNativeMethod(CLEAN_PB, data, null)
    }

    /**
     * 预加载PAG的so
     * */
    fun downloadPagSo() {
        val data = JSONObject()
        callNativeMethod(DOWNLOAD_PAG_SO, data, null)
    }

    /**
     * 获取指定 url 的本地缓存地址
     */
    fun getLocalImagePath(url: String, callback: CallbackFn?) {
        val params = JSONObject()
        params.put("imageUrl", url)
        callNativeMethod(GET_LOCAL_IMAGE_PATH, params, callback)
    }

    /**
     * 读取文件内容
     */
    fun readAssetFile(assetPath: String, callback: CallbackFn?) {
        val params = JSONObject()
        params.put("assetPath", assetPath)
        syncCallNativeMethod(READ_ASSET_FILE, params, callback)
    }

    private fun callNativeMethod(methodName: String, data: JSONObject?, callbackFn: CallbackFn?) {
        toNative(
            false,
            methodName,
            data?.toString(),
            callbackFn,
            false
        )
    }

    // --------- 同步调用Native方法 -------
    private fun syncCallNativeMethod(
        methodName: String,
        data: JSONObject?,
        callbackFn: CallbackFn?
    ): String {
        return toNative(
            false,
            methodName,
            data?.toString(),
            callbackFn,
            true
        ).toString()
    }


    companion object {
        const val MODULE_NAME = "HRBridgeModule"
        const val OPEN_PAGE = "openPage"
        const val CLOSE_PAGE = "closePage"
        const val LOG = "log"
        const val LOG_AND_TELEMETRY = "logAndTelemetry"
        const val REPORT_DT = "reportDT"
        const val LOCAL_SERVE_TIME = "localServeTime"
        const val SERVER_TIME_MILLIS = "serverTimeMillis"
        const val CURRENT_TIMESTAMP = "currentTimestamp"
        const val DATE_FORMATTER = "dateFormatter"
        const val REPORT_REALTIME = "reportRealTime"
        const val REPORT_PAGE_COST_TIME_FOR_CACHE = "reportPageCostTimeForCache"
        const val REPORT_PAGE_COST_TIME_FOR_SUCCESS = "reportPageCostTimeForSuccess"
        const val REPORT_PAGE_COST_TIME_FOR_ERROR = "reportPageCostTimeForError"
        const val REMOTE_CONFIG = "loadRemoteConfig"
        const val SIGN_ALERT = "signAlert"
        const val CLOSE_KEYBOARD = "closeKeyboard"
        const val URL_ENCODE = "urlEncode"
        const val URL_DECODE = "urlDecode"
        const val HUMAN_VERIFICATION = "humanVerification"
        const val PRELOAD_PB = "preloadPB"
        const val CLEAN_PB = "cleanPB"
        const val KEY_FEED_PB_TOKEN = "feedPbToken"
        const val SET_STATUS_BAR_WHITE = "setWhiteStatusBarStyle"
        const val SET_STATUS_BAR_BLACK = "setBlackStatusBarStyle"
        const val GET_CURRENT_ACCOUNT = "getAccount"
        const val DOWNLOAD_PAG_SO = "downloadPagSo"
        const val GET_LOCAL_IMAGE_PATH = "getLocalImagePath"
        const val READ_ASSET_FILE = "readAssetFile"
    }


}

data class RemoteConfig(
    val configID: Int = 0 ,
    val configKey: String = "",
    val defaultValue: Any
) {
    fun encode(): JSONObject {
        return JSONObject().apply {
            put("configID", configID)
            put("configKey", configKey)
            put("defaultValue", defaultValue)
        }
    }
}

enum class DCReportActionType(val value: Int) {
    Content(1),
    Video(2),
}

enum class DCReportSubActionType(val value: Int) {
    Expose(1),
    Play(4),
}