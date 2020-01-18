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
import com.msindwan.shoebox.R
import androidx.core.content.res.ResourcesCompat
import android.widget.LinearLayout


/**
 * A control to view a certain percentage of a finite amount.
 * Implementation based on
 * https://stackoverflow.com/questions/21333866/how-to-create-a-circular-progressbar-in-android-which-rotates-on-it
 */
class Gauge : View {

    private lateinit var textContainerLayout: LinearLayout
    private lateinit var remainingText: TextView
    private lateinit var totalText: TextView
    private var currentPercentage = 0F
    private var progress: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    private val backgroundWidth = 25f
    private val progressWidth = 25f
    private val backgroundPaint = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = progressWidth
        isAntiAlias = true
    }
    private var progressPaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = backgroundWidth
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
        canvas?.drawCircle(arcX, arcY, arcRadius, backgroundPaint)
        canvas?.drawArc(arc, 270f, 360f * progress, false, progressPaint)
        textContainerLayout.measure(width, height)
        textContainerLayout.layout(0, 0, width, height)
        textContainerLayout.draw(canvas)
    }

    /**
     * Initializes the view.
     */
    private fun setup() {
        textContainerLayout = LinearLayout(context)
        textContainerLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        textContainerLayout.orientation = LinearLayout.HORIZONTAL
        textContainerLayout.gravity = Gravity.CENTER

        remainingText = TextView(context)
        remainingText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
        remainingText.typeface = ResourcesCompat.getFont(context, R.font.staatliches_font_family)

        val divider = TextView(context)
        divider.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
        divider.typeface = ResourcesCompat.getFont(context, R.font.staatliches_font_family)
        divider.text = resources.getString(R.string.gauge_fraction_divider)

        divider.setPadding(5, 0, 5, 0)

        totalText = TextView(context)
        totalText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
        totalText.typeface = ResourcesCompat.getFont(context, R.font.staatliches_font_family)

        textContainerLayout.addView(remainingText)
        textContainerLayout.addView(divider)
        textContainerLayout.addView(totalText)
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
