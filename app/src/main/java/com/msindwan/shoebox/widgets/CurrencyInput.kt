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
import android.widget.LinearLayout
import com.msindwan.shoebox.R
import android.text.Editable
import android.widget.EditText
import android.text.TextWatcher
import android.text.Selection


/**
 * A TextView control with formatting to ensure that a user inputs a valid monetary value.
 */
class CurrencyInput : LinearLayout {
    private var editText: EditText? = null

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
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.currency_input, this, true)

        setBackgroundResource(R.drawable.input_background)
        orientation = HORIZONTAL

        editText = getChildAt(1) as EditText
        editText?.addTextChangedListener(currencyEditTextFormatter)
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
     * Sets an error message for the input.
     *
     * @param error {String} The error string to set.
     */
    fun setError(error: String) {
        editText?.error = error
    }

    /**
     * Sets the text for the input.
     *
     * @param text {String} The text to set.
     */
    fun setText(text: String) {
        editText?.setText(text)
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