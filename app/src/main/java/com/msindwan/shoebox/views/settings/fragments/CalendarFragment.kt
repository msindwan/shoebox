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
import androidx.lifecycle.ViewModelProviders
import com.msindwan.shoebox.views.settings.models.SettingsViewModel
import org.threeten.bp.LocalDate
import java.text.NumberFormat
import java.util.*


val MONTHS = arrayOf(
    "Jan",
    "Feb",
    "Mar",
    "Apr",
    "May",
    "Jun",
    "Jul",
    "Aug",
    "Sep",
    "Oct",
    "Nov",
    "Dec"
)

class ScheduleCalendarFragment: Fragment() {

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    private val monthViews: MutableList<MonthCalendarItem> = mutableListOf()
    private lateinit var settingsModel: SettingsViewModel

    private inner class MonthCalendarItem(context: Context?): LinearLayout(context) {

        var select: CheckBox? = null
        var month: TextView? = null
        var budget: TextView? = null

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
            setPadding(10, 10, 10, 10)

            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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

    private fun setup(view: LinearLayout) {
        view.layoutParams = ViewGroup.LayoutParams(
            GridLayout.LayoutParams.MATCH_PARENT,
            GridLayout.LayoutParams.MATCH_PARENT
        )
        view.orientation = LinearLayout.VERTICAL


        for (i in 1..4) {
            val row = LinearLayout(context)
            row.orientation = LinearLayout.HORIZONTAL
            row.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.0F
            )

            for (j in 1..3) {
                val item = MonthCalendarItem(context)
                item.setOnClickListener{ setSelectedMonth((j - 1) + (i - 1)*3 + 1) }
                item.month?.text = MONTHS[(j - 1) + (i - 1)*3]
                monthViews.add(item)
                row.addView(item)
            }

            view.addView(row)
        }

        settingsModel = ViewModelProviders.of(activity!!).get(SettingsViewModel::class.java)
        settingsModel.getBudgetScheduleMonth().observe(viewLifecycleOwner, Observer { update() })
        settingsModel.getMonthlyBudgets().observe(viewLifecycleOwner, Observer { update() })
    }

    private fun update() {
        val selectedMonth: Int? = settingsModel.getBudgetScheduleMonth().value
        val selectedYear: Int? = settingsModel.getBudgetScheduleYear().value
        val budgets = settingsModel.getMonthlyBudgets().value
        val now = LocalDate.now()

        if (selectedYear != null && selectedMonth != null) {
            for ((index, col) in monthViews.withIndex()) {
                if (budgets?.get(index) != null) {
                    col.budget?.text = currencyFormatter.format(budgets[index]!!.amount / 100)
                } else {
                    col.budget?.text = "No Budget"
                }

                if (selectedYear < now.year || (selectedYear == now.year && selectedMonth < now.monthValue)) {
                    col.select?.visibility = View.GONE
                    col.select?.isChecked = false
                    col.isEnabled  = false
                } else if (index == selectedMonth - 1) {
                    col.select?.visibility = View.VISIBLE
                    col.select?.isChecked = true
                    col.isEnabled  = true
                } else {
                    col.select?.visibility = View.GONE
                    col.select?.isChecked = false
                    col.isEnabled  = true
                }
            }
        }
    }

    private fun setSelectedMonth(month: Int) {
        settingsModel.setBudgetScheduleMonth(month)
    }
}