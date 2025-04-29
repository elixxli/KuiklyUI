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

package com.tencent.kuikly.core.views

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.base.event.Event
import com.tencent.kuikly.core.base.event.EventName
import com.tencent.kuikly.core.base.event.TouchParams

open class DivView : ViewContainer<DivAttr, DivEvent>() {
    
    override fun createAttr(): DivAttr {
        return DivAttr()
    }

    override fun createEvent(): DivEvent {
        return DivEvent()
    }

    override fun viewName(): String {
        return ViewConst.TYPE_VIEW
    }

    override fun isRenderView(): Boolean {
        return isRenderViewForFlatLayer()
    }

    /**
     * 层级置顶方法
     */
    fun bringToFront() {
        performTaskWhenRenderViewDidLoad {
            renderView?.callMethod("bringToFront")
        }
    }

}


open class DivAttr : ContainerAttr() {

    internal var backgroundImageView: ImageView? = null

    /**
     * 暂停屏幕刷新帧事件（提升性能）
     */
    fun screenFramePause(pause: Boolean) {
        "screenFramePause" with pause.toInt()
    }

    /**
     * 设置容器背景图片（默认resize为cover）
     * @param src 图片源(Image组件src能力一致)
     * @param imageAttr 自定义该图片属性
     */
    fun backgroundImage(src: String, imageAttr:(ImageAttr.() -> Unit)? = null) {
        if (backgroundImageView != null) {
            backgroundImageView?.getViewAttr()?.also {
                it.src(src)
                imageAttr?.invoke(it)
            }
        } else {
            val divView = view()
            if (divView is ViewContainer<*, *>) {
                backgroundImageView = ImageView()
                backgroundImageView?.also {
                    divView.addChild(it) {
                        getViewAttr().apply {
                            absolutePositionAllZero()
                            src(src)
                            zIndex(-999) // 层级最低
                            imageAttr?.invoke(this)
                        }
                    }
                    divView.insertDomSubView(it, divView.domChildren().indexOf(it))
                }
            }
        }
    }

}

typealias TouchEventHandlerFn = (TouchParams) -> Unit
open class DivEvent : Event() {

    /**
     * 触摸抬起事件兼容模式开关，为true时touchUp和touchCancel都由触摸抬起事件处理器响应。
     */
    private var touchUpCompatibilityMode = true
    private var touchUpHandler: TouchEventHandlerFn? = null
    private var touchCancelHandler: TouchEventHandlerFn? = null
    private var touchUpRegistered = false

    override fun unRegister(eventName: String) {
        when (eventName) {
            EventName.TOUCH_UP.value -> {
                touchUpHandler = null
                updateTouchUpRegister()
            }
            EventName.TOUCH_CANCEL.value -> {
                touchCancelHandler = null
                updateTouchUpRegister()
            }
            else -> {
                super.unRegister(eventName)
            }
        }
    }

    /**
     * 设置触摸按下事件处理器。
     * @param handler 一个 TouchEventHandlerFn，当触摸按下事件发生时调用。
     */
    fun touchDown(handler: TouchEventHandlerFn) {
        register(EventName.TOUCH_DOWN.value) {
            handler(TouchParams.decode(it))
        }
    }

    private fun updateTouchUpRegister() {
        if (touchUpHandler != null || touchCancelHandler != null) {
            if (touchUpRegistered) {
                return
            }
            touchUpRegistered = true
            register(EventName.TOUCH_UP.value) {
                if (touchUpCompatibilityMode) {
                    touchUpHandler?.invoke(TouchParams.decode(it))
                } else {
                    val param = TouchParams.decode(it)
                    if (param.action == EventName.TOUCH_CANCEL.value) {
                        touchCancelHandler?.invoke(param)
                    } else {
                        touchUpHandler?.invoke(param)
                    }
                }
            }
        } else {
            if (!touchUpRegistered) {
                return
            }
            touchUpRegistered = false
            super.unRegister(EventName.TOUCH_UP.value)
        }
    }

    /**
     * 设置触摸抬起事件处理器。
     * @param handler 一个 TouchEventHandlerFn，当触摸抬起事件发生时调用。
     */
    fun touchUp(handler: TouchEventHandlerFn) {
        touchUpHandler = handler
        updateTouchUpRegister()
    }

    /**
     * 设置触摸取消事件处理器，该事件最低兼容1.1.86版本。
     * 注意：为了向下兼容，当没有注册 touchCancel 事件时，touchUp 处理器会接收到取消事件。
     * @param handler 一个 TouchEventHandlerFn，当触摸取消事件发生时调用。
     */
    fun touchCancel(handler: TouchEventHandlerFn) {
        touchUpCompatibilityMode = false
        touchCancelHandler = handler
        updateTouchUpRegister()
    }

    /**
     * 设置触摸移动事件处理器。
     * @param handler 一个 TouchEventHandlerFn，当触摸移动事件发生时调用。
     */
    fun touchMove(handler: TouchEventHandlerFn) {
        register(EventName.TOUCH_MOVE.value) {
            handler(TouchParams.decode(it))
        }
    }

    /**
     * 屏幕刷新帧回调事件（VSYNC信号）。
     * @param handlerFn 一个无参函数，当屏幕刷新帧回调事件发生时调用。
     * 注意：
     * 1. 该回调内不建议执行消耗性能逻辑，因为该回调在平台 UI 线程同步执行。
     * 2. 在不使用该事件时，通过 attr.screenFramePause 属性设置为 true 暂停 Vsync 信号监听回调，提升性能。
     */
    open fun screenFrame(handlerFn: () -> Unit) {
        this.register(EventName.SCREEN_FRAME.value, {
            handlerFn()
        }, true)
    }
}

/**
 * 创建一个类似于 ViewGroup/UIView/Div 的视图容器。
 * @param init 一个 DivView.() -> Unit 函数，用于初始化视图容器的属性和子视图。
 */
fun ViewContainer<*, *>.View(init: DivView.() -> Unit) {
    val viewGroup = createViewFromRegister(ViewConst.TYPE_VIEW_CLASS_NAME) as? DivView
    if (viewGroup != null) { // 存在自定义扩展
        addChild(viewGroup, init)
    } else {
        addChild(DivView(), init)
    }
}
