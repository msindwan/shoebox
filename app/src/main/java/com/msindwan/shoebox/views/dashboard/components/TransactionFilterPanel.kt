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
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.*
import androidx.fragment.app.FragmentManager
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar
import com.msindwan.shoebox.R
import com.msindwan.shoebox.data.entities.LocalDateRange
import com.msindwan.shoebox.data.entities.SearchFilters
import com.msindwan.shoebox.widgets.DateInput
import java.text.NumberFormat
import java.util.*


/**
 * Transaction filter panel component.
 */
class TransactionFilterPanel : LinearLayout {

    private var filterTitle: EditText? = null
    private var filterApply: Button? = null
    private var filterAmount: CrystalRangeSeekbar? = null
    private var filterAmountMin: TextView? = null
    private var filterAmountMax: TextView? = null
    private var filterFrom: DateInput? = null
    private var filterTo: DateInput? = null
    private var filterCount: TextView? = null
    private var filterNoStartDate: TextView? = null
    private var filterNoEndDate: TextView? = null
    private var filterCategory: AutoCompleteTextView? = null
    private var filterReset: LinearLayout? = null

    private var minAmount: Int? = null
    private var maxAmount: Int? = null

    private var applyFiltersCallback: ((filters: SearchFilters) -> Unit)? = null
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

    companion object {
        private const val MAX_FILTER_AMOUNT = 5000
        private const val MIN_FILTER_AMOUNT = 0
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        setup()
    }

    constructor(context: Context) : super(context) {
        setup()
    }

