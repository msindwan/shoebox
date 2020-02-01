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

    /**
     * Represents a data point in the graph.
     */
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

    private var textPaint = TextPaint().apply {
        isAntiAlias = true
        textSize = 32F
        color = Color.parseColor("#858585")
    }

    private val barPaintBlue = Color.parseColor("#F0695A")
    private val barPaintRed = Color.parseColor("#56B2DC")

    private val budgetChartPath = Path()
    private val budgetChartPathColor = Paint().apply {
        color = Color.parseColor("#12B7D9")
        style = Paint.Style.STROKE
        strokeWidth = 4F
        isAntiAlias = true
    }
    private val budgetChartPointColor = Paint().apply {
        color = Color.parseColor("#12B7D9")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        setup()
    }

    constructor(context: Context) : super(context) {
        setup()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (data != null) {
            val amountMax = data!!.maxBy { t -> t.amount }!!.amount
            val budgetMax = data!!.maxBy { t -> t.budget }!!.budget
            val yMax = max(amountMax, budgetMax).toFloat()

            val yAxisWidth = getYAxisLabelWidth(yMax)
            val graphWidth = width.toFloat() - yAxisWidth
            val xSpace = (graphWidth / (data!!.size))
            val xAxisHeight = getXAxisLabelHeight(xSpace)
            val graphHeight = height.toFloat() - X_AXIS_PADDING_TOP - xAxisHeight
            val graphTop = X_AXIS_PADDING_TOP.toFloat()
            val graphBottom = graphHeight + X_AXIS_PADDING_TOP

            for (i in 0..data!!.size) {
                canvas?.drawLine(
                    yAxisWidth + xSpace * i,
                    graphTop,
                    yAxisWidth + xSpace * i,
                    graphBottom,
                    gridPaint
                )
            }

            for (i in data!!.indices) {
                val x1 = yAxisWidth.toFloat() + (xSpace / 4)
                val x2 = yAxisWidth.toFloat() + xSpace - (xSpace / 4)
                val y1 = graphHeight - (graphHeight * (data!![i].amount / yMax)) + graphTop

                if (data!![i].budget < data!![i].amount) {
                    barPaint.color = barPaintBlue
                    canvas?.drawRect(x1, y1, x2, graphBottom, barPaint)
                } else {
                    barPaint.color = barPaintRed
                    canvas?.drawRect(x1, y1, x2, graphBottom, barPaint)
                }

                val width = xSpace.roundToInt()
                val alignment = Layout.Alignment.ALIGN_CENTER
                val spacingMultiplier = 1f
                val spacingAddition = 0f
                val includePadding = false

                val myStaticLayout = StaticLayout(
                    data!![i].xLabel,
                    textPaint,
                    width,
                    alignment,
                    spacingMultiplier,
                    spacingAddition,
                    includePadding
                )

                canvas?.save()
                canvas?.translate(yAxisWidth + (xSpace * i), graphBottom + 15)
                myStaticLayout.draw(canvas)
                canvas?.restore()
            }

            for (i in 0..5) {
                val yVal = if (i == 0) yMax / 100 else yMax / 100 - (yMax / 500 * i)
                val format = NumberFormatters.getCompactNumberInstance(yVal.roundToLong())

                val alignment = Layout.Alignment.ALIGN_CENTER
                val spacingMultiplier = 1f
                val spacingAddition = 0f
                val includePadding = false

                val myStaticLayout = StaticLayout(
                    "$$format",
                    textPaint,
                    yAxisWidth,
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

            //budgetChartPath.reset()
            for (i in 1..data!!.size) {
                val x = yAxisWidth + xSpace * i - (xSpace / 2)
                val y = graphHeight - (graphHeight * (data!![i - 1].budget / yMax)) + graphTop

                if (i == 1) {
                    budgetChartPath.moveTo(yAxisWidth.toFloat(), y)
                }

                budgetChartPath.lineTo(x, y)
                canvas?.drawCircle(x, y, 15F, budgetChartPointColor)

                if (i == data!!.size) {
                    budgetChartPath.lineTo(width.toFloat(), y)
                }
            }
            canvas?.drawPath(budgetChartPath, budgetChartPathColor)
        }
    }

    private fun getXAxisLabelHeight(xWidth: Float): Int {
        val staticLayouts = mutableListOf<StaticLayout>()
        for (point in data!!) {
            val width = xWidth.roundToInt()
            val alignment = Layout.Alignment.ALIGN_CENTER
            val spacingMultiplier = 1f
            val spacingAddition = 0f
            val includePadding = false

            staticLayouts.add(
                StaticLayout(
                    point.xLabel,
                    textPaint,
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
        var axisWidth = 0

        for (i in 0..5) {
            val yVal = if (i == 0) yMax / 100 else yMax / 100 - (yMax / 500 * i)
            val format = "$${NumberFormatters.getCompactNumberInstance(yVal.roundToLong())}"
            val bounds = textPaint.measureText(format, 0, format.length)
            if (bounds > axisWidth) {
                axisWidth = bounds.roundToInt()
            }
        }

        return axisWidth + 15
    }

    /**
     * Initializes the view.
     */
    private fun setup() {}
}
