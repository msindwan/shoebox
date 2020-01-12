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
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.msindwan.shoebox.R

/**
 * A control to view a certain percentage of a finite amount.
 */
class Gauge : ConstraintLayout {

    private var gaugeProgressRemaining: ProgressBar? = null
    private var gaugeTxtRemaining: TextView? = null
    private var gaugeTxtTotal: TextView? = null

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        setup()
    }

    constructor(context: Context) : super(context) {
        setup()
    }

    /**
     * Initializes the view.
     */
    private fun setup() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.gauge, this, true)

        gaugeTxtRemaining = findViewById(R.id.gauge_txt_remaining)
        gaugeTxtTotal = findViewById(R.id.gauge_txt_total)
        gaugeProgressRemaining = findViewById(R.id.gauge_progress_remaining)
    }

    /**
     * Set the gauge remaining vs total text
     * @param remaining {string} The text to display for the remaining value portion
     * @param total {string} The text to display for the total value portion
     */
    fun setGaugeText(remaining: String, total: String) {
        gaugeTxtTotal?.text = total
        gaugeTxtRemaining?.text = remaining
    }

    /**
     * Sets the percentage for the total remaining value
     *
     * @param percentage {int} The remaining value as a percentage
     */
    fun setPercent(percentage: Int) {
        gaugeProgressRemaining?.progress = percentage
    }
}
