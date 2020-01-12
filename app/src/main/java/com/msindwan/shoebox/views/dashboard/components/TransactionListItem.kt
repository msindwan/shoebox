package com.msindwan.shoebox.views.dashboard.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.msindwan.shoebox.R

class TransactionListItem: LinearLayout {
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        inflater.inflate(R.layout.dashboard_transaction_list_item, this, true)
    }

    constructor(context: Context): super(context) {
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        orientation = VERTICAL
        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        inflater.inflate(R.layout.dashboard_transaction_list_item, this, true)
    }

    fun setPrimaryHeader(header: String) {
        val headerView: TextView = findViewById(R.id.transaction_list_item_month_header)
        headerView.visibility = TextView.VISIBLE
        headerView.text = header
    }

    fun setHeader(header: String) {
        val headerView: TextView = findViewById(R.id.transaction_list_item_header)
        headerView.visibility = TextView.VISIBLE
        headerView.text = header
    }

    fun setTitle(title: String) {
        val titleView: TextView = findViewById(R.id.transaction_list_item_title)
        titleView.text = title
    }

    fun setAmount(amount: String) {
        val amountView: TextView = findViewById(R.id.transaction_list_item_amount)
        amountView.text = amount
    }

    fun setDate(date: String) {
        val dateView: TextView = findViewById(R.id.transaction_list_item_date)
        dateView.text = date
    }

    fun setType(type: String) {
        val typeView: TextView = findViewById(R.id.transaction_list_item_type)
        typeView.text = type
    }
}