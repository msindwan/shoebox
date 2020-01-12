package com.msindwan.shoebox.views.dashboard.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.msindwan.shoebox.R
import com.msindwan.shoebox.data.DataAccessLayer
import com.msindwan.shoebox.data.dao.TransactionDAO
import com.msindwan.shoebox.data.entities.Budget
import com.msindwan.shoebox.data.entities.DateRange
import com.msindwan.shoebox.data.entities.Transaction
import com.msindwan.shoebox.data.sqlite.tables.TransactionTable
import com.msindwan.shoebox.helpers.DateHelpers
import com.msindwan.shoebox.views.dashboard.components.TransactionListView

class DashboardTransactionsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dashboard_transactions_fragment, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dal: DataAccessLayer = DataAccessLayer.getInstance(activity!!.applicationContext)
        val currentMonth: DateRange = DateHelpers.getCurrentMonth()
        val transactions: MutableList<Transaction>? = dal.transactionDAO?.getTransactions(
            currentMonth.endDate,
            null,
            TransactionDAO.ORDER_DESC,
            300
        )

        if (transactions?.size ?: -1 > 0) {
            activity!!.findViewById<TransactionListView>(R.id.dashboard_transactions_txns_list)
                .setTransactionsList(transactions!!)
        }
    }
}
