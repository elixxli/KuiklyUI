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
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.View
import com.tencent.kuikly.core.render.android.adapter.KuiklyRenderLog
import com.tencent.kuikly.core.render.android.const.KRCssConst
import com.tencent.kuikly.core.render.android.const.KRViewConst
import com.tencent.kuikly.core.render.android.css.ktx.toColor
import com.tencent.kuikly.core.render.android.css.ktx.toJSONObjectSafely
import com.tencent.kuikly.core.render.android.css.ktx.toPxF
import com.tencent.kuikly.core.render.android.expand.component.text.FontWeightSpan
import com.tencent.kuikly.core.render.android.expand.component.text.TypeFaceUtil
import com.tencent.kuikly.core.render.android.expand.module.KRMemoryCacheModule
import com.tencent.kuikly.core.render.android.export.IKuiklyRenderViewExport
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import org.json.JSONObject
import kotlin.math.PI

/**
 * KTV CanvasView, 用于绘制一些不规则图形
 */
class KRCanvasView(context: Context) : View(context), IKuiklyRenderViewExport {

    private val drawOperationList = mutableListOf<DrawOperation>()
    private var currentDrawStyle = DrawOperation.DrawStyle()

    private val paint by lazy {
        Paint().apply {
            isAntiAlias = true
        }
    }
    private var hrPath: HRPath = HRPath()

