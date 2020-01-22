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
import com.msindwan.shoebox.data.entities.SearchFilters
import com.msindwan.shoebox.views.dashboard.components.TransactionFilterPanel


/**
 * Fragment for the dashboard transactions view.
 */
class DashboardTransactionsFragment : Fragment() {

    private var transactionFilters: TransactionFilterPanel? = null
    private var transactionList: TransactionListView? = null
    private var transactionSearch: Button? = null
    private lateinit var dashboardModel: DashboardViewModel

    companion object {
        private const val FILTER_PANEL_TRANSITION_DURATION = 300L
    }

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
        transactionList = view.findViewById(R.id.dashboard_transactions_txns_list)
        transactionSearch = view.findViewById(R.id.dashboard_transactions_btn_search)
        transactionFilters = view.findViewById(R.id.dashboard_transactions_filter_layout)

        val displayMetrics = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels

        transactionFilters?.y = -height.toFloat()
        transactionFilters?.setFragmentManager(fragmentManager)
        ViewCompat.setElevation(transactionFilters!!, 15.0F)

        transactionSearch?.setOnClickListener(onSearchOrFilterClicked)
        transactionList?.setViewMoreClickListener(onViewMoreTransactionsClicked)
        transactionList?.setOnDeleteTransactions { dashboardModel.deleteTransactions(it) }

        dashboardModel = ViewModelProviders.of(activity!!).get(DashboardViewModel::class.java)
        dashboardModel.getSearchTransactions().observe(viewLifecycleOwner, Observer { update() })

        val searchFilters = dashboardModel.getSearchTransactionsFilters()
        transactionFilters?.setFilters(searchFilters)
        transactionFilters?.setOnApplyFiltersClickListener(applyFilters)
    }

    /**
     * Updates the view based on the view model state.
     */
    private fun update() {
        val transactions = dashboardModel.getSearchTransactions()
        transactionList?.setTransactionsList(transactions.value!!)
        transactionList?.showViewMoreButton(transactions.value?.size ?: -1 >= 100)
    }

    /**
     * Toggles the search filters panel display.
     */
    private fun toggleSearchFilters() {
        if (transactionFilters != null) {
            val animator = if (transactionFilters!!.y == 0.0F) {
                ValueAnimator.ofFloat(0.0F, -transactionFilters!!.height.toFloat())
            } else {
                ValueAnimator.ofFloat(-transactionFilters!!.height.toFloat(), 0.0F)
            }

            animator.duration = FILTER_PANEL_TRANSITION_DURATION
            animator.interpolator = DecelerateInterpolator()
            animator.addUpdateListener { animation ->
                transactionFilters!!.y = animation.animatedValue as Float
                transactionFilters!!.requestLayout()
            }
            animator.start()
        }
    }

    /**
     * Callback invoked to apply selected filters to the view.
     */
    private val applyFilters = fun(searchFilters: SearchFilters) {
        dashboardModel.setSearchTransactionsFilters(searchFilters)
        toggleSearchFilters()
    }

    /**
     * Handles clicking on "View More Transactions".
     */
    private val onViewMoreTransactionsClicked = View.OnClickListener {
        val transactionsRead = dashboardModel.nextSearchTransactions(100)
        transactionList?.showViewMoreButton(transactionsRead == 100)
    }

    /**
     * Handles clicking on "Search and Filter".
     */
    private val onSearchOrFilterClicked = View.OnClickListener {
        transactionFilters?.setFilters(dashboardModel.getSearchTransactionsFilters())
        toggleSearchFilters()
    }
}
