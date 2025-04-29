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

package com.tencent.kuikly.core.render.android.expand.component

import android.content.Context
import android.view.Gravity
import android.view.MotionEvent

/**
 * 多行输入框
 */
class KRTextAreaView(context: Context, softInputMode: Int?) : KRTextFieldView(context, softInputMode) {

    init {
        isSingleLine = false
        gravity = Gravity.LEFT or Gravity.TOP
    }

    companion object {
        const val VIEW_NAME = "KRTextAreaView"
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 解决Scroller内TextArea内容无法滚动
        if (event.actionMasked == MotionEvent.ACTION_DOWN &&
            computeVerticalScrollRange() > computeVerticalScrollExtent()) {
            parent?.requestDisallowInterceptTouchEvent(true)
        }
        return super.onTouchEvent(event)
    }
}
