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
package com.msindwan.shoebox.views.settings

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.msindwan.shoebox.R
import com.msindwan.shoebox.data.entities.Interval
import com.msindwan.shoebox.helpers.ActivityHelpers
import com.msindwan.shoebox.views.settings.fragments.ScheduleCalendarFragment
import com.msindwan.shoebox.views.settings.models.SettingsViewModel
import com.msindwan.shoebox.widgets.CurrencyInput
import org.threeten.bp.LocalDate
import com.msindwan.shoebox.widgets.NumberSelector


/**
 * Activity for budget schedule settings.
 */
class BudgetSchedule : AppCompatActivity() {
    private var ciCurrentBudget: CurrencyInput? = null
    private var ysCalendarYear: NumberSelector? = null
    private var txtCurrentMonth: TextView? = null
    private var spnRepeatBudget: Spinner? = null
    private var btnUpdateBudget: Button? = null
    private var vpCalendar: ViewPager2? = null

    private var intervalTextMap: LinkedHashMap<String, Interval> = linkedMapOf(
        Pair("Never", Interval.N),
        Pair("Every Month", Interval.M),
        Pair("Every Year", Interval.Y)
    )

    private lateinit var settingsModel: SettingsViewModel

    companion object {
        private const val NUM_PAGES = 101
    }

    /**
     * Pager class for calendar views.
     */
    private inner class CalendarPagerAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int {
            return NUM_PAGES
        }

        override fun createFragment(position: Int): Fragment {
            return ScheduleCalendarFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.budget_schedule)
        setup()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Initializes the view.
     */
    private fun setup() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.budget_schedule)

        settingsModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        settingsModel.getBudgetScheduleYear().observe(this, Observer { update() })
        settingsModel.getBudgetScheduleMonth().observe(this, Observer { update() })
        settingsModel.getMonthlyBudgets().observe(this, Observer { update() })

        btnUpdateBudget = findViewById(R.id.budget_schedule_update)
        btnUpdateBudget?.setOnClickListener(onBudgetUpdated)

        val intervalAdapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            intervalTextMap.keys.toTypedArray()
        )
        intervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnRepeatBudget = findViewById(R.id.budget_schedule_repeat_budget)
        spnRepeatBudget?.adapter = intervalAdapter

        ysCalendarYear = findViewById(R.id.budget_schedule_year)
        ysCalendarYear?.onYearSelected = onYearSelected
        ysCalendarYear?.title = resources.getString(R.string.year)

        txtCurrentMonth = findViewById(R.id.budget_schedule_current_month)
        txtCurrentMonth?.setOnClickListener(onCurrentMonthSelected)

        ciCurrentBudget = findViewById(R.id.budget_schedule_edit_budget)

        vpCalendar = findViewById(R.id.budget_schedule_calendar_pager)
        vpCalendar?.adapter = CalendarPagerAdapter(supportFragmentManager, lifecycle)
        vpCalendar?.registerOnPageChangeCallback(onCalendarViewPaged)
        vpCalendar?.setCurrentItem(NUM_PAGES / 2, false)
    }

    /**
     * Updates the view based on the view model state.
     */
    private fun update() {
        val now = LocalDate.now()
        val month = settingsModel.getBudgetScheduleMonth().value ?: now.monthValue
        val year = settingsModel.getBudgetScheduleYear().value ?: now.year
        val budgets = settingsModel.getMonthlyBudgets().value

        ysCalendarYear?.value = year
        ysCalendarYear?.maxValue = now.year + NUM_PAGES/2
        ysCalendarYear?.minValue = now.year - NUM_PAGES/2

        val budget = budgets?.get(month - 1)
        if (budget != null) {
            ciCurrentBudget?.editText?.setText(budget.amount.toString())
        } else {
            ciCurrentBudget?.editText?.text?.clear()
        }

        if (year < now.year || (year == now.year && month < now.monthValue)) {
            btnUpdateBudget?.isEnabled = false
            ciCurrentBudget?.isEnabled = false
            spnRepeatBudget?.isEnabled = false
            ciCurrentBudget?.editText?.text?.clear()
        } else {
            ciCurrentBudget?.isEnabled = true
            spnRepeatBudget?.isEnabled = true
            btnUpdateBudget?.isEnabled = true
        }
    }

    /**
     * Handler for the year selector.
     */
    private val onYearSelected = fun(year: Int) {
        vpCalendar?.setCurrentItem(
            NUM_PAGES / 2 + (year - LocalDate.now().year),
            false
        )
    }

    /**
     * Handler for selecting the current month.
     */
    private val onCurrentMonthSelected = View.OnClickListener {
        val year = settingsModel.getBudgetScheduleYear().value!!
        val now = LocalDate.now()

        if (year == now.year) {
            settingsModel.setBudgetScheduleMonth(now.monthValue)
        } else {
            vpCalendar?.setCurrentItem(NUM_PAGES / 2, false)
        }
    }

    /**
     * Handler for the calendar view pager.
     */
    private val onCalendarViewPaged = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            val now = LocalDate.now()
            val selectedYear = now.plusYears((position - NUM_PAGES / 2).toLong())

            settingsModel.setBudgetScheduleYear(selectedYear.year)
            if (selectedYear.year == now.year) {
                settingsModel.setBudgetScheduleMonth(now.monthValue)
            } else {
                settingsModel.setBudgetScheduleMonth(1)
            }
        }
    }

    /**
     * Handler for when the budget is updated.
     */
    private val onBudgetUpdated = View.OnClickListener {
        val year = settingsModel.getBudgetScheduleYear().value ?: LocalDate.now().year
        val month = settingsModel.getBudgetScheduleMonth().value ?: LocalDate.now().monthValue
        val updatedBudget = ciCurrentBudget?.getAmount()
        val updatedInterval = intervalTextMap[spnRepeatBudget?.selectedItem] ?: Interval.N

        val intent = Intent()
        intent.putExtra("month", month)
        intent.putExtra("year", year)
        intent.putExtra("budget", updatedBudget)
        intent.putExtra("interval", updatedInterval)

        setResult(ActivityHelpers.BUDGET_SCHEDULE_SUCCESS_RESPONSE_CODE, intent)
        finish()
    }
}
