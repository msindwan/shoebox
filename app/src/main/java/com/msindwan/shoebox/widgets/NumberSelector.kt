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

import android.app.Dialog
import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import com.msindwan.shoebox.R
import org.threeten.bp.LocalDate


/**
 * Year selector component.
 */
class NumberSelector : TextView {
    var value = LocalDate.now().year
        set(value) {
            text = value.toString()
            field = value
        }
    var maxValue: Int = value + 100
    var minValue: Int = (value - 100).coerceAtLeast(0)
        set(value) {
            field = value.coerceAtLeast(0)
        }

    var onYearSelected: ((year: Int) -> Unit)? = null
    var title: String? = null

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
        setOnClickListener(onSelectorClick)
        text = value.toString()
    }

    /**
     * Handles clicking on the selector.
     */
    private val onSelectorClick = OnClickListener {
        // Initialize the dialog to select a year.
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.number_selector_dialog)
        val picker = dialog.findViewById<NumberPicker>(R.id.number_selector_dialog_number_picker)
        val btnOk = dialog.findViewById<Button>(R.id.number_selector_dialog_btn_ok)
        val btnCancel = dialog.findViewById<Button>(R.id.number_selector_dialog_btn_cancel)
        val txtTitle = dialog.findViewById<TextView>(R.id.number_selector_dialog_txt_title)

        txtTitle.text = title

        btnOk.setOnClickListener {
            text = picker.value.toString()
            onYearSelected?.invoke(picker.value)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        // Initialize the year picker to the existing value constraints.
        picker.maxValue = maxValue
        picker.minValue = minValue
        picker.wrapSelectorWheel = false
        picker.value = value
        dialog.show()
    }
}