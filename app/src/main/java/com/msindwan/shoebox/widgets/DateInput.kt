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

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.msindwan.shoebox.R
import java.util.*
import java.text.SimpleDateFormat


/**
 * Input with a date picker trigger.
 */
class DateInput : LinearLayout {

    private val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var date: Date = Calendar.getInstance().time
    private var textView: TextView? = null

    /**
     * Date picker dialog fragment.
     *
     * @constructor(date, handleDateSet)
     * @param date {Date} The initial date picker date.
     * @param handleDateSet {(...) -> Unit) Callback fired when the date is set.
     */
    class DatePickerFragment(
        private val date: Date,
        private val handleDateSet: ((view: DatePicker, year: Int, month: Int, day: Int) -> Unit)
    ) : DialogFragment(), DatePickerDialog.OnDateSetListener {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val c = Calendar.getInstance()
            c.time = date

            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            return DatePickerDialog(context!!, this, year, month, day)
        }

        override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
            handleDateSet(view, year, month, day)
        }
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        setup()
    }

    constructor(context: Context) : super(context) {
        setup()
    }

    /**
     * Gets the date picker date.
     *
     * @returns the selected date.
     */
    fun getDate(): Date {
        return date
    }

    /**
     * Initializes the view.
     */
    private fun setup() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.date_input, this, true)
        orientation = HORIZONTAL

        textView = getChildAt(0) as TextView
        textView?.text = formatter.format(date)
        setOnClickListener(onDateInputClicked)
    }

    /**
     * Handles opening the date picker dialog.
     */
    private val onDateInputClicked = OnClickListener {
        val activity = context as AppCompatActivity
        val datePickerFragment =
            DatePickerFragment(date) { _: DatePicker, year: Int, month: Int, day: Int ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day)
                date = calendar.time
                textView?.text = formatter.format(date)
            }
        datePickerFragment.show(activity.supportFragmentManager, "datePicker")
    }
}