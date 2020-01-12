package com.msindwan.shoebox.views.dashboard.components

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.msindwan.shoebox.data.entities.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransactionListView: RecyclerView {
    class MyAdapter(private val myDataset: MutableList<Transaction>) :
        RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

        private val dateGroups: MutableMap<String, Int> = mutableMapOf()
        private val monthGroups: MutableMap<String, Int> = mutableMapOf()

        init {
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val monthF  = SimpleDateFormat("MMMM, yyyy", Locale.getDefault())
            var date = ""
            var month = ""

            for ((index, transaction) in myDataset.withIndex()) {
                val nextDate = sdf.format(Date(transaction.date * 1000))
                val nextMonth = monthF.format(Date(transaction.date * 1000))
                if (date != nextDate) {
                    dateGroups[nextDate] = index
                    date = nextDate
                }

                if (nextMonth != month) {
                    monthGroups[nextMonth] = index
                    month = nextMonth
                }
            }
        }

        class MyViewHolder(val textView: TransactionListItem) : RecyclerView.ViewHolder(textView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAdapter.MyViewHolder {
            return MyViewHolder(TransactionListItem(parent.context))
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val transaction = myDataset[position]
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val monthF  = SimpleDateFormat("MMMM, yyyy", Locale.getDefault())

            val formattedDate = sdf.format(Date(transaction.date * 1000))
            val formattedMonth = monthF.format(Date(transaction.date * 1000))
            val dateHeaderPos = dateGroups[formattedDate]
            if (dateHeaderPos == position) {
                holder.textView.setHeader(formattedDate)
            }

            val monthHeaderPos = monthGroups[formattedMonth]
            if (monthHeaderPos == position) {
                holder.textView.setPrimaryHeader(formattedMonth)
            }

            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
            holder.textView.setAmount(currencyFormatter.format(transaction.amount / 100))
            holder.textView.setDate(formattedDate)
            holder.textView.setTitle(transaction.title ?: "No Title")
            holder.textView.setType(transaction.type)
        }

        override fun getItemCount() = myDataset.size
    }

    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

    fun setTransactionsList(transactions: MutableList<Transaction>) {
        val viewManager = LinearLayoutManager(context)
        val viewAdapter = MyAdapter(transactions)

            apply {
                layoutManager = viewManager
                adapter = viewAdapter
            }
    }
}