    /**
     * HRCanvasView的方法调用处理
     *
     * <p>这里为啥不用使用map<key, handler>来处理?
     *
     * <p>1.方法列表不会太多, 使用when语句的可读性比map<key，handler>的方式好
     *
     * <p>2.一般只有维护者一人编写
     *
     * <p>3.降低内存开销
     *
     * <p>这里的value类型是与kuiklyCore侧约定好的，因此没判断就使用强转
     *
     * @param method 方法名字
     * @param params 参数
     * @param callback 回调
     * @return 如果是同步调用的话，为同步调用结果的返回值
     */
    override fun call(method: String, params: String?, callback: KuiklyRenderCallback?): Any? {
        return when (method) {
            LINE_CAP -> setLineCap(params)
            LINE_WIDTH -> setLineWidth(params)
            LINE_DASH -> setLineDash(params)
            STROKE_STYLE -> setStrokeStyle(params)
            FILL_STYLE -> setFillStyle(params)
            BEGIN_PATH -> beginPath()
            MOVE_TO -> moveTo(params)
            LINE_TO -> lineTo(params)
            ARC -> arc(params)
            CLOSE_PATH -> closePath()
            STROKE -> drawStroke()
            FILL -> drawFill()
            CREATE_LINEAR_GRADIENT -> createLinearGradient(params)
            QUADRATIC_CURVE_TO -> quadraticCurveTo(params)
            BEZIER_CURVE_TO -> bezierCurveTo(params)
            RESET -> reset()
            TEXT_ALIGN -> setTextAlign(params)
            FONT -> setFont(params)
            FILL_TEXT -> fillText(params)
            STROKE_TEXT -> strokeText(params)
            DRAW_IMAGE -> drawImage(params)
            else -> super.call(method, params, callback)
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawCommonDecoration(width, height, canvas) // 绘制通用样式
        performDrawOperationList(canvas) // 绘制canvas指令
        drawCommonForegroundDecoration(width, height, canvas)
    }

    private fun reset() {
        drawOperationList.clear()
        currentDrawStyle = DrawOperation.DrawStyle()
    }

    private fun performDrawOperationList(canvas: Canvas) {
        for (op in drawOperationList) {
            op.draw(paint, canvas)
        }
    }

    private fun setLineCap(params: String?) {
        val paramsJSON = params.toJSONObjectSafely()
        currentDrawStyle.lineCap(paramsJSON.optString(STYLE))
    }

    private fun setLineWidth(params: String?) {
        val paramsJSON = params.toJSONObjectSafely()
        currentDrawStyle.lineWidth(paramsJSON.optDouble(KRViewConst.WIDTH).toFloat().toPxF())
    }

    private fun setLineDash(params: String?) {
        val json = params.toJSONObjectSafely()
        val jsonArray = json.optJSONArray("intervals")
        if (jsonArray == null) {
            currentDrawStyle.lineDash(null)
        } else {
            val intervals = FloatArray(jsonArray.length())
            for (i in intervals.indices) {
                intervals[i] = jsonArray.getDouble(i).toFloat().toPxF()
            }
            currentDrawStyle.lineDash(DashPathEffect(intervals, 0f))
        }
    }

    private fun setStrokeStyle(params: String?) {
        val paramsJSON = params.toJSONObjectSafely()
        currentDrawStyle.strokeStyle(paramsJSON.optString(STYLE))
    }

    private fun setFillStyle(params: String?) {
        val paramsJSON = params.toJSONObjectSafely()
        currentDrawStyle.fillStyle(paramsJSON.optString(STYLE))
    }

    private fun beginPath() {
        hrPath = HRPath()
        hrPath.begin()
    }

    private fun moveTo(params: String?) {
        val paramsJSON = params.toJSONObjectSafely()
        val x = paramsJSON.optDouble(KRViewConst.X).toFloat().toPxF()
        val y = paramsJSON.optDouble(KRViewConst.Y).toFloat().toPxF()
        hrPath.path.moveTo(x, y)
    }

    private fun lineTo(params: String?) {
        val paramsJSON = params.toJSONObjectSafely()
        val x = paramsJSON.optDouble(KRViewConst.X).toFloat().toPxF()
        val y = paramsJSON.optDouble(KRViewConst.Y).toFloat().toPxF()
        hrPath.path.lineTo(x, y)
    }


    private fun arc(params: String?) {
        val paramsJSON = params.toJSONObjectSafely()
        val cx = paramsJSON.optDouble(KRViewConst.X).toFloat().toPxF()
        val cy = paramsJSON.optDouble(KRViewConst.Y).toFloat().toPxF()
        val radius = paramsJSON.optDouble(RADIUS).toFloat().toPxF()
        val startAngle = paramsJSON.optDouble(START_ANGLE) * KRViewConst.PI_AS_ANGLE / PI
        val endAngle = paramsJSON.optDouble(END_ANGLE) * KRViewConst.PI_AS_ANGLE / PI
        val counterclockwise = paramsJSON.optInt(COUNTER_CLOCKWISE) == TYPE_COUNTER_CLOCKWISE
        var sweepAngle = endAngle - startAngle
        if (counterclockwise) {
            if (sweepAngle > 0) {
                sweepAngle = sweepAngle % KRViewConst.ROUND_ANGLE - KRViewConst.ROUND_ANGLE
            }
        } else {
            if (sweepAngle < 0) {
                sweepAngle = sweepAngle % KRViewConst.ROUND_ANGLE + KRViewConst.ROUND_ANGLE
            }
        }

        if (-KRViewConst.ROUND_ANGLE < sweepAngle && sweepAngle < KRViewConst.ROUND_ANGLE) {
            // deal with arc less than 2π
            hrPath.path.arcTo(
                cx - radius, cy - radius, cx + radius, cy + radius,
                startAngle.toFloat(),
                sweepAngle.toFloat(),
                false
            )
        } else {
            // deal with arc greater than or equal to 2π
            // lineTo start-point
            hrPath.path.arcTo(
                cx - radius, cy - radius, cx + radius, cy + radius,
                startAngle.toFloat(),
                0f,
                false
            )
            // draw circle
            hrPath.path.addCircle(cx, cy, radius, if (counterclockwise) Path.Direction.CCW else Path.Direction.CW)
            if (sweepAngle < -KRViewConst.ROUND_ANGLE || KRViewConst.ROUND_ANGLE < sweepAngle) {
                // moveTo end-point
                hrPath.path.arcTo(
                    cx - radius, cy - radius, cx + radius, cy + radius,
                    endAngle.toFloat(),
                    0f,
                    true
                )
            }
        }
    }

    private fun closePath() {
        hrPath.path.close()
    }

    private fun drawStroke() {
        currentDrawStyle.drawStyle(Paint.Style.STROKE)
        flushDrawCommand()
    }

    private fun drawFill() {
        currentDrawStyle.drawStyle(Paint.Style.FILL)
        flushDrawCommand()
    }

    private fun flushDrawCommand() {
        hrPath.also {
            it.pushDrawStyle(DrawOperation.DrawStyle().apply {
                lineCap = currentDrawStyle.lineCap
                fillColor = currentDrawStyle.fillColor
                fillGradient = currentDrawStyle.fillGradient
                strokeColor = currentDrawStyle.strokeColor
                strokeGradient = currentDrawStyle.strokeGradient
                lineWidth = currentDrawStyle.lineWidth
                drawStyle = currentDrawStyle.drawStyle
                lineDash = currentDrawStyle.lineDash
            })
            if (drawOperationList.contains(it)) {
                // 已经含有currentDrawOperation的话移除掉，保证绘制的指令时最新的
                drawOperationList.remove(it)
            }
            drawOperationList.add(it)
            invalidate()
        }
    }
    private fun createLinearGradient(params: String?) {
    }

    private fun quadraticCurveTo(params: String?) {
        val json = params.toJSONObjectSafely()
        val cpx = json.optDouble("cpx").toFloat().toPxF()
        val cpy = json.optDouble("cpy").toFloat().toPxF()
        val x = json.optDouble(KRViewConst.X).toFloat().toPxF()
        val y = json.optDouble(KRViewConst.Y).toFloat().toPxF()
        hrPath.path.quadTo(cpx, cpy, x, y)
    }

    private fun bezierCurveTo(params: String?) {
        val json = params.toJSONObjectSafely()
        val cp1x = json.optDouble("cp1x").toFloat().toPxF()
        val cp1y = json.optDouble("cp1y").toFloat().toPxF()
        val cp2x = json.optDouble("cp2x").toFloat().toPxF()
        val cp2y = json.optDouble("cp2y").toFloat().toPxF()
        val x = json.optDouble(KRViewConst.X).toFloat().toPxF()
        val y = json.optDouble(KRViewConst.Y).toFloat().toPxF()
        hrPath.path.cubicTo(cp1x, cp1y, cp2x, cp2y, x, y)
    }

    private fun setTextAlign(params: String?) {
        currentDrawStyle.textAlign = when (params) {
            "center" -> Paint.Align.CENTER
            "right" -> Paint.Align.RIGHT
            else -> Paint.Align.LEFT
        }
    }

    private fun setFont(params: String?) {
        val json = params.toJSONObjectSafely()
        val size = json.optDouble("size", 0.0)
        if (size > 0) {
            currentDrawStyle.textSize = size.toFloat().toPxF()
        }
        currentDrawStyle.fontStyle = json.optString("style")
        currentDrawStyle.fontWeight = json.optString("weight")
        currentDrawStyle.fontFamily = json.optString("family")
    }

    private fun fillText(params: String?) {
        currentDrawStyle.drawStyle(Paint.Style.FILL)
        flushTextCommand(params.toJSONObjectSafely())
    }

    private fun strokeText(params: String?) {
        currentDrawStyle.drawStyle(Paint.Style.STROKE)
        flushTextCommand(params.toJSONObjectSafely())
    }

    private fun flushTextCommand(json: JSONObject) {
        val text = json.optString("text")
        if (text.isEmpty()) {
            return
        }
        val x = json.optDouble("x")
        val y = json.optDouble("y")
        val drawStyle = DrawOperation.DrawStyle().apply {
            lineCap = currentDrawStyle.lineCap
            if (currentDrawStyle.drawStyle == Paint.Style.FILL) {
                fillColor = currentDrawStyle.fillColor
                fillGradient = currentDrawStyle.fillGradient
                lineWidth = FontWeightSpan.getFontWeight(currentDrawStyle.fontWeight)
                if (lineWidth > 0) {
                    strokeColor = currentDrawStyle.fillColor
                    strokeGradient = currentDrawStyle.fillGradient
                    drawStyle = Paint.Style.FILL_AND_STROKE
                } else {
                    drawStyle = Paint.Style.FILL
                }
            } else {
                strokeColor = currentDrawStyle.strokeColor
                strokeGradient = currentDrawStyle.strokeGradient
                lineWidth = currentDrawStyle.lineWidth
                drawStyle = Paint.Style.STROKE
            }
            textAlign = currentDrawStyle.textAlign
            textSize = currentDrawStyle.textSize
            val italic = currentDrawStyle.fontStyle == KRTextProps.FONT_STYLE_ITALIC
            typeface = TypeFaceUtil.getTypeface(currentDrawStyle.fontFamily, italic)
        }
        val op = TextOp(text, x.toFloat().toPxF(), y.toFloat().toPxF(), drawStyle)
        drawOperationList.add(op)
        invalidate()
    }

    private fun drawImage(params: String?) {
        val json = params.toJSONObjectSafely()
        val cacheKey = json.optString("cacheKey")
        val drawable: Any? = kuiklyRenderContext?.module<KRMemoryCacheModule>(KRMemoryCacheModule.MODULE_NAME)?.get(cacheKey)
        if (drawable !is Drawable) {
            KuiklyRenderLog.e("KRCanvas", "image cacheKey invalid")
            return
        }
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        val sx = json.optInt("sx", 0)
        val sy = json.optInt("sy", 0)
        val sWidth = json.optInt("sWidth", drawable.intrinsicWidth)
        val sHeight = json.optInt("sHeight", drawable.intrinsicHeight)
        val dx = json.optDouble("dx", 0.0)
        val dy = json.optDouble("dy", 0.0)
        val dWidth = json.optDouble("dWidth", sWidth.toDouble())
        val dHeight = json.optDouble("dHeight", sHeight.toDouble())
        val op = ImageOp(
            drawable,
            dx.toFloat().toPxF(),
            dy.toFloat().toPxF(),
            dWidth.toFloat().toPxF(),
            dHeight.toFloat().toPxF(),
            sx,
            sy,
            sWidth,
            sHeight
        )
        drawOperationList.add(op)
        invalidate()
    }

    companion object {
        const val VIEW_NAME = "KRCanvasView"

        private const val LINE_CAP = "lineCap"
        private const val LINE_WIDTH = "lineWidth"
        private const val STROKE_STYLE = "strokeStyle"
        private const val FILL_STYLE = "fillStyle"
        private const val BEGIN_PATH = "beginPath"
        private const val MOVE_TO = "moveTo"
        private const val LINE_TO = "lineTo"
        private const val ARC = "arc"
        private const val CLOSE_PATH = "closePath"
        private const val STROKE = "stroke"
        private const val FILL = "fill"
        private const val STYLE = "style"
        private const val RADIUS = "r"
        private const val START_ANGLE = "sAngle"
        private const val END_ANGLE = "eAngle"
        private const val COUNTER_CLOCKWISE = "counterclockwise"
        private const val CREATE_LINEAR_GRADIENT = "createLinearGradient"
        private const val QUADRATIC_CURVE_TO = "quadraticCurveTo"
        private const val BEZIER_CURVE_TO = "bezierCurveTo"
        private const val RESET = "reset"
        private const val TYPE_COUNTER_CLOCKWISE = 1
        private const val TEXT_ALIGN = "textAlign"
        private const val FONT = "font"
        private const val FILL_TEXT = "fillText"
        private const val STROKE_TEXT = "strokeText"
        private const val DRAW_IMAGE = "drawImage"
        private const val LINE_DASH = "lineDash"
    }
}

private class HRPath : DrawOperation() {

