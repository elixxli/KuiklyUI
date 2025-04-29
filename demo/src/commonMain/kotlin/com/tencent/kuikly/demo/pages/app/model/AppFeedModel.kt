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

package com.tencent.kuikly.demo.pages.app.model

import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.demo.pages.base.ktx.map

data class AppFeedModel(
    val appId: String,
    val categoryId: String,
    val content: String,
    var userInfo: AppUserInfo,
    val picurl: List<String>,
    val zfContent: String?,
    val zfNick: String?,
    val zfUserId: String?,
    val zfPicurl: List<String>?,
    val zfAppId: String?,
    val zfVedioUrl: String?,
    val containZf: Boolean,
    val vediourl: String,
    val tail: String,
    val createtime: Int,
    val zanStatus: Int,
    val zhuanfaNum: Int,
    val likeNum: Int,
    val commentNum: Int
) {
    companion object {

        private const val KEY_FEED = "feed"
        private const val KEY_USER = "user"
        private const val KEY_FEED_APP_ID = "appId"
        private const val KEY_FEED_CATEGORY_ID = "categoryId"
        private const val KEY_FEED_CONTENT = "content"
        private const val KEY_FEED_PIC_URL = "picurl"

        private const val KEY_FEED_ZF_CONTENT = "zfContent"
        private const val KEY_FEED_ZF_NICK = "zfNick"
        private const val KEY_FEED_ZF_USER_ID = "zfUserId"
        private const val KEY_FEED_ZF_APP_ID = "zfAppId"
        private const val KEY_FEED_CONTAIN_ZF = "containZf"
        private const val KEY_FEED_ZF_PIC_URL = "zfPicurl"
        private const val KEY_FEED_ZF_VIDEO_URL = "zfVedioUrl"

        private const val KEY_FEED_VIDEO_URL = "vediourl"
        private const val KEY_FEED_CREATE_TAIL = "tail"
        private const val KEY_FEED_CREATE_TIME = "createtime"
        private const val KEY_FEED_LIKE_STATUS = "zanStatus"
        private const val KEY_FEED_ZHUANFA_NUM = "zhuanfaNum"
        private const val KEY_FEED_LIKE_NUM = "likeNum"
        private const val KEY_FEED_COMMENT_NUM = "commentNum"

        private fun parsePictureUrl(urlStr: String) : String {
            var urlStr = urlStr.removePrefix("[")
            urlStr =  urlStr.removeSuffix("]")
            urlStr = urlStr.trim()
            urlStr = urlStr.trim('\'')
            return urlStr
        }

        fun fromJson(jsonObject: JSONObject): AppFeedModel? {
            val feedObj = jsonObject.optJSONObject(KEY_FEED)
            val userObj = jsonObject.optJSONObject(KEY_USER)
            if (feedObj == null || userObj == null) {
                return null
            }

            return AppFeedModel(
                appId = feedObj.optString(KEY_FEED_APP_ID, ""),
                categoryId = feedObj.optString(KEY_FEED_CATEGORY_ID, ""),
                content = feedObj.optString(KEY_FEED_CONTENT, ""),
                picurl = feedObj.optJSONArray(KEY_FEED_PIC_URL)?.map<String, String> {
                    return@map parsePictureUrl(it)
                } ?: listOf(),
                userInfo = AppUserInfo.fromJson(userObj),
                zfContent = feedObj.optString(KEY_FEED_ZF_CONTENT),
                zfNick = feedObj.optString(KEY_FEED_ZF_NICK),
                zfUserId = feedObj.optString(KEY_FEED_ZF_USER_ID),
                zfAppId = feedObj.optString(KEY_FEED_ZF_APP_ID),
                containZf = feedObj.optBoolean(KEY_FEED_CONTAIN_ZF),
                zfPicurl = feedObj.optJSONArray(KEY_FEED_ZF_PIC_URL)?.map<String, String> {
                    return@map parsePictureUrl(it)
                },
                zfVedioUrl = "",
                vediourl = feedObj.optString(KEY_FEED_VIDEO_URL),
                tail = feedObj.optString(KEY_FEED_CREATE_TAIL),
                createtime = feedObj.optInt(KEY_FEED_CREATE_TIME),
                zanStatus = 0,
                zhuanfaNum = feedObj.optInt(KEY_FEED_ZHUANFA_NUM),
                likeNum = feedObj.optInt(KEY_FEED_LIKE_NUM),
                commentNum = feedObj.optInt(KEY_FEED_COMMENT_NUM)
            )
        }

    }
}
