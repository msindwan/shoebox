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
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.msindwan.shoebox.R
import kotlin.math.roundToInt
import android.util.TypedValue
import android.widget.TextView


/**
 * Groups buttons into a single container with a select state.
 */
class ButtonToggleGroup : LinearLayout {
    private val buttons: MutableList<TextView> = mutableListOf()
    var onButtonClickedListener: ((view: View) -> Unit)? = null

    constructor(context: Context) : super(context) {
        setup()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        setup()
    }

    /**
     * Initializes the view.
     */
    private fun setup() {}

    /**
     * Adds a button to the group.
     *
     * @param text {String} The button text.
     * @param tag {Any} The unique tag to reference the button by.
     */
    fun addButton(text: String, tag: Any) {
        val button = TextView(context)
        val buttonPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            8F,
            context.resources.displayMetrics
        ).roundToInt()

        button.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding)
        button.gravity = Gravity.CENTER
        button.text = text
        // @todo Text color constant
        button.setTextColor(Color.parseColor("#8B8B8B"))
        button.typeface = ResourcesCompat.getFont(context, R.font.staatliches_font_family)
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)

        button.setOnClickListener(onButtonClicked)
        button.tag = tag

        val buttonTheme = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, buttonTheme, true)
        button.setBackgroundResource(buttonTheme.resourceId)

        if (buttons.isNotEmpty()) {
            val divider = View(context)
            val dividerWidth = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                1F,
                context.resources.displayMetrics
            ).roundToInt()
            val dividerMargin = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                5F,
                context.resources.displayMetrics
            ).roundToInt()

            val dividerLayout = MarginLayoutParams(
                dividerWidth,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            dividerLayout.setMargins(
                dividerMargin,
                0,
                dividerMargin,
                0
            )
            // @todo Text color constant
            divider.setBackgroundColor(Color.parseColor("#C2C2C2"))
            divider.layoutParams = dividerLayout
            addView(divider)
        }

        addView(button)
        buttons.add(button)
    }

    /**
     * Sets the selected button in the toggle group.
     *
     * @param tag {Any} The tag of the button to select.
     */
    fun setSelectedButton(tag: Any) {
        for (button in buttons) {
            if (button.tag == tag) {
                button.setTextColor(ResourcesCompat.getColor(resources, R.color.colorPrimary, null))
            } else {
                // @todo Text color constant
                button.setTextColor(Color.parseColor("#8B8B8B"))
            }
        }
    }

    /**
     * Handles clicking on one of the buttons in the button group.
     */
    private val onButtonClicked = OnClickListener {
        setSelectedButton(it.tag)
        onButtonClickedListener?.invoke(it)
    }
}