package com.msindwan.shoebox.views.dashboard.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.msindwan.shoebox.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.msindwan.shoebox.data.DataAccessLayer
import com.msindwan.shoebox.data.dao.TransactionDAO
import com.msindwan.shoebox.data.entities.Budget
import com.msindwan.shoebox.data.entities.DateRange
import com.msindwan.shoebox.data.entities.Transaction
import com.msindwan.shoebox.helpers.DateHelpers
import com.msindwan.shoebox.views.dashboard.components.TransactionListView
import com.msindwan.shoebox.views.transactions.NewTransaction
import com.msindwan.shoebox.widgets.Gauge
import java.text.NumberFormat
import java.util.*


class DashboardHome : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dashboard_home_fragment, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val emptyTxText =
            activity!!.findViewById<TextView>(R.id.dashboard_home_txt_empty_transactions)
        val moreTxnBtn =
            activity!!.findViewById<Button>(R.id.dashboard_home_btn_view_more_transaction)
        val gauge = activity!!.findViewById<Gauge>(R.id.dashboard_home_gauge_budget)
        val budgetStatusTxt =
            activity!!.findViewById<TextView>(R.id.dashboard_home_txt_budget_status)

        val dal: DataAccessLayer = DataAccessLayer.getInstance(activity!!.applicationContext)
        val currentMonth: DateRange = DateHelpers.getCurrentMonth()
        val budgets: MutableList<Budget>? = dal.budgetDAO?.getBudgets(
            currentMonth.startDate,
            currentMonth.endDate
        )
        val transactions: MutableList<Transaction>? = dal.transactionDAO?.getTransactions(
            currentMonth,
            null,
            TransactionDAO.ORDER_DESC,
            4
        )

        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        val totalBudget = budgets!![0].amount / 100.0
        val remainingBudget =
            totalBudget - (dal.transactionDAO?.getSumOfTransactions(currentMonth) ?: 0) / 100.0

        if (transactions?.size ?: -1 > 0) {
            emptyTxText.visibility = View.GONE

            if (transactions?.size ?: -1 == 4) {
                moreTxnBtn.visibility = View.VISIBLE
            } else {
                moreTxnBtn.visibility = View.GONE
            }

        } else {
            emptyTxText.visibility = View.VISIBLE
            moreTxnBtn.visibility = View.GONE
        }

        gauge.setGaugeText(
            currencyFormatter.format(remainingBudget),
            currencyFormatter.format(totalBudget)
        )

        val budgetPercentage = remainingBudget / totalBudget * 100
        gauge.setPercent(budgetPercentage.toInt())

        if (remainingBudget <= 0) {
            budgetStatusTxt.text = resources.getString(R.string.exceeded_budget)
        } else {
            budgetStatusTxt.text = resources.getString(R.string.within_budget)
        }

        val btnSubmitBudget =
            activity!!.findViewById<Button>(R.id.dashboard_home_btn_add_transaction)
        btnSubmitBudget.setOnClickListener {
            startActivity(Intent(activity, NewTransaction::class.java))
        }

        activity!!.findViewById<TransactionListView>(R.id.lst_txns)
            .setTransactionsList(transactions!!)
    }
}