    val path: Path = Path()

    val drawStyleList = mutableListOf<DrawStyle>()

    fun pushDrawStyle(drawStyle: DrawStyle) {
        drawStyleList.add(drawStyle)
    }

    fun popDrawStyle() {
        drawStyleList.removeAt(drawStyleList.size - 1)
    }

    fun clearDrawStyle() {
        drawStyleList.clear()
    }

    fun begin() {
        path.reset()
        clearDrawStyle()
    }

    override fun draw(paint: Paint, canvas: Canvas) {
        // 设置可绘制区域为 Canvas 的整个布局区域
        canvas.clipRect(0, 0, canvas.width, canvas.height)
        for (drawStyle in drawStyleList) {
            drawStyle.applyStyle(paint)
            canvas.drawPath(path, paint)
        }
    }

}

private class TextOp(val text: String, val x: Float, val y: Float, val drawStyle: DrawStyle): DrawOperation() {

    override fun draw(paint: Paint, canvas: Canvas) {
        // 设置可绘制区域为 Canvas 的整个布局区域
        canvas.clipRect(0, 0, canvas.width, canvas.height)
        drawStyle.applyStyle(paint)
        drawStyle.typeface?.also { paint.typeface = it }
        paint.textAlign = drawStyle.textAlign
        paint.textSize = drawStyle.textSize
        canvas.drawText(text, x, y, paint)
    }

}

private class ImageOp(
    val drawable: Drawable,
    val dx: Float,
    val dy: Float,
    val dWidth: Float,
    val dHeight: Float,
    val sx: Int,
    val sy: Int,
    val sWidth: Int,
    val sHeight: Int
) : DrawOperation() {
    override fun draw(paint: Paint, canvas: Canvas) {
        canvas.save()
        canvas.clipRect(dx, dy, dx + dWidth, dy + dHeight)
        val scaleX = dWidth / sWidth
        val scaleY = dHeight / sHeight
        canvas.translate(dx, dy)
        canvas.scale(scaleX, scaleY)
        canvas.translate(-sx.toFloat(), -sy.toFloat())
        drawable.draw(canvas)
        canvas.restore()
    }

}

private abstract class DrawOperation {

    abstract fun draw(paint: Paint, canvas: Canvas)

    class DrawStyle {
        var lineCap = Paint.Cap.BUTT
        var lineWidth = 0f
        var lineDash: DashPathEffect? = null
        var drawStyle = Paint.Style.STROKE
        var fillColor: Int? = null
        var fillGradient: LinearGradient? = null

        var strokeColor: Int? = null
        var strokeGradient: LinearGradient? = null
        var textAlign = Paint.Align.LEFT
        var typeface: Typeface? = null
        var textSize: Float = 15f
        var fontStyle: String = KRCssConst.EMPTY_STRING
        var fontWeight: String = KRCssConst.EMPTY_STRING
        var fontFamily: String = KRCssConst.EMPTY_STRING

        fun lineCap(cap: String): DrawStyle = apply {
            lineCap = when (cap) {
                LINE_CAP_TYPE_BUTT -> Paint.Cap.BUTT
                LINE_CAP_TYPE_ROUND -> Paint.Cap.ROUND
                LINE_CAP_TYPE_SQUARE -> Paint.Cap.SQUARE
                else -> Paint.Cap.BUTT
            }
        }

        fun lineWidth(lineWidth: Float): DrawStyle = apply {
            this.lineWidth = lineWidth
        }

        fun lineDash(dashPathEffect: DashPathEffect?): DrawStyle = apply {
            this.lineDash = dashPathEffect
        }

        fun fillStyle(style: String): DrawStyle = apply {
            fillGradient = tryParseGradient(style)
            fillColor = if (fillGradient == null) {
                style.toColor()
            } else {
                null
            }
        }

        fun strokeStyle(style: String): DrawStyle = apply {
            strokeGradient = tryParseGradient(style)
            strokeColor = if (strokeGradient == null) {
                style.toColor()
            } else {
                null
            }
        }

        fun drawStyle(style: Paint.Style): DrawStyle = apply {
            this.drawStyle = style
        }

        fun applyStyle(paint: Paint) {
            paint.strokeCap = lineCap
            paint.style = drawStyle
            if (drawStyle == Paint.Style.FILL) {
                internalApplyStyle(fillGradient, fillColor, paint)
                paint.strokeWidth = 0f
            } else {
                internalApplyStyle(strokeGradient, strokeColor, paint)
                paint.strokeWidth = lineWidth
            }
            paint.setPathEffect(lineDash)
        }

        private fun internalApplyStyle(gradient: LinearGradient?, color: Int?, paint: Paint) {
            if (color != null) {
                paint.color = color
                paint.shader = null
                return
            }
            if (gradient != null) {
                paint.shader = gradient
                return
            }
        }

        private fun tryParseGradient(style: String): LinearGradient? {
            val gradientPrefix = "linear-gradient"
            return if (style.startsWith(gradientPrefix)) {
                createLinearGradient(style.substring(gradientPrefix.length))
            } else {
                null
            }
        }

        private fun createLinearGradient(params: String): LinearGradient {
            val paramsJSON = params.toJSONObjectSafely()
            val leftX = paramsJSON.optDouble("x0").toFloat().toPxF()
            val leftY = paramsJSON.optDouble("y0").toFloat().toPxF()
            val rightX = paramsJSON.optDouble("x1").toFloat().toPxF()
            val rightY = paramsJSON.optDouble("y1").toFloat().toPxF()
            val colorStops = paramsJSON.optString("colorStops").split(",")
            val colors = IntArray(colorStops.size)
            val positions = FloatArray(colorStops.size)
            colorStops.forEachIndexed { index, s ->
                val colorAndPosition = s.split(" ")
                colors[index] = colorAndPosition[0].toColor()
                positions[index] = colorAndPosition[1].toFloat()
            }
            return LinearGradient(
                leftX,
                leftY,
                rightX,
                rightY,
                colors,
                positions,
                Shader.TileMode.CLAMP
            )
        }


        companion object {
            private const val LINE_CAP_TYPE_BUTT = "butt"
            private const val LINE_CAP_TYPE_ROUND = "round"
            private const val LINE_CAP_TYPE_SQUARE = "square"
        }
    }
}
