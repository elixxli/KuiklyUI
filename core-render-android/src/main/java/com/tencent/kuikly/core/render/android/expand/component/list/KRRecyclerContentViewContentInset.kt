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

package com.tencent.kuikly.core.render.android.expand.component.list

import com.tencent.kuikly.core.render.android.const.KRCssConst
import com.tencent.kuikly.core.render.android.css.ktx.toPxF

/**
 * Created by kam on 2023/5/1.
 */
class KRRecyclerContentViewContentInset(
    contentInset: String = KRCssConst.EMPTY_STRING,
    var finishCallback: KRContentInsertFinishCallback? = null
) {
    var top = 0f
    var left = 0f
    var bottom = 0f
    var right = 0f
    var animate: Boolean = false

    init {
        if (contentInset.isNotEmpty()) {
            val spilt = contentInset.split(KRCssConst.BLANK_SEPARATOR)
            top = spilt[0].toFloat().toPxF()
            left = spilt[1].toFloat().toPxF()
            bottom = spilt[2].toFloat().toPxF()
            right = spilt[3].toFloat().toPxF()
            if (spilt.size > 4) {
                animate = spilt[4].toInt() == 1
            }
        }
    }
}

typealias KRContentInsertFinishCallback = () -> Unit