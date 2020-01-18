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

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import com.msindwan.shoebox.R
import com.msindwan.shoebox.views.dashboard.components.TransactionListView
import com.msindwan.shoebox.views.dashboard.models.DashboardViewModel
import android.util.DisplayMetrics
import android.widget.*
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar
import com.msindwan.shoebox.data.entities.DateRange
import com.msindwan.shoebox.data.entities.SearchFilters
import com.msindwan.shoebox.widgets.CurrencyInput
import com.msindwan.shoebox.widgets.DateInput
import java.lang.NumberFormatException
import java.text.NumberFormat
import java.util.*


/**
 * Fragment for the dashboard transactions view.
 */
class DashboardTransactionsFragment : Fragment() {

    private var dashboardTransactionsLstTxns: TransactionListView? = null
    private var dashboardTransactionsBtnSearch: Button? = null
    private var dashboardTransactionsFilters: LinearLayout? = null
    private var dashboardTransactionsFiltersTxtTitle: EditText? = null
    private var dashboardTransactionsFilterBtnApply: Button? = null
    private var dashboardTransactionsFilterAmount: CrystalRangeSeekbar? = null
    private var dashboardTransactionsFilterFrom: DateInput? = null
    private var dashboardTransactionsFilterTo: DateInput? = null
    private var dashboardTransactionsFilterCount: TextView? = null
    private var dashboardTransactionsFilterNoStartDate: TextView? = null
    private var dashboardTransactionsFilterNoEndDate: TextView? = null
    private var dashboardTransactionsFilterCategory: AutoCompleteTextView? = null
    private var dashboardTransactionsFilterReset: LinearLayout? = null

