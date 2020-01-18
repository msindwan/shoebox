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

package com.msindwan.shoebox.views.dashboard.components

import android.content.Context
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.msindwan.shoebox.R
import com.msindwan.shoebox.data.entities.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


/**
 * Transaction list view component.
 */
class TransactionListView : LinearLayout {
    /**
     * Transaction List Item component.
     */
    private inner class TransactionListItem : LinearLayout {
        private var listItemTxtSecondaryHeader: TextView? = null
        private var listItemTxtPrimaryHeader: TextView? = null
        private var listItemSelect: LinearLayout? = null
        private var listItemChkSelect: CheckBox? = null
        private var listItemTxtCategory: TextView? = null
        private var listItemTxtTitle: TextView? = null
        private var listItemTxtAmount: TextView? = null
        private var listItemTxtDate: TextView? = null

        private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        private val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

        constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
            setup()
        }

        constructor(context: Context) : super(context) {
            setup()
        }

        /**
         * Initializes the view.
         */
        private fun setup() {
            // @todo: Merge attribute overrides.
            orientation = VERTICAL
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.dashboard_transaction_list_item, this, true)
            setBackgroundResource(R.drawable.list_view_card_background)

            // @todo Rename ids
            listItemChkSelect = findViewById(R.id.dashboard_transaction_list_item_chk_select)
            listItemTxtPrimaryHeader = findViewById(R.id.transaction_list_item_month_header)
            listItemTxtSecondaryHeader = findViewById(R.id.transaction_list_item_header)
            listItemSelect = findViewById(R.id.transaction_select)
            listItemTxtCategory = findViewById(R.id.transaction_list_item_type)
            listItemTxtTitle = findViewById(R.id.transaction_list_item_title)
            listItemTxtAmount = findViewById(R.id.transaction_list_item_amount)
            listItemTxtDate = findViewById(R.id.transaction_list_item_date)
        }

        /**
         * Sets the item as selected.
         *
         * @param toggle {Boolean} whether or not the item is selected.
         */
        fun setIsChecked(toggle: Boolean) {
            listItemChkSelect?.isChecked = toggle
        }

        /**
         * Updates the primary header display.
         *
         * @param header {String} the header text to display.
         */
        fun setPrimaryHeader(header: String?) {
            if (header == null) {
                listItemTxtPrimaryHeader?.visibility = TextView.GONE
            } else {
                listItemTxtPrimaryHeader?.visibility = TextView.VISIBLE
                listItemTxtPrimaryHeader?.text = header
            }
        }

        /**
         * Updates the secondary header display.
         *
         * @param header {String} the header text to display.
         */
        fun setHeader(header: String?) {
            if (header == null) {
                listItemTxtSecondaryHeader?.visibility = TextView.GONE
            } else {
                listItemTxtSecondaryHeader?.visibility = TextView.VISIBLE
                listItemTxtSecondaryHeader?.text = header
            }
        }

        /**
         * Sets the transaction to display in the list item.
         *
         * @param transaction {Transaction} The transaction to display.
         */
        fun setTransaction(transaction: Transaction) {
            listItemTxtCategory?.text = transaction.type
            listItemTxtTitle?.text = transaction.title ?: "No Title"
            listItemTxtAmount?.text = currencyFormatter.format(transaction.amount / 100)
            listItemTxtDate?.text = sdf.format(Date(transaction.date * 1000))
        }

        /**
         * Toggles the selection display.
         *
         * @param visible {Boolean} Whether or not to display the select control.
         */
        fun setSelectable(visible: Boolean) {
            listItemSelect?.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

    /**
     * View holder for transactions.
     */
    private inner class TransactionViewHolder(val view: TransactionListItem) :
        RecyclerView.ViewHolder(view)

    /**
     * Adapter class for transaction items.
     */
    private inner class TransactionViewAdapter(private val transactions: MutableList<Transaction>) :
        RecyclerView.Adapter<TransactionViewHolder>() {

        private val selectedItems: MutableSet<Transaction> = mutableSetOf()
        private val dayGroups: MutableMap<String, Int> = mutableMapOf()
        private val monthGroups: MutableMap<String, Int> = mutableMapOf()

        private val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        private val monthF = SimpleDateFormat("MMMM, yyyy", Locale.getDefault())

        init {
            updateItemGroups()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
            return TransactionViewHolder(TransactionListItem(parent.context))
        }

        override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
            val transaction = transactions[position]
            val formattedDate = sdf.format(Date(transaction.date * 1000))
            val formattedMonth = monthF.format(Date(transaction.date * 1000))

            val view = holder.view
            if (monthGroups[formattedMonth] == position) {
                view.setPrimaryHeader(formattedMonth)
            } else {
                view.setPrimaryHeader(null)
            }

            if (dayGroups[formattedDate] == position) {
                view.setHeader(formattedDate)
            } else {
                view.setHeader(null)
            }

            view.setTransaction(transaction)
            view.setSelectable(editMode)
            view.setIsChecked(editMode && selectedItems.contains(transaction))

            holder.view.findViewById<LinearLayout>(R.id.transaction_view).setOnClickListener {
                if (editMode) {
                    onSelectItem(transaction)
                }
            }

            holder.view.findViewById<LinearLayout>(R.id.transaction_view).setOnLongClickListener {
                onSelectItem(transaction)
            }
        }

        override fun getItemCount(): Int = transactions.size

        private fun updateItemGroups() {
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val monthF = SimpleDateFormat("MMMM, yyyy", Locale.getDefault())

            dayGroups.clear()
            monthGroups.clear()

            for ((index, transaction) in transactions.withIndex()) {
                val formattedDate = sdf.format(Date(transaction.date * 1000))
                val formattedMonth = monthF.format(Date(transaction.date * 1000))

                if (!dayGroups.contains(formattedDate)) {
                    dayGroups[formattedDate] = index
                }

                if (!monthGroups.contains(formattedMonth)) {
                    monthGroups[formattedMonth] = index
                }
            }

        }

        private fun onSelectItem(transaction: Transaction): Boolean {
            if (selectedItems.contains(transaction)) {
                selectedItems.remove(transaction)
            } else {
                selectedItems.add(transaction)
            }

            editMode = selectedItems.size > 0
            actionBar?.visibility = if (selectedItems.size > 0) VISIBLE else GONE
            deleteFab?.text = "DELETE ${selectedItems.size}"
            deleteFab?.extend()
                // resources.getString(R.string.delete_n_transactions, selectedItems.size)

            notifyDataSetChanged()
            return true
        }

        fun deleteSelectedItems() {
            val selectedTransactions = selectedItems.toList()
            transactions.removeAll(selectedTransactions)
            selectedItems.clear()
            updateItemGroups()
            notifyDataSetChanged()
            onDeleteTransactions?.invoke(selectedTransactions.toList())
        }

        fun clearSelectedItems() {
            selectedItems.clear()
            notifyDataSetChanged()
        }
    }

    private var onDeleteTransactions: ((transactions: List<Transaction>) -> Unit)? = null
    private var recyclerView: RecyclerView? = null
    private var actionBar: LinearLayout? = null
    private var viewMoreBtn: Button? = null
    private var editMode: Boolean = false
    private var backFab: FloatingActionButton? = null
    private var deleteFab: ExtendedFloatingActionButton? = null
    private var viewAdapter: TransactionViewAdapter? = null
    private var emptyState: TextView? = null

    constructor(context: Context) : super(context) {
        setup()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        setup()
    }

    /**
     * Initializes the view.
     */
    private fun setup() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.dashboard_transaction_list_view, this, true)
        orientation = VERTICAL

        recyclerView = findViewById(R.id.dashboard_transaction_list_view_recycler)
        viewMoreBtn = findViewById(R.id.dashboard_transaction_list_view_btn_view_more)
        actionBar = findViewById(R.id.dashboard_transaction_list_view_action_bar)
        emptyState = findViewById(R.id.dashboard_transaction_list_view_empty_state)

        backFab = findViewById(R.id.dashboard_transaction_list_view_fab_back)
        backFab?.setOnClickListener(onEditBackFabClick)

        deleteFab = findViewById(R.id.dashboard_transaction_list_view_fab_delete)
        deleteFab?.setOnClickListener(onEditDeleteFabClick)
    }

    /**
     * Sets the list of transactions to render.
     *
     * @param transactions {MutableList<Transaction>} The transactions to display in the list.
     */
    fun setTransactionsList(transactions: List<Transaction>) {
        val viewManager = LinearLayoutManager(context)
        viewAdapter = TransactionViewAdapter(transactions.toMutableList())

        if (transactions.isEmpty()) {
            emptyState?.visibility = View.VISIBLE
        } else {
            emptyState?.visibility = View.GONE
        }

        recyclerView?.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    /**
     * Whether or not to display the "View More" button.
     *
     * @param toggle { Boolean } Toggle to show or hide the button
     */
    fun showViewMoreButton(toggle: Boolean) {
        viewMoreBtn?.visibility = if (toggle) View.VISIBLE else View.GONE
    }

    /**
     * Attaches a click listener to the "View More" button.
     *
     * @param listener {View.OnClickListener} The callback to invoke on click.
     */
    fun setViewMoreClickListener(listener: OnClickListener) {
        viewMoreBtn?.setOnClickListener(listener)
    }

    /**
     *  Attaches a listener fired when transactions are deleted.
     *
     *  @param listener {(transactions: List<Transaction>) -> Unit)} The callback to invoke on click.
     */
    fun setOnDeleteTransactions(listener: ((transactions: List<Transaction>) -> Unit)) {
        onDeleteTransactions = listener
    }

    /**
     * Toggles edit mode in the list view
     */
    private fun clearEditMode() {
        editMode = false
        actionBar?.visibility = View.GONE
        viewAdapter?.notifyDataSetChanged()
    }

    /**
     * Handles clicking on the "Back" floating action button in edit mode.
     */
    private val onEditBackFabClick = OnClickListener {
        viewAdapter?.clearSelectedItems()
        clearEditMode()
    }

    /**
     * Handles clicking on the "Delete" floating action button in edit mode.
     */
    private val onEditDeleteFabClick = OnClickListener {
        viewAdapter?.deleteSelectedItems()
        clearEditMode()
    }
}