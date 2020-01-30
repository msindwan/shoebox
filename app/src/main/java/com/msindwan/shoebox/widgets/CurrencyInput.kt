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
import android.widget.LinearLayout
import com.msindwan.shoebox.R
import android.text.Editable
import android.text.InputType
import android.widget.EditText
import android.text.TextWatcher
import android.text.Selection
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView


/**
 * A TextView control with formatting to ensure that a user inputs a valid monetary value.
 */
class CurrencyInput : LinearLayout {
    var editText: EditText? = null
        private set

    private lateinit var textView: TextView

    constructor(context: Context) : super(context) {
        setup()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        setup()
    }

    override fun setEnabled(enabled: Boolean) {
        alpha = when {
            enabled -> 1.0f
            else -> 0.5f
        }
        editText?.isEnabled = enabled
        super.setEnabled(enabled)
    }

    /**
     * Initializes the view.
     */
    private fun setup() {
        setBackgroundResource(R.drawable.input_background)
        orientation = HORIZONTAL

        val textViewPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            5F,
            resources.displayMetrics
        ).toInt()

        val editTextPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            8F,
            resources.displayMetrics
        ).toInt()

        textView = TextView(context)
        textView.gravity = Gravity.CENTER
        textView.setPadding(textViewPadding, textViewPadding, 0, 0)
        textView.text = resources.getString(R.string.currency_symbol)
        textView.setTextColor(Color.parseColor("#707070"))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)

        editText = EditText(context)
        editText?.layoutParams = LayoutParams(
            0,
            LayoutParams.WRAP_CONTENT,
            1.0F
        )
        editText?.setPadding(editTextPadding, editTextPadding, editTextPadding, editTextPadding)
        editText?.setBackgroundColor(Color.TRANSPARENT)
        editText?.hint = resources.getString(R.string.zero_dollars)
        editText?.addTextChangedListener(currencyEditTextFormatter)
        editText?.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
        editText?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)

        addView(textView)
        addView(editText)
    }

    /**
     * Reads from the input and returns the amount.
     *
     * @returns the input value in cents.
     */
    fun getAmount(): Long {
        val text = editText?.text.toString()

        if (text.isEmpty()) {
            return 0L
        }
        return (text.toDouble() * 100).toLong()
    }

    /**
     * Edits the input text as the user types to display a valid monetary value.
     * Derived from
     * http://www.blog.nathanhaze.com/inserting-currency-in-a-edit-text-field-with-text-watcher-android/
     */
    private var currencyEditTextFormatter: TextWatcher = object : TextWatcher {
        private val currencyRegex: Regex = "^\\$(\\d{1,3}(,\\d{3})*|(\\d+))(\\.\\d{2})?$".toRegex()
        private val userTextRegex: Regex = "[^\\d]".toRegex()

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

        override fun afterTextChanged(s: Editable) {
            val text = s.toString()

            if (!text.matches(currencyRegex)) {
                val userInput = text.replace(userTextRegex, "")
                val updatedText = StringBuilder(userInput)

                while (updatedText.length > 3 && updatedText[0] == '0') {
                    updatedText.deleteCharAt(0)
                }
                while (updatedText.length < 3) {
                    updatedText.insert(0, '0')
                }
                updatedText.insert(updatedText.length - 2, '.')

                editText?.removeTextChangedListener(this)
                editText?.setText(updatedText)
                editText?.setTextKeepState(updatedText)
                Selection.setSelection(editText?.text, updatedText.length)
                editText?.addTextChangedListener(this)
            }
        }
    }
}