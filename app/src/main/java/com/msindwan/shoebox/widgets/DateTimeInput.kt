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
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.msindwan.shoebox.R
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter


/**
 * Input with a date picker trigger.
 */
class DateTimeInput : LinearLayout {

    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy h:mma")
    private lateinit var editText: EditText

    var fragmentManager: FragmentManager? = null
    var date: LocalDateTime? = LocalDateTime.now()
        set(newDate) {
            editText.setText(if (newDate == null) "" else formatter.format(newDate))
            field = newDate
        }

    /**
     * Date picker dialog fragment.
     */
    class DatePickerFragment(
        val date: LocalDateTime?,
        val handleDataSet: ((year: Int, month: Int, days: Int) -> Unit)
    ) : DialogFragment(), DatePickerDialog.OnDateSetListener {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val selectedDate = date ?: LocalDateTime.now()
            return DatePickerDialog(
                context!!,
                this,
                selectedDate.year,
                selectedDate.monthValue - 1,
                selectedDate.dayOfMonth
            )
        }

        override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
            handleDataSet(year, month + 1, day)
        }
    }

    /**
     * Time picker dialog fragment.
     */
    class TimePickerFragment(
        val date: LocalDateTime?,
        val handleDataSet: ((hourOfDay: Int, minute: Int) -> Unit)
    ) : DialogFragment(), TimePickerDialog.OnTimeSetListener {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val selectedDate = date ?: LocalDateTime.now()
            return TimePickerDialog(
                context!!,
                this,
                selectedDate.hour,
                selectedDate.minute,
                false
            )
        }

        override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
            handleDataSet(hourOfDay, minute)
        }
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        setup()
    }

    constructor(context: Context) : super(context) {
        setup()
    }

    /**
     * Initializes the view.
     */
    private fun setup() {
        orientation = HORIZONTAL

        val imageView = ImageView(context)

        // @todo: Scale the width to the height of the text input (possibly with constraint layout)
        imageView.layoutParams = LayoutParams(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                20F,
                context.resources.displayMetrics
            ).toInt(),
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        imageView.adjustViewBounds = true
        imageView.contentDescription = resources.getString(R.string.date)
        imageView.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_calendar,
                null
            )
        )

        editText = EditText(context)
        editText.isClickable = false
        editText.setOnClickListener(onDateInputClicked)
        editText.maxLines = 1
        editText.inputType = InputType.TYPE_CLASS_TEXT
        editText.isFocusable = false
        editText.clearFocus()
        editText.setBackgroundResource(0)
        editText.layoutParams = LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            1.0F
        )
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)

        if (date != null) {
            editText.setText(formatter.format(date!!))
        }

        addView(editText)
        addView(imageView)
        setOnClickListener(onDateInputClicked)
    }

    /**
     * Handles opening the date picker dialog.
     */
    private val onDateInputClicked = OnClickListener {
        if (fragmentManager != null) {
            DatePickerFragment(date) { year: Int, month: Int, day: Int ->
                TimePickerFragment(date) { hour, minute ->
                    date = LocalDateTime.of(year, month, day, hour, minute)
                }.show(fragmentManager!!, "TimeInput")
            }.show(fragmentManager!!, "DateInput")
        }
    }
}