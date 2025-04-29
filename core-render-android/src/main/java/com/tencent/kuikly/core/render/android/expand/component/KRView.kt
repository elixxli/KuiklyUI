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
import android.graphics.Canvas
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.tencent.kuikly.core.render.android.adapter.KuiklyRenderLog
import com.tencent.kuikly.core.render.android.const.KRViewConst
import com.tencent.kuikly.core.render.android.css.ktx.drawCommonDecoration
import com.tencent.kuikly.core.render.android.css.ktx.drawCommonForegroundDecoration
import com.tencent.kuikly.core.render.android.css.ktx.toDpF
import com.tencent.kuikly.core.render.android.export.IKuiklyRenderViewExport
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback

open class KRView(context: Context) : FrameLayout(context), IKuiklyRenderViewExport {

    private var touchDownCallback: KuiklyRenderCallback? = null
    private var touchMoveCallback: KuiklyRenderCallback? = null
    private var touchUpCallback: KuiklyRenderCallback? = null
    private var screenFrameCallback: ((Long) ->Unit)? = null
    private var screenFramePause: Boolean = false

    private var touchListenerProxy: View.OnTouchListener? = null

    override val reusable: Boolean
        get() = true

    /**
     * 嵌套滚动相关
     */
    var nestedScrollDelegate: ViewGroup? = null

    init {
        setWillDraw()
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean =
        nestedScrollDelegate != null

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        nestedScrollDelegate?.onNestedPreScroll(target, dx, dy, consumed)
            ?: super.onNestedPreScroll(target, dx, dy, consumed)
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        return nestedScrollDelegate?.onNestedPreFling(target, velocityX, velocityY) ?: super.onNestedPreFling(
            target,
            velocityX,
            velocityY
        )
    }

    private fun setScreenFramePause(propValue: Any) {
        val result = propValue == 1
        if (result != screenFramePause) {
            screenFramePause = result
                if (screenFramePause) {
                    screenFrameCallback?.also {
                        Choreographer.getInstance().removeFrameCallback(it)
                    }
                } else {
                    screenFrameCallback?.also {
                        Choreographer.getInstance().postFrameCallback(it)
                    }
                }
        }

    }

    override fun setProp(propKey: String, propValue: Any): Boolean {
        return when (propKey) {
            SCREEN_FRAME_PAUSE -> {
                setScreenFramePause(propValue)
                true
            }
            EVENT_TOUCH_DOWN -> {
                touchDownCallback = propValue as KuiklyRenderCallback
                true
            }
            EVENT_TOUCH_MOVE -> {
                touchMoveCallback = propValue as KuiklyRenderCallback
                true
            }
            EVENT_TOUCH_UP -> {
                touchUpCallback = propValue as KuiklyRenderCallback
                true
            }
            EVENT_SCREEN_FRAME -> {
                setScreenFrameCallback(propValue as? KuiklyRenderCallback)
                true
            }
            else -> super.setProp(propKey, propValue)
        }
    }

    override fun resetProp(propKey: String): Boolean {
        nestedScrollDelegate = null
        touchListenerProxy = null
        return when (propKey) {
            EVENT_TOUCH_DOWN -> {
                touchDownCallback = null
                true
            }
            EVENT_TOUCH_MOVE -> {
                touchMoveCallback = null
                true
            }
            EVENT_TOUCH_UP -> {
                touchUpCallback = null
                true
            }
            EVENT_SCREEN_FRAME -> {
                setScreenFrameCallback(null)
                true
            }
            SCREEN_FRAME_PAUSE -> {
                screenFramePause = false
                true
            }
            else -> super.resetProp(propKey)
        }
    }
    override fun setOnTouchListener(l: OnTouchListener?) {
        touchListenerProxy = object : OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                tryFireTouchEvent(event)
                return l?.onTouch(v, event) ?: false
            }

        }
        super.setOnTouchListener(touchListenerProxy)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val result = super.onTouchEvent(event)
        val touchResult = tryFireTouchEvent(event)
        return result || touchResult
    }

    private fun tryFireTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> tryFireDownEvent(event)
            MotionEvent.ACTION_MOVE -> tryFireMoveEvent(event)
            MotionEvent.ACTION_UP -> tryFireUpEvent(event, EVENT_TOUCH_UP)
            MotionEvent.ACTION_CANCEL -> tryFireUpEvent(event, EVENT_TOUCH_CANCEL)
            else -> false
        }
    }
    override fun draw(canvas: Canvas) {
        drawCommonDecoration(canvas)
        super.draw(canvas)
        drawCommonForegroundDecoration(canvas)
    }

    private fun setWillDraw() {
        setWillNotDraw(false) // HRView有通用样式, 需要开启绘制
    }

    private fun tryFireDownEvent(motionEvent: MotionEvent): Boolean {
        val downCallback = touchDownCallback ?: return false
        downCallback(generateBaseParamsWithTouch(motionEvent, EVENT_TOUCH_DOWN))
        return true
    }

    private fun tryFireUpEvent(motionEvent: MotionEvent, eventName: String): Boolean {
        val upCallback = touchUpCallback ?: return false
        upCallback(generateBaseParamsWithTouch(motionEvent, eventName))
        return true
    }

    private fun tryFireMoveEvent(motionEvent: MotionEvent): Boolean {
        val moveCallback = touchMoveCallback ?: return false
        moveCallback(generateBaseParamsWithTouch(motionEvent, EVENT_TOUCH_MOVE))
        return true
    }

    private fun generateBaseParamsWithTouch(motionEvent: MotionEvent, eventName: String): Map<String, Any> {
        val params = mapOf<String, Any>()
        val krRootView = krRootView() ?: return params
        val rootViewRect = IntArray(2)
        krRootView.getLocationInWindow(rootViewRect)
        val currentViewRect = IntArray(2)
        getLocationInWindow(currentViewRect)
        val x = motionEvent.x
        val y = motionEvent.y
        val touches = arrayListOf<Map<String, Any>>()
        // 遍历所有触摸点
        for (i in 0 until motionEvent.pointerCount) {
            // 获取触摸点的ID和坐标
            val pointerId = motionEvent.getPointerId(i)
            val x = motionEvent.getX(i)
            val y = motionEvent.getY(i)

            touches.add(mapOf(
                KRViewConst.X to x.toDpF(),
                KRViewConst.Y to y.toDpF(),
                PAGE_X to (currentViewRect[0] - rootViewRect[0] + x).toDpF(),
                PAGE_Y to (currentViewRect[1] - rootViewRect[1] + y).toDpF(),
                POINTER_ID to pointerId
            ))
        }
        return (touches.first() ?: mapOf()).toMutableMap().apply {
            put(TOUCHES, touches)
            put(EVENT_ACTION, eventName)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        setScreenFrameCallback(null)
    }


    private fun setScreenFrameCallback(callback: KuiklyRenderCallback?) {
        screenFrameCallback?.also {
            choreographer()?.removeFrameCallback(it)
        }
        if (callback != null) {
            screenFrameCallback = {
                if (!screenFramePause && this.screenFrameCallback != null) {
                    choreographer()?.postFrameCallback(this.screenFrameCallback)
                }
                callback.invoke(null)
            }
            choreographer()?.postFrameCallback(screenFrameCallback)
        } else {
            screenFrameCallback = null
        }
    }

    private fun choreographer(): Choreographer? {
        return try {
            Choreographer.getInstance()
        } catch (e: Throwable) {
            KuiklyRenderLog.e(VIEW_NAME, "get Choreographer.getInstance exception:${e}")
            null
        }
    }

    companion object {
        const val VIEW_NAME = "KRView"
        private const val PAGE_X = "pageX"
        private const val PAGE_Y = "pageY"
        private const val TOUCHES = "touches"
        private const val POINTER_ID = "pointerId"
        private const val SCREEN_FRAME_PAUSE = "screenFramePause"

        private const val EVENT_ACTION = "action"
        private const val EVENT_TOUCH_DOWN = "touchDown"
        private const val EVENT_TOUCH_MOVE = "touchMove"
        private const val EVENT_TOUCH_UP = "touchUp"
        private const val EVENT_TOUCH_CANCEL = "touchCancel"
        private const val EVENT_SCREEN_FRAME = "screenFrame"
    }

}