    private lateinit var dashboardModel: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dashboard_transactions_fragment, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setup(view)
    }

    /**
     * Initializes the view.
     */
    private fun setup(view: View) {
        dashboardTransactionsLstTxns = view.findViewById(R.id.dashboard_transactions_txns_list)
        dashboardTransactionsBtnSearch = view.findViewById(R.id.dashboard_transactions_btn_search)
        dashboardTransactionsFilters = view.findViewById(R.id.dashboard_transactions_filter_layout)
        dashboardTransactionsFilterBtnApply =
            view.findViewById(R.id.dashboard_transactions_btn_apply_filters)
        dashboardTransactionsFilterAmount =
            view.findViewById(R.id.dashboard_transactions_amount_slider)
        dashboardTransactionsFilterFrom = view.findViewById(R.id.dashboard_transactions_txt_from)
        dashboardTransactionsFilterTo = view.findViewById(R.id.dashboard_transactions_txt_to)
        dashboardTransactionsFilterNoStartDate =
            view.findViewById(R.id.dashboard_transactions_filter_no_start_date)
        dashboardTransactionsFilterNoEndDate =
            view.findViewById(R.id.dashboard_transactions_filter_no_end_date)
        dashboardTransactionsFilterCategory =
            view.findViewById(R.id.dashboard_transactions_txt_category)
        dashboardTransactionsFiltersTxtTitle =
            view.findViewById(R.id.dashboard_transactions_txt_title)
        dashboardTransactionsFilterCount =
            view.findViewById(R.id.dashboard_transactions_filters_count)

        dashboardTransactionsFilterReset =
            view.findViewById(R.id.dashboard_transaction_filters_btn_reset)
        dashboardTransactionsFilterReset?.setOnClickListener(onResetFiltersClicked)

        dashboardTransactionsFilterNoStartDate?.setOnClickListener(onNoStartDateClicked)
        dashboardTransactionsFilterNoEndDate?.setOnClickListener(onNoEndDateClicked)

        dashboardTransactionsFilterAmount?.setMaxValue(200000.0F)
        dashboardTransactionsFilterAmount?.setMinValue(0.0F)

        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

        dashboardTransactionsFilterAmount?.setOnRangeSeekbarChangeListener(
            OnRangeSeekbarChangeListener(
                fun (minValue: Number, maxValue: Number) {
                    view.findViewById<CurrencyInput>(R.id.dashboard_transactions_amount_min).setText(
                        minValue.toString()
                    )

                    view.findViewById<CurrencyInput>(R.id.dashboard_transactions_amount_max).setText(
                        maxValue.toString()
                    )
                }
            )
        )

        dashboardTransactionsFilterTo?.setDate(null)
        dashboardTransactionsFilterFrom?.setDate(null)

        val displayMetrics = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels

        dashboardTransactionsFilters?.y = -height.toFloat()
        dashboardTransactionsFilters?.isEnabled = false
        dashboardTransactionsFilters?.isFocusable = false

        ViewCompat.setElevation(dashboardTransactionsFilters!!, 15.0F)

        dashboardTransactionsFilterBtnApply?.setOnClickListener(onApplyFilters)
        dashboardTransactionsBtnSearch?.setOnClickListener(onSearchOrFilterClicked)

        dashboardTransactionsLstTxns?.setViewMoreClickListener(onViewMoreTransactionsClicked)
        dashboardTransactionsLstTxns?.setOnDeleteTransactions { dashboardModel.deleteTransactions(it) }

        dashboardTransactionsFilterTo?.fragmentManager = fragmentManager
        dashboardTransactionsFilterFrom?.fragmentManager = fragmentManager

        dashboardModel = ViewModelProviders.of(activity!!).get(DashboardViewModel::class.java)
        dashboardModel.getSearchTransactions().observe(viewLifecycleOwner, Observer { update() })

        val searchFilters = dashboardModel.getSearchTransactionsFilters()
        dashboardTransactionsFiltersTxtTitle?.setText(searchFilters.title)
        dashboardTransactionsFilterCategory?.setText(searchFilters.category)

        val startDate = if (searchFilters.dateRange.startDate == DateRange.NO_START_DATE) {
            null
        } else {
            Date(searchFilters.dateRange.startDate)
        }

        val endDate = if (searchFilters.dateRange.endDate == DateRange.NO_START_DATE) {
            null
        } else {
            Date(searchFilters.dateRange.endDate)
        }

        dashboardTransactionsFilterFrom?.setDate(startDate)
        dashboardTransactionsFilterTo?.setDate(endDate)
    }

    private fun foo(min: Number, max: Number) {

    }

    /**
     * Updates the view based on the view model state.
     */
    private fun update() {
        val transactions = dashboardModel.getSearchTransactions()
        dashboardTransactionsLstTxns?.setTransactionsList(transactions.value!!)
        dashboardTransactionsLstTxns?.showViewMoreButton(transactions.value?.size ?: -1 >= 100)
    }

    private fun toggleSearchFilters() {
        val animator: ValueAnimator
        if (dashboardTransactionsFilters?.y == 0.0F) {
            animator = ValueAnimator.ofFloat(0.0F, -dashboardTransactionsFilters!!.height.toFloat())
            dashboardTransactionsFilters?.isClickable = false
            dashboardTransactionsFilters?.isFocusable = false
        } else {
            animator = ValueAnimator.ofFloat(-dashboardTransactionsFilters!!.height.toFloat(), 0.0F)
            dashboardTransactionsFilters?.isClickable = true
            dashboardTransactionsFilters?.isFocusable = true
        }

        animator.duration = 300
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            dashboardTransactionsFilters!!.y = animation.animatedValue as Float
            dashboardTransactionsFilters!!.requestLayout()
        }
        animator.start()
    }

    private fun applyFilters() {
        var filterCount = 0

        val title: String?
        val category: String?

        if (dashboardTransactionsFiltersTxtTitle?.text?.isEmpty() == false) {
            title = dashboardTransactionsFiltersTxtTitle!!.text!!.toString()
            filterCount++
        } else {
            title = null
        }

        var from = dashboardTransactionsFilterFrom?.getDate()?.time ?: DateRange.NO_START_DATE
        var to = dashboardTransactionsFilterTo?.getDate()?.time ?: DateRange.NO_END_DATE

        if (from != DateRange.NO_START_DATE) from /= 1000
        if (to != DateRange.NO_END_DATE) to /= 1000

        if (from != DateRange.NO_START_DATE || to != DateRange.NO_END_DATE) {
            filterCount++
        }

        if (dashboardTransactionsFilterCategory?.text?.isEmpty() == false) {
            category = dashboardTransactionsFilterCategory!!.text!!.toString()
            filterCount++
        } else {
            category = null
        }

        dashboardTransactionsFilterCount?.text = if (filterCount == 0) {
            "No Filters Applied"
        } else {
            "%d Filters Applied".format(filterCount)
        }

        toggleSearchFilters()
        dashboardModel.setSearchTransactionsFilters(SearchFilters(title, DateRange(from, to), category))
    }

    /**
     * Handles clicking on "View More Transactions".
     */
    private val onViewMoreTransactionsClicked = View.OnClickListener {
        val transactionsRead = dashboardModel.nextSearchTransactions(100)
        dashboardTransactionsLstTxns?.showViewMoreButton(transactionsRead == 100)
    }

    private val onSearchOrFilterClicked = View.OnClickListener {
        toggleSearchFilters()
    }

    private val onApplyFilters = View.OnClickListener {
        applyFilters()
    }

    private val onNoStartDateClicked = View.OnClickListener {
        dashboardTransactionsFilterFrom?.setDate(null)
    }

    private val onNoEndDateClicked = View.OnClickListener {
        dashboardTransactionsFilterTo?.setDate(null)
    }

    private val onResetFiltersClicked = View.OnClickListener {
        dashboardTransactionsFiltersTxtTitle?.text?.clear()
        dashboardTransactionsFilterCategory?.text?.clear()
        dashboardTransactionsFilterFrom?.setDate(null)
        dashboardTransactionsFilterTo?.setDate(null)
        applyFilters()
    }
}
