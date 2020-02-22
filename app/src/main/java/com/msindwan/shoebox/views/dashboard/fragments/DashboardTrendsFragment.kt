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
import androidx.lifecycle.ViewModelProviders
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
    private val monthF = DateTimeFormatter.ofPattern("MMM yyyy")
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

    private fun setup(view: View) {
        dashboardModel = ViewModelProviders.of(activity!!).get(DashboardViewModel::class.java)
        budgetGraph = view.findViewById(R.id.budget_graph)
        remainingBudgetTxt = view.findViewById(R.id.dashboard_trends_remaining_budget)
        dateRangeTxt = view.findViewById(R.id.dashboard_trends_range)
        selectedView = view.findViewById(R.id.dashboard_trends_view)

        selectedView?.addButton("Monthly", "Monthly")
        selectedView?.addButton("Yearly", "Yearly")
        selectedView?.onButtonClickedListener = onDateRangeChanged

        dashboardModel.getBudgetGraph().observe(viewLifecycleOwner, Observer { update() })
        dashboardModel.getTrendsDateRange().observe(viewLifecycleOwner, Observer { update() })
    }

    private fun update() {
        budgetGraph?.data = dashboardModel.getBudgetGraph().value
        val totalBudget = dashboardModel.getBudgetGraph().value!!.sumBy { it.budget.toInt() } / 100F
        val remainingBudget =
            totalBudget - dashboardModel.getBudgetGraph().value!!.sumBy { it.amount.toInt() } / 100F
        val dateRange = dashboardModel.getTrendsDateRange().value!!

        if (dateRange.years > 1) {
            selectedView?.setSelectedButton("Yearly")
        } else {
            selectedView?.setSelectedButton("Monthly")
        }

        if (remainingBudget < 0) {
            remainingBudgetTxt?.text = "${currencyFormatter.format(remainingBudget)} over budget"
            remainingBudgetTxt?.setTextColor(Color.parseColor("#F0695A"))
        } else {
            remainingBudgetTxt?.text = "${currencyFormatter.format(remainingBudget)} remaining"
            remainingBudgetTxt?.setTextColor(Color.parseColor("#3CC28D"))
        }

        dateRangeTxt?.text =
            "From ${monthF.format(dateRange.startDate)} - ${monthF.format(dateRange.endDate)}"
    }

    private val onDateRangeChanged = fun(view: View) {
        val dateRange = if (view.tag == "Monthly") {
            OffsetDateTimeRange.currentYear().minusEnd(4, ChronoUnit.MONTHS)
        } else {
            OffsetDateTimeRange.currentYear().plusEnd(7, ChronoUnit.YEARS)
        }
        dashboardModel.setTrendsDateRange(dateRange)
    }
}
