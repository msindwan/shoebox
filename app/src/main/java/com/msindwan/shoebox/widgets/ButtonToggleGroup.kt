package com.msindwan.shoebox.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.msindwan.shoebox.R
import kotlin.math.roundToInt
import android.util.TypedValue


class ButtonToggleGroup: LinearLayout {
    private val buttons: MutableList<Button> = mutableListOf()
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
    private fun setup() {
    }

    fun addButton(text: String, tag: Any) {
        val button = Button(context)
        button.setPadding(
            (5F * resources.displayMetrics.density).roundToInt(),
            (5F * resources.displayMetrics.density).roundToInt(),
            (5F * resources.displayMetrics.density).roundToInt(),
            (5F * resources.displayMetrics.density).roundToInt()
        )
        button.gravity = Gravity.CENTER
        button.text = text
        // @todo Text color
        button.setTextColor(Color.parseColor("#8B8B8B"))
        button.typeface = ResourcesCompat.getFont(context, R.font.staatliches_font_family)
        button.textSize = 20F

        button.setOnClickListener(onButtonClicked)
        button.tag = tag
        val outValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        button.setBackgroundResource(outValue.resourceId)

        if (buttons.isNotEmpty()) {
            val divider = View(context)
            val dividerLayout = MarginLayoutParams(
                (1F * resources.displayMetrics.density).roundToInt(),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            dividerLayout.setMargins(
                (5F * resources.displayMetrics.density).roundToInt(),
                0,
                (5F * resources.displayMetrics.density).roundToInt(),
                0
            )
            // @todo Text Color
            divider.setBackgroundColor(Color.parseColor("#C2C2C2"))
            divider.layoutParams = dividerLayout
            addView(divider)

        }

        addView(button)
        buttons.add(button)
    }

    fun setSelectedButton(tag: Any) {
        for (button in buttons) {
            // @todo Color constant
            if (button.tag == tag) {
                button.setTextColor(resources.getColor(R.color.colorPrimary))
            } else {
                button.setTextColor(Color.parseColor("#8B8B8B"))
            }
        }
    }

    private val onButtonClicked = OnClickListener {
        setSelectedButton(it.tag)
        onButtonClickedListener?.invoke(it)
    }
}