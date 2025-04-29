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

data class AppUserInfo(
    val id: String,
    val nick: String,
    val headurl: String,
    val decs: String,
    val ismember: Int,
    val isvertify: Int
) {
    companion object {

        private const val KEY_USER_ID = "id"
        private const val KEY_USER_NICK = "nick"
        private const val KEY_USER_DECS = "decs"
        private const val KEY_USER_HEAD_URL = "headurl"
        private const val KEY_IS_MEMBER = "ismember"
        private const val KEY_IS_VERTIFY = "isvertify"

        fun fromJson(jsonObject : JSONObject): AppUserInfo {
            return AppUserInfo(
                id = jsonObject.optString(KEY_USER_ID, ""),
                nick = jsonObject.optString(KEY_USER_NICK, ""),
                headurl = jsonObject.optString(KEY_USER_HEAD_URL, ""),
                decs = jsonObject.optString(KEY_USER_DECS, ""),
                ismember = jsonObject.optInt(KEY_IS_MEMBER, 0),
                isvertify = jsonObject.optInt(KEY_IS_VERTIFY, 0),
            )
        }
        fun toJson(obj: AppUserInfo) {

        }
    }
}
