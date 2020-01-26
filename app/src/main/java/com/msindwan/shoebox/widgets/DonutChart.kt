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

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import android.widget.LinearLayout
import android.graphics.RectF
import android.view.ViewGroup
import com.msindwan.shoebox.R


class DonutChart : View {

    private lateinit var textContainerLayout: LinearLayout
    private lateinit var remainingText: TextView
    private lateinit var totalText: TextView
    private var currentPercentage = 0F
    private var progress: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    private val backgroundWidth = 150f
    private val progressWidth = 150f
    private val backgroundPaint = Paint().apply {
        color = Color.parseColor("#72E7FF")
        style = Paint.Style.STROKE
        strokeWidth = progressWidth
        isAntiAlias = true
    }
    private var progressPaint = Paint().apply {
        color = Color.parseColor("#12B7D9")
        style = Paint.Style.STROKE
        strokeWidth = progressWidth
        isAntiAlias = true
    }
    private val arc = RectF()
    private var arcX: Float = 0f
    private var arcY: Float = 0f
    private var arcRadius: Float = 0f

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        setup()
    }

    constructor(context: Context) : super(context) {
        setup()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        arcX = w.toFloat() / 2
        arcY = h.toFloat() / 2
        arcRadius = w.toFloat() / 2 - progressWidth
        arc.set(
            arcX - arcRadius,
            arcY - arcRadius,
            arcX + arcRadius,
            arcY + arcRadius
        )
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val scaledValues = arrayOf(0f, 90f, 180f, 270f)
        val colors = arrayOf(
            Color.parseColor("#12B7D9"),
            Color.parseColor("#5EC7DC"),
            Color.parseColor("#226E7D"),
            Color.parseColor("#626F72")
        )

        for (i in scaledValues.indices) {
            progressPaint.setColor(colors[i])
            canvas?.drawArc(arc, scaledValues[i], 90f, false, progressPaint)

        }

        /*for (i in scaledValues.indices) {
            val radius = 300f
            val x = (radius * Math.cos(1 * Math.PI / 180f)).toFloat() + width / 2 - 10
            val y =
                (radius * Math.sin(1 * Math.PI / 180f)).toFloat() + height / 2 - 20
            canvas?.drawText("($i)", x, y, Paint().apply {
                textSize = 50F
                color = Color.WHITE
            })

        }*/

    }

    /**
     * Initializes the view.
     */
    private fun setup() {

    }

    public override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)
        setMeasuredDimension(measuredWidth, measuredWidth)
    }

    /**
     * Set the gauge remaining vs total text.
     *
     * @param remaining {string} The text to display for the remaining value portion.
     * @param total {string} The text to display for the total value portion.
     */
    fun setGaugeText(remaining: String, total: String) {
        totalText.text = total
        remainingText.text = remaining
        invalidate()
    }

    /**
     * Sets the paint color for the progress bar.
     *
     * @param c {Int} The color to set.
     */
    fun setProgressBarColor(c: Int) {
        progressPaint = Paint().apply {
            color = c
            style = Paint.Style.STROKE
            strokeWidth = progressWidth
            isAntiAlias = true
        }
    }

    /**
     * Sets the percentage for the total remaining value.
     *
     * @param percentage {Float} The remaining value as a percentage.
     */
    fun setPercent(percentage: Float) {
        val minPercentage = percentage.coerceAtLeast(0F)

        if (minPercentage != currentPercentage) {
            val animator: ValueAnimator = ValueAnimator.ofFloat(progress, minPercentage)

            animator.duration = 500
            animator.interpolator = DecelerateInterpolator()
            animator.addUpdateListener { animation ->
                progress = animation.animatedValue as Float
            }
            animator.start()
            currentPercentage = minPercentage
        }
    }
}
