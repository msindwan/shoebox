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

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.msindwan.shoebox.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.msindwan.shoebox.helpers.ActivityHelpers
import com.msindwan.shoebox.views.dashboard.components.FooterMenu
import com.msindwan.shoebox.views.dashboard.components.TransactionListView
import com.msindwan.shoebox.views.dashboard.models.DashboardViewModel
import com.msindwan.shoebox.views.transactions.NewTransaction
import com.msindwan.shoebox.widgets.Gauge
import java.text.NumberFormat
import java.util.*


/**
 * Fragment for the dashboard home view
 */
class DashboardHome : Fragment() {

    private lateinit var dashboardModel: DashboardViewModel

    private var gaugeBudget: Gauge? = null
    private var lstTxns: TransactionListView? = null
    private var btnAddTxn: Button? = null
    private var defaultGaugeRemainingTextColor: ColorStateList? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dashboard_home_fragment, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setup(view)
    }

    /**
     * Initializes the view.
     */
    fun setup(view: View) {
        gaugeBudget = view.findViewById(R.id.dashboard_home_gauge_budget)
        defaultGaugeRemainingTextColor = gaugeBudget?.remainingTextColors
        lstTxns = view.findViewById(R.id.dashboard_home_lst_transactions)
        lstTxns?.setViewMoreClickListener(onViewMoreTransactionsClicked)

        btnAddTxn = view.findViewById(R.id.dashboard_home_btn_add_transaction)
        btnAddTxn?.setOnClickListener(onNewTransactionClicked)

        dashboardModel = ViewModelProvider(activity!!).get(DashboardViewModel::class.java)
        dashboardModel.getRecentTransactions().observe(viewLifecycleOwner, Observer { update() })
        dashboardModel.getCurrentBudget().observe(viewLifecycleOwner, Observer { update() })
        dashboardModel.getSumOfRecentTransactions()
            .observe(viewLifecycleOwner, Observer { update() })
    }

    /**
     * Updates the view based on the view model state.
     */
    private fun update() {
        val totalBudget = dashboardModel.getCurrentBudget().value!!.amount / 100F
        val remainingBudget =
            totalBudget - (dashboardModel.getSumOfRecentTransactions().value!! / 100F)
        val budgetPercentage = remainingBudget / totalBudget
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

        gaugeBudget?.remainingText?.text = currencyFormatter.format(remainingBudget)

        gaugeBudget?.setGaugeText(
            currencyFormatter.format(remainingBudget),
            currencyFormatter.format(totalBudget)
        )

        // @todo Make constants out of colors
        if (remainingBudget <= 0) {
            gaugeBudget?.setRemainingTextColor(Color.parseColor("#E28080"))
        } else {
            gaugeBudget?.setRemainingTextColor(defaultGaugeRemainingTextColor)
        }
        when {
            budgetPercentage > 0.5F -> gaugeBudget?.setProgressBarColor(
                Color.parseColor(
                    "#87D6B9"
                )
            )
            budgetPercentage > 0.3F -> gaugeBudget?.setProgressBarColor(
                Color.parseColor(
                    "#EDE575"
                )
            )
            else -> gaugeBudget?.setProgressBarColor(Color.parseColor("#E28080"))
        }

        gaugeBudget?.setPercent(budgetPercentage)

        lstTxns?.showViewMoreButton(dashboardModel.getRecentTransactions().value?.size ?: -1 >= 4)
        lstTxns?.setTransactionsList(dashboardModel.getRecentTransactions().value!!)
        lstTxns?.setOnDeleteTransactions {
            dashboardModel.deleteTransactions(it)
        }
    }

    /**
     * Handles clicking on the "Add Transaction" button.
     */
    private val onNewTransactionClicked = View.OnClickListener {
        activity?.startActivityForResult(
            Intent(activity, NewTransaction::class.java),
            ActivityHelpers.NEW_TRANSACTION_REQUEST_CODE
        )
    }

    /**
     * Handles clicking on the "View More Transactions" button.
     */
    private val onViewMoreTransactionsClicked = View.OnClickListener {
        dashboardModel.setCurrentMenuItem(FooterMenu.MenuItem.TRANSACTIONS)
    }
}
