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

package com.msindwan.shoebox.views.transactions

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.msindwan.shoebox.R
import com.msindwan.shoebox.widgets.CurrencyInput
import com.msindwan.shoebox.widgets.DateInput
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent


/**
 * New transaction activity.
 */
class NewTransaction : AppCompatActivity() {

    private var newTransactionTxtCategory: EditText? = null
    private var newTransactionTxtAmount: CurrencyInput? = null
    private var newTransactionTxtTitle: EditText? = null
    private var newTransactionTxtDate: DateInput? = null

    private var newTransactionBtnAddTxn: Button? = null
    private var newTransactionBtnCancel: Button? = null

    companion object {
        const val NEW_TRANSACTION_REQUEST_CODE = 1
        const val NEW_TRANSACTION_RESPONSE_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_transaction)
        setup()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Initializes the view.
     */
    private fun setup() {
        newTransactionTxtCategory = findViewById(R.id.new_transaction_txt_category)
        newTransactionTxtAmount = findViewById(R.id.new_transaction_txt_amount)
        newTransactionTxtTitle = findViewById(R.id.new_transaction_txt_title)

        newTransactionTxtDate = findViewById(R.id.new_transaction_txt_date)
        newTransactionTxtDate?.fragmentManager = supportFragmentManager

        newTransactionBtnAddTxn = findViewById(R.id.new_transaction_btn_add_transaction)
        newTransactionBtnCancel = findViewById(R.id.new_transaction_btn_cancel)

        newTransactionBtnAddTxn?.setOnClickListener(onAddClicked)
        newTransactionBtnCancel?.setOnClickListener(onCancelClicked)
    }

    /**
     * Handles when the "Add" button is clicked.
     */
    private val onAddClicked = View.OnClickListener {
        newTransactionBtnAddTxn?.isEnabled = false

        val amount: Long = newTransactionTxtAmount?.getAmount() ?: 0
        if (amount <= 0L) {
            newTransactionTxtAmount?.setError(resources.getString(R.string.budget_validation))
            newTransactionBtnAddTxn?.isEnabled = true
        } else {
            var title: String = newTransactionTxtTitle?.text?.toString() ?: ""
            if (title.isEmpty()) {
                title = resources.getString(R.string.no_title)
            }

            var category: String = newTransactionTxtCategory?.text?.toString() ?: ""
            if (category.isEmpty()) {
                category = resources.getString(R.string.misc)
            }

            val date: Long = newTransactionTxtDate!!.getDate()!!.time / 1000

            val intent = Intent()
            intent.putExtra("date", date)
            intent.putExtra("title", title)
            intent.putExtra("category", category)
            intent.putExtra("amount", amount)

            setResult(NEW_TRANSACTION_RESPONSE_CODE, intent)
            finish()
        }
    }

    /**
     * Handles when the "Cancel" button is clicked.
     */
    private val onCancelClicked = View.OnClickListener {
        finish()
    }
}