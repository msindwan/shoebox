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

package com.msindwan.shoebox.widgets

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import android.R.attr.typeface
import com.msindwan.shoebox.data.entities.LocalDateRange
import android.icu.lang.UCharacter.GraphemeClusterBreak.T




class BudgetGraph : View {

    class Point(
        val budget: Long,
        val amount: Long,
        val xLabels: List<String>
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

        val yAxisWidth = 5
        val xAxisWidth = width - yAxisWidth
        val xAxisLeft = yAxisWidth
        val xAxisHeight = 70
        val yAxisTop = X_AXIS_PADDING_TOP.toFloat()
        val graphWidth = width.toFloat() - yAxisWidth
        val graphHeight = height.toFloat() - X_AXIS_PADDING_TOP - xAxisHeight
        val graphTop = X_AXIS_PADDING_TOP.toFloat()
        val graphBottom = graphHeight + X_AXIS_PADDING_TOP

        if (data != null) {
            val amountMax = data!!.maxBy { t -> t.amount }!!.amount
            val budgetMax = data!!.maxBy { t -> t.budget }!!.budget
            val yMax = max(amountMax, budgetMax).toFloat()
            val xSpace = (graphWidth / (data!!.size)) - 7

            for (i in 1..data!!.size) {
                canvas?.drawLine(xAxisLeft + xSpace * i, graphTop, xAxisLeft + xSpace * i, graphBottom, gridPaint)
            }

            for (i in 1..data!!.size) {
                val x1 = xAxisLeft + (xSpace * i - (xSpace / 4))
                val x2 = xAxisLeft + (xSpace * i + (xSpace / 4))
                val y1 = graphHeight - (graphHeight * (data!![i - 1].amount / yMax)) + graphTop

                if (data!![i - 1].budget < data!![i - 1].amount) {
                    canvas?.drawRect(x1, y1, x2, graphBottom, barPaint)
                } else {
                    canvas?.drawRect(x1, y1, x2, graphBottom,  Paint().apply {
                        color = Color.parseColor("#56B2DC")
                        style = Paint.Style.FILL
                        isAntiAlias = true
                    })
                }

                val p = Paint()
                p.textSize = 32F
                p.color = Color.parseColor("#858585")

                var padding = 0
                for (label in data!![i - 1].xLabels) {
                    val bounds = Rect()
                    p.getTextBounds(label, 0, label.length, bounds)
                    val textHeight = bounds.height()
                    val textWidth = bounds.width()

                    padding += textHeight + 10
                    canvas?.drawText(label,xAxisLeft + xSpace * i - textWidth/2, graphBottom + padding, p)
                }
            }

            for (i in 0..4) {
                val bounds = Rect()
                val p = Paint()
                p.textSize = 32F
                p.color = Color.parseColor("#858585")

                p.getTextBounds("$20K", 0, 4, bounds)
                val textHeight = bounds.height()
                canvas?.drawText("$20K",0F, graphTop + (graphHeight / 5) * i + textHeight, p)
            }

            val path = Path()
            var firstY: Float? = null
            var firstX: Float? = null

            for (i in 1..data!!.size) {
                val x = xAxisLeft + xSpace * i
                val y = graphHeight - (graphHeight * (data!![i - 1].budget / yMax)) + graphTop

                if (firstY == null) {
                    firstY = y
                }
                if (firstX == null) {
                    firstX = x
                }

                if (i == 1) {
                    path.moveTo(x, y)
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
                }
            }

            // path.lineTo(graphWidth - 4, graphHeight - (graphHeight * (data!!.last().budget / yMax)) + graphTop)
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

    /**
     * Initializes the view.
     */
    private fun setup() {
    }
}
