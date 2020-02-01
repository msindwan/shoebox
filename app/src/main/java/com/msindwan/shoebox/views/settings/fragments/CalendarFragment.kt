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
package com.msindwan.shoebox.views.settings.fragments

import android.content.Context
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.msindwan.shoebox.R
import android.util.TypedValue
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.msindwan.shoebox.data.entities.LocalDateRange
import com.msindwan.shoebox.views.settings.models.SettingsViewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.text.NumberFormat
import java.util.*


/**
 * Fragment for each calendar view.
 */
class ScheduleCalendarFragment : Fragment() {

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    private val monthCalendarItems: MutableList<MonthCalendarItem> = mutableListOf()
    private val monthFormatter = DateTimeFormatter.ofPattern("MMM")
    private lateinit var settingsModel: SettingsViewModel

    companion object {
        private const val MONTH_CALENDAR_ITEM_PADDING = 10
    }

    /**
     * Calendar item component representing a month.
     */
    private inner class MonthCalendarItem(context: Context?) : LinearLayout(context) {

        var select: CheckBox? = null
        var budget: TextView? = null
        var month: TextView? = null

        init {
            setup()
        }

        /**
         * Initializes the view.
         */
        private fun setup() {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1.0F
            )
            gravity = Gravity.CENTER
            setPadding(
                MONTH_CALENDAR_ITEM_PADDING,
                MONTH_CALENDAR_ITEM_PADDING,
                MONTH_CALENDAR_ITEM_PADDING,
                MONTH_CALENDAR_ITEM_PADDING
            )

            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.budget_schedule_calendar_item, this, true)

            select = findViewById(R.id.budget_schedule_calendar_item_select)
            month = findViewById(R.id.budget_schedule_calendar_item_month)
            budget = findViewById(R.id.budget_schedule_calendar_item_budget)

            val outValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            setBackgroundResource(outValue.resourceId)
        }

        override fun setEnabled(enabled: Boolean) {
            alpha = when {
                enabled -> 1.0f
                else -> 0.5f
            }
            super.setEnabled(enabled)
        }

        override fun setSelected(selected: Boolean) {
            select?.isChecked = selected
            select?.visibility = if (selected) View.VISIBLE else View.GONE
            super.setSelected(selected)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = LinearLayout(context)
        setup(view)
        return view
    }

    /**
     * Initializes the view.
     */
    private fun setup(view: LinearLayout) {
        view.layoutParams = ViewGroup.LayoutParams(
            GridLayout.LayoutParams.MATCH_PARENT,
            GridLayout.LayoutParams.MATCH_PARENT
        )
        view.orientation = LinearLayout.VERTICAL

        val currentYear = LocalDateRange.currentYear()

        // Create month items across N rows
        for (i in 1..4) {
            val row = LinearLayout(context)
            row.orientation = LinearLayout.HORIZONTAL
            row.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.0F
            )

            for (j in 1..3) {
                val month = currentYear.startDate?.plusMonths((j - 1) + (i - 1) * 3L)
                val item = MonthCalendarItem(context)

                item.setOnClickListener { settingsModel.setBudgetScheduleMonth(month!!.monthValue) }
                item.month?.text = monthFormatter.format(month)
                monthCalendarItems.add(item)
                row.addView(item)
            }

            view.addView(row)
        }

        settingsModel = ViewModelProvider(activity!!).get(SettingsViewModel::class.java)
        settingsModel.getBudgetScheduleMonth().observe(viewLifecycleOwner, Observer { update() })
        settingsModel.getMonthlyBudgets().observe(viewLifecycleOwner, Observer { update() })
    }

    /**
     * Updates the view based on the view model state.
     */
    private fun update() {
        val month: Int? = settingsModel.getBudgetScheduleMonth().value
        val year: Int? = settingsModel.getBudgetScheduleYear().value
        val budgets = settingsModel.getMonthlyBudgets().value
        val now = LocalDate.now()

        if (year != null && month != null) {
            for ((i, col) in monthCalendarItems.withIndex()) {
                if (budgets?.getOrNull(i) != null) {
                    col.budget?.text = currencyFormatter.format(budgets[i]!!.amount / 100)
                } else {
                    col.budget?.text = getString(R.string.no_budget)
                }

                val isEnabled = year >= now.year || (year == now.year && (i + 1) >= now.monthValue)
                col.isSelected = isEnabled && i == month - 1
                col.isEnabled = isEnabled
            }
        }
    }
}