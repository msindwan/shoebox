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
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.msindwan.shoebox.R
import java.util.*
import java.text.SimpleDateFormat


/**
 * Input with a date picker trigger.
 */
class DateInput : LinearLayout {

    var fragmentManager: FragmentManager? = null

    private val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var textView: TextView? = null
    private var date: Date? = Calendar.getInstance().time

    /**
     * Date picker dialog fragment.
     *
     * @constructor(date, handleDateSet)
     * @param date {Date} The initial date.
     * @param handleDateSet {(...) -> Unit) Callback fired when the date is set.
     */
    class DatePickerFragment(
        private val date: Date?,
        private val handleDateSet: ((view: DatePicker, year: Int, month: Int, day: Int) -> Unit)
    ) : DialogFragment(), DatePickerDialog.OnDateSetListener {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val c = Calendar.getInstance()
            c.time = date ?: Calendar.getInstance().time

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
     * Getter for the input date.
     *
     * @returns the selected date
     */
    fun getDate(): Date? {
        return date
    }

    /**
     * Sets the current date for the input.
     *
     * @param newDate {Date} The date to set.
     */
    fun setDate(newDate: Date?) {
        if (newDate == null) {
            textView?.text = ""
        } else {
            textView?.text = formatter.format(newDate)
        }
        date = newDate
    }

    /**
     * Initializes the view.
     */
    private fun setup() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.date_input, this, true)
        orientation = HORIZONTAL

        textView = getChildAt(0) as TextView

        if (date != null) {
            textView?.text = formatter.format(date!!)
        }
        setOnClickListener(onDateInputClicked)
    }

    /**
     * Handles opening the date picker dialog.
     */
    private val onDateInputClicked = OnClickListener {
        if (fragmentManager != null) {
            val datePickerFragment =
                DatePickerFragment(date) { _: DatePicker, year: Int, month: Int, day: Int ->
                    val calendar = Calendar.getInstance()
                    calendar.set(year, month, day)
                    date = calendar.time
                    textView?.text = formatter.format(calendar.time)
                }
            datePickerFragment.show(fragmentManager!!, "datePicker")
        }
    }
}