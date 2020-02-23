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

package com.msindwan.shoebox.views.dashboard.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.msindwan.shoebox.R
import com.msindwan.shoebox.data.entities.OffsetDateTimeRange
import com.msindwan.shoebox.views.dashboard.models.DashboardViewModel
import com.msindwan.shoebox.views.dashboard.components.BudgetGraph
import com.msindwan.shoebox.widgets.ButtonToggleGroup
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import java.text.NumberFormat
import java.util.*


/**
 * Fragment for the dashboard trends view.
 */
class DashboardTrendsFragment : Fragment() {

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    private var selectedView: ButtonToggleGroup? = null
    private var remainingBudgetTxt: TextView? = null
    private var dateRangeTxt: TextView? = null
    private var budgetGraph: BudgetGraph? = null

    private lateinit var dashboardModel: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dashboard_trends_fragment, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setup(view)
    }

    /**
     * Initializes the view.
     */
    private fun setup(view: View) {
        dashboardModel = ViewModelProvider(activity!!).get(DashboardViewModel::class.java)
        budgetGraph = view.findViewById(R.id.budget_graph)
        remainingBudgetTxt = view.findViewById(R.id.dashboard_trends_remaining_budget)
        dateRangeTxt = view.findViewById(R.id.dashboard_trends_range)
        selectedView = view.findViewById(R.id.dashboard_trends_view)

        selectedView?.addButton("Monthly", "Monthly")
        selectedView?.addButton("Yearly", "Yearly")
        selectedView?.onButtonClickedListener = onDateRangeChanged

        dashboardModel.getTrendsBudgetGraph().observe(viewLifecycleOwner, Observer { update() })
        dashboardModel.getTrendsDateRange().observe(viewLifecycleOwner, Observer { update() })
    }

    /**
     * Updates the view based on the view model state.
     */
    private fun update() {
        val monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

        budgetGraph?.data = dashboardModel.getTrendsBudgetGraph().value
        val totalBudget =
            dashboardModel.getTrendsBudgetGraph().value!!.sumBy { it.budget.toInt() } / 100F
        val remainingBudget =
            totalBudget - dashboardModel.getTrendsBudgetGraph().value!!.sumBy { it.amount.toInt() } / 100F
        val dateRange = dashboardModel.getTrendsDateRange().value!!

        if (dateRange.years > 1) {
            selectedView?.setSelectedButton("Yearly")
        } else {
            selectedView?.setSelectedButton("Monthly")
        }

        if (remainingBudget < 0) {
            remainingBudgetTxt?.text = resources.getString(
                R.string.n_over_budget,
                currencyFormatter.format(remainingBudget)
            )
            remainingBudgetTxt?.setTextColor(Color.parseColor("#F0695A"))
        } else {
            remainingBudgetTxt?.text = resources.getString(
                R.string.n_budget_remaining,
                currencyFormatter.format(remainingBudget)
            )
            remainingBudgetTxt?.setTextColor(Color.parseColor("#3CC28D"))
        }

        dateRangeTxt?.text = resources.getString(
            R.string.from_date_range,
            monthFormatter.format(dateRange.startDate),
            monthFormatter.format(dateRange.endDate)
        )
    }

    /**
     * Handles changing the date range for trends.
     */
    private val onDateRangeChanged = fun(view: View) {
        val dateRange = if (view.tag == "Monthly") {
            OffsetDateTimeRange.currentMonth().minusStart(7, ChronoUnit.MONTHS)
        } else {
            OffsetDateTimeRange.currentYear().minusStart(7, ChronoUnit.YEARS)
        }
        dashboardModel.setTrendsDateRange(dateRange)
    }
}
