/**
 * Copyright (C) 2020 Mayank Sindwani
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.msindwan.shoebox.views.dashboard.components

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import android.text.StaticLayout
import android.text.Layout
import android.text.TextPaint
import com.msindwan.shoebox.helpers.NumberFormatters
import kotlin.math.roundToInt
import kotlin.math.roundToLong


class BudgetGraph : View {

    class Point(
        val budget: Long,
        val amount: Long,
        val xLabel: String
    )

    companion object {
        const val X_AXIS_PADDING_TOP = 15
    }

    var data: List<Point>? = null
        set(value) {
            field = value
            invalidate()
        }

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        setup()
    }

    constructor(context: Context) : super(context) {
        setup()
    }

    private var gridPaint = Paint().apply {
        color = Color.parseColor("#E6E6E6")
        style = Paint.Style.STROKE
        strokeWidth = 2F
        isAntiAlias = true
    }

    private var barPaint = Paint().apply {
        color = Color.parseColor("#F0695A")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (data != null) {
            val amountMax = data!!.maxBy { t -> t.amount }!!.amount
            val budgetMax = data!!.maxBy { t -> t.budget }!!.budget
            val yMax = max(amountMax, budgetMax).toFloat()

            val yAxisWidth = getYAxisLabelWidth(yMax)
            val xAxisWidth = width - yAxisWidth
            val xAxisLeft = yAxisWidth
            val graphWidth = width.toFloat() - yAxisWidth
            val xSpace = (graphWidth / (data!!.size))
            val xAxisHeight = getXAxisLabelHeight(xSpace)
            val yAxisTop = X_AXIS_PADDING_TOP.toFloat()
            val graphHeight = height.toFloat() - X_AXIS_PADDING_TOP - xAxisHeight
            val graphTop = X_AXIS_PADDING_TOP.toFloat()
            val graphBottom = graphHeight + X_AXIS_PADDING_TOP

            val myTextPaint = TextPaint()
            myTextPaint.isAntiAlias = true
            myTextPaint.textSize = 32F
            myTextPaint.color = Color.parseColor("#858585")

            for (i in 0..data!!.size) {
                canvas?.drawLine(xAxisLeft + xSpace * i, graphTop, xAxisLeft + xSpace * i, graphBottom, gridPaint)
            }

            for (i in 0..data!!.size - 1) {
                val x1 = xAxisLeft.toFloat() + (xSpace / 4)
                val x2 = xAxisLeft.toFloat() + xSpace - (xSpace / 4)
                val y1 = graphHeight - (graphHeight * (data!![i].amount / yMax)) + graphTop

                if (data!![i].budget < data!![i].amount) {
                    canvas?.drawRect(x1, y1, x2, graphBottom, barPaint)
                } else {
                    canvas?.drawRect(x1, y1, x2, graphBottom,  Paint().apply {
                        color = Color.parseColor("#56B2DC")
                        style = Paint.Style.FILL
                        isAntiAlias = true
                    })
                }

                // val p = Paint()
                // p.textSize = 32F
                // p.color = Color.parseColor("#858585")

                // var padding = 0
                // val bounds = Rect()
                // p.getTextBounds(data!![i - 1].xLabel, 0, data!![i - 1].xLabel.length, bounds)
                // val textHeight = bounds.height()
                // val textWidth = bounds.width()

                // padding += textHeight + 10
                // canvas?.drawText(data!![i - 1].xLabel,xAxisLeft + xSpace * i - textWidth/2, graphBottom + padding, p)

                val width = xSpace.roundToInt()
                val alignment = Layout.Alignment.ALIGN_CENTER
                val spacingMultiplier = 1f
                val spacingAddition = 0f
                val includePadding = false

                val myStaticLayout = StaticLayout(
                    data!![i].xLabel,
                    myTextPaint,
                    width,
                    alignment,
                    spacingMultiplier,
                    spacingAddition,
                    includePadding
                )

                canvas?.save()
                canvas?.translate(xAxisLeft + (xSpace * i), graphBottom + 15)
                myStaticLayout.draw(canvas)
                canvas?.restore()
            }

            for (i in 0..5) {
                val p = Paint()
                p.textSize = 32F
                p.color = Color.parseColor("#858585")

                val yVal = if (i == 0) yMax / 100 else yMax / 100 - (yMax / 500 * i)
                val format = NumberFormatters.getCompactNumberInstance(yVal.roundToLong())

                val width = yAxisWidth
                val alignment = Layout.Alignment.ALIGN_CENTER
                val spacingMultiplier = 1f
                val spacingAddition = 0f
                val includePadding = false

                val myStaticLayout = StaticLayout(
                    "$$format",
                    myTextPaint,
                    width,
                    alignment,
                    spacingMultiplier,
                    spacingAddition,
                    includePadding
                )

                canvas?.save()
                canvas?.translate(0F, graphTop + (graphHeight / 5) * i - myStaticLayout.height / 2)
                myStaticLayout.draw(canvas)
                canvas?.restore()
            }

            val path = Path()
            for (i in 1..data!!.size) {
                val x = xAxisLeft + xSpace * i - (xSpace/2)
                val y = graphHeight - (graphHeight * (data!![i - 1].budget / yMax)) + graphTop

                if (i == 1) {
                    path.moveTo(xAxisLeft.toFloat(), y)
                    path.lineTo(x, y)
                    canvas?.drawCircle(x, y, 15F, Paint().apply {
                        color = Color.parseColor("#12B7D9")
                        style = Paint.Style.FILL
                        isAntiAlias = true
                    })
                } else {
                    path.lineTo(x, y)
                    canvas?.drawCircle(x, y, 15F, Paint().apply {
                        color = Color.parseColor("#12B7D9")
                        style = Paint.Style.FILL
                        isAntiAlias = true
                    })

                    if (i == data!!.size) {
                        path.lineTo(width.toFloat(), y)
                    }
                }
            }

            // path.lineTo(graphWidth - 4, graphBottom)
            // path.lineTo(0F + 4, graphBottom)
            // path.lineTo(0F + 4, firstY!!)

            canvas?.drawPath(path, Paint().apply {
                color = Color.parseColor("#12B7D9")
                style = Paint.Style.STROKE
                strokeWidth = 4F
                isAntiAlias = true
            })

            // canvas?.drawPath(path, Paint().apply {
            //    color = Color.parseColor("#0090AC")
            //    alpha = 30
            //    style = Paint.Style.FILL
            //    strokeWidth = 4F
            //    isAntiAlias = true
            // })
        }
    }

    private fun getXAxisLabelHeight(xWidth: Float): Int {
        val staticLayouts = mutableListOf<StaticLayout>()
        for (i in 1..data!!.size) {
            val myTextPaint = TextPaint()
            myTextPaint.isAntiAlias = true
            myTextPaint.textSize = 32F
            myTextPaint.color = Color.parseColor("#858585")

            val width = xWidth.roundToInt()
            val alignment = Layout.Alignment.ALIGN_CENTER
            val spacingMultiplier = 1f
            val spacingAddition = 0f
            val includePadding = false

            staticLayouts.add(
                StaticLayout(
                    data!![i - 1].xLabel,
                    myTextPaint,
                    width,
                    alignment,
                    spacingMultiplier,
                    spacingAddition,
                    includePadding
                )
            )
        }

        return (staticLayouts.maxBy { l -> l.height }?.height ?: 0) + 15
    }

    private fun getYAxisLabelWidth(yMax: Float): Int {
        val myTextPaint = TextPaint()

        myTextPaint.isAntiAlias = true
        myTextPaint.textSize = 32F
        myTextPaint.color = Color.parseColor("#858585")

        var axisWidth = 0

        for (i in 0..5) {
            val yVal = if (i == 0) yMax / 100 else yMax / 100 - (yMax / 500 * i)
            val format = "$${NumberFormatters.getCompactNumberInstance(yVal.roundToLong())}"
            val bounds = myTextPaint.measureText(format, 0, format.length)
            if (bounds > axisWidth) {
                axisWidth = bounds.roundToInt()
            }
        }

        return axisWidth + 15
    }

    /**
     * Initializes the view.
     */
    private fun setup() {
    }
}