    /**
     * Initializes the view.
     */
    private fun setup() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.dashboard_transaction_filter_panel, this, true)

        currencyFormatter.maximumFractionDigits = 0

        filterApply = findViewById(R.id.dashboard_transactions_btn_apply_filters)
        filterAmount = findViewById(R.id.dashboard_transactions_amount_slider)
        filterAmountMax = findViewById(R.id.dashboard_transactions_amount_max)
        filterAmountMin = findViewById(R.id.dashboard_transactions_amount_min)
        filterFrom = findViewById(R.id.dashboard_transactions_txt_from)
        filterTo = findViewById(R.id.dashboard_transactions_txt_to)
        filterNoStartDate = findViewById(R.id.dashboard_transactions_filter_no_start_date)
        filterNoEndDate = findViewById(R.id.dashboard_transactions_filter_no_end_date)
        filterCategory = findViewById(R.id.dashboard_transactions_txt_category)
        filterTitle = findViewById(R.id.dashboard_transactions_txt_title)
        filterCount = findViewById(R.id.dashboard_transactions_filters_count)
        filterReset = findViewById(R.id.dashboard_transaction_filters_btn_reset)

        filterReset?.setOnClickListener(onResetFiltersClicked)

        filterNoStartDate?.setOnClickListener(onNoStartDateClicked)
        filterNoEndDate?.setOnClickListener(onNoEndDateClicked)

        filterTo?.setDate(null)
        filterFrom?.setDate(null)

        filterAmount?.setMaxValue(MAX_FILTER_AMOUNT.toFloat())
        filterAmount?.setMinValue(MIN_FILTER_AMOUNT.toFloat())
        filterAmount?.setOnRangeSeekbarChangeListener(onAmountFilterChanged)

        filterApply?.setOnClickListener(onApplyFilters)
    }

    /**
     * Sets the listener that's fired when filters are applied.
     *
     * @param listener {(filters: SearchFilters) -> Unit} The listener to set.
     */
    fun setOnApplyFiltersClickListener(listener: ((filters: SearchFilters) -> Unit)) {
        applyFiltersCallback = listener
    }

    /**
     * Sets the filters for the panel.
     */
    fun setFilters(searchFilters: SearchFilters) {
        var numFilters = 0
        val startDate = searchFilters.dateRange.startDate
        val endDate = searchFilters.dateRange.endDate

        minAmount = searchFilters.minAmount
        maxAmount = searchFilters.maxAmount

        if (searchFilters.title != null) {
            numFilters++
        }

        if (searchFilters.category != null) {
            numFilters++
        }

        if (startDate != null || endDate != null) {
            numFilters++
        }

        if (searchFilters.minAmount != null || searchFilters.maxAmount != null) {
            numFilters++
        }

        filterTitle?.setText(searchFilters.title)
        filterCategory?.setText(searchFilters.category)
        filterFrom?.setDate(startDate)
        filterTo?.setDate(endDate)
        setAmountRange(
            searchFilters.minAmount?.div(100) ?: MIN_FILTER_AMOUNT,
            searchFilters.maxAmount?.div(100) ?: MAX_FILTER_AMOUNT
        )
        filterCount?.text =
            resources.getQuantityString(R.plurals.applied_filters, numFilters, numFilters)
    }

    /**
     * Sets the fragment manager for necessary controls.
     *
     * @param manager {FragmentManager?} The fragment manager to set.
     */
    fun setFragmentManager(manager: FragmentManager?) {
        filterFrom?.fragmentManager = manager
        filterTo?.fragmentManager = manager
    }

    /**
     * Updates the amount range filter
     *
     * @param minValue {Int} The minimum value to set (in dollars).
     * @param maxValue {Int} The maximum value to set (in dollars).
     */
    private fun setAmountRange(minValue: Int, maxValue: Int) {
        filterAmountMin?.text = currencyFormatter.format(minValue)
        minAmount = if (minValue == 0) null else minValue * 100

        if (maxValue >= MAX_FILTER_AMOUNT) {
            filterAmountMax?.text =
                resources.getString(
                    R.string.no_max_amount,
                    currencyFormatter.format(maxValue)
                )
            maxAmount = null
        } else {
            filterAmountMax?.text = currencyFormatter.format(maxValue)
            maxAmount = maxValue * 100
        }
    }

    /**
     * Handles clicking on "Apply filters".
     */
    private val onApplyFilters = OnClickListener {
        val category: String?
        val title: String?
        var numFilters = 0

        if (filterTitle?.text?.isEmpty() == false) {
            title = filterTitle!!.text!!.toString()
            numFilters++
        } else {
            title = null
        }

        if (filterCategory?.text?.isEmpty() == false) {
            category = filterCategory!!.text!!.toString()
            numFilters++
        } else {
            category = null
        }

        val from = filterFrom?.getDate()
        val to = filterTo?.getDate()

        if (from != null || to != null) {
            numFilters++
        }

        if (minAmount != null || maxAmount != null) {
            numFilters++
        }

        filterCount?.text =
            resources.getQuantityString(R.plurals.applied_filters, numFilters, numFilters)

        val filters = SearchFilters(
            title,
            LocalDateRange(from, to),
            minAmount,
            maxAmount,
            category
        )
        applyFiltersCallback?.invoke(filters)
    }

    /**
     * Handles clicking on "No start date" for the date range.
     */
    private val onNoStartDateClicked = OnClickListener {
        filterFrom?.setDate(null)
    }

    /**
     * Handles clicking on "No end date" for the date range.
     */
    private val onNoEndDateClicked = OnClickListener {
        filterTo?.setDate(null)
    }

    /**
     * Handles clicking on the "Reset Filters" button.
     */
    private val onResetFiltersClicked = OnClickListener {
        val filters = SearchFilters(
            null,
            LocalDateRange(null, null),
            null,
            null,
            null
        )
        setFilters(filters)
        applyFiltersCallback?.invoke(filters)
    }

    /**
     * Handles changing the amount filter.
     */
    private val onAmountFilterChanged = OnRangeSeekbarChangeListener(
        fun(minValue: Number, maxValue: Number) {
            setAmountRange(minValue.toInt(), maxValue.toInt())
        }
    )
}
