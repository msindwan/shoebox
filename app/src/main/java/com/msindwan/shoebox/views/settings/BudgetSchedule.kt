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
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.msindwan.shoebox.R
import com.msindwan.shoebox.data.entities.Interval
import com.msindwan.shoebox.helpers.ActivityHelpers
import com.msindwan.shoebox.views.settings.fragments.ScheduleCalendarFragment
import com.msindwan.shoebox.views.settings.models.SettingsViewModel
import com.msindwan.shoebox.widgets.CurrencyInput
import org.threeten.bp.LocalDate
import com.msindwan.shoebox.widgets.YearSelector

/**
 * Activity for budget schedule settings.
 */
class BudgetSchedule : AppCompatActivity() {

    private var calendarViewPager: ViewPager2? = null
    private var currentMonth: TextView? = null
    private var calendarYear: YearSelector? = null
    private var currentBudget: CurrencyInput? = null
    private var updateBudget: Button? = null
    private var repeatBudget: Spinner? = null

    private var text = mapOf(
        Pair("Never", Interval.N),
        Pair("Every Month", Interval.M),
        Pair("Every Year", Interval.Y)
    )

    private lateinit var settingsModel: SettingsViewModel

    private inner class CalendarPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int {
            return 200
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

        settingsModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)
        settingsModel.getBudgetScheduleYear().observe(this, Observer { update() })
        settingsModel.getBudgetScheduleMonth().observe(this, Observer { update() })
        settingsModel.getMonthlyBudgets().observe(this, Observer { update() })

        updateBudget = findViewById(R.id.budget_schedule_update)
        updateBudget?.setOnClickListener(onBudgetUpdated)

        repeatBudget = findViewById(R.id.budget_schedule_repeat_budget)

        calendarYear = findViewById(R.id.budget_schedule_year)
        calendarYear?.setOnYearSelectedListener(onYearSelected)

        currentMonth = findViewById(R.id.budget_schedule_current_month)
        currentMonth?.setOnClickListener(onCurrentMonthSelected)

        currentBudget = findViewById(R.id.budget_schedule_edit_budget)

        calendarViewPager = findViewById(R.id.budget_schedule_calendar_pager)
        calendarViewPager?.adapter =
            CalendarPagerAdapter(supportFragmentManager, lifecycle)
        calendarViewPager?.registerOnPageChangeCallback(onCalendarViewPaged)
        calendarViewPager?.setCurrentItem(100, false)
    }

    private fun update() {
        val year = settingsModel.getBudgetScheduleYear().value ?: LocalDate.now().year
        val month = settingsModel.getBudgetScheduleMonth().value ?: LocalDate.now().monthValue
        val budgets = settingsModel.getMonthlyBudgets().value
        val now = LocalDate.now()

        calendarYear?.value = year
        calendarYear?.maxValue = year + 100
        calendarYear?.minValue = (year - 100).coerceAtLeast(0)

        val budget = budgets?.get(month - 1)

        if (budget != null) {
            currentBudget?.setText(budget.amount.toString())
        } else {
            currentBudget?.setText("")
        }

        if (year < now.year || month < now.monthValue) {
            updateBudget?.isClickable = false
            updateBudget?.isEnabled = false
            currentBudget?.isEnabled = false
            repeatBudget?.isEnabled = false
            currentBudget?.setText("")
        } else {
            currentBudget?.isEnabled = true
            repeatBudget?.isEnabled = true
            updateBudget?.isClickable = true
            updateBudget?.isEnabled = true // if either have changed
        }
    }

    private val onYearSelected = fun (year: Int) {
        calendarViewPager?.setCurrentItem(100 + (year - LocalDate.now().year), false)
    }

    private val onCurrentMonthSelected = View.OnClickListener {
        if (calendarViewPager?.currentItem == 100) {
            val now = LocalDate.now()
            settingsModel.setBudgetScheduleMonth(now.monthValue)
        } else {
            calendarViewPager?.setCurrentItem(100, false)
        }
    }

    private val onCalendarViewPaged = object: ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            val now = LocalDate.now()
            settingsModel.setBudgetScheduleYear(now.year + position - 100)
            // @todo: find the first active month, else null
            settingsModel.setBudgetScheduleMonth(now.monthValue)
        }
    }

    private val onBudgetUpdated = View.OnClickListener {
        val year = settingsModel.getBudgetScheduleYear().value ?: LocalDate.now().year
        val month = settingsModel.getBudgetScheduleMonth().value ?: LocalDate.now().monthValue

        val updatedBudget = currentBudget?.getAmount()
        val updatedInterval = text[repeatBudget?.selectedItem] ?: Interval.N

        settingsModel.insertOrUpdateBudget(month, year, updatedBudget!!, updatedInterval)
        setResult(ActivityHelpers.BUDGET_SCHEDULE_SUCCESS_RESPONSE_CODE)
        finish()
    }
}
