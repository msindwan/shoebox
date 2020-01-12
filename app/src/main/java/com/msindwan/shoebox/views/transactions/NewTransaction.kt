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
import androidx.appcompat.app.AppCompatActivity
import com.msindwan.shoebox.R
import com.msindwan.shoebox.data.DataAccessLayer
import com.msindwan.shoebox.widgets.CurrencyInput
import com.msindwan.shoebox.widgets.DateInput


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_transaction)
        setup()
    }

    private fun setup() {
        newTransactionTxtCategory = findViewById(R.id.new_transaction_txt_category)
        newTransactionTxtAmount = findViewById(R.id.new_transaction_txt_amount)
        newTransactionTxtTitle = findViewById(R.id.new_transaction_txt_title)
        newTransactionTxtDate = findViewById(R.id.new_transaction_txt_date)

        newTransactionBtnAddTxn = findViewById(R.id.new_transaction_btn_add_transaction)
        newTransactionBtnCancel = findViewById(R.id.new_transaction_btn_cancel)

        newTransactionBtnAddTxn?.setOnClickListener(onAddClicked)
        newTransactionBtnCancel?.setOnClickListener(onCancelClicked)
    }

    private val onAddClicked = View.OnClickListener {
        newTransactionBtnAddTxn?.isEnabled = false

        val dal: DataAccessLayer = DataAccessLayer.getInstance(this)
        val amount: Long = newTransactionTxtAmount?.getAmount() ?: 0
        if (amount <= 0L) {
            newTransactionTxtAmount?.setError(resources.getString(R.string.budget_validation))
            newTransactionBtnAddTxn?.isEnabled = true
        } else {
            var title: String = newTransactionTxtTitle?.text?.toString() ?: "No title"
            if (title.isEmpty()) {
                title = "No Title"
            }

            var category: String = newTransactionTxtCategory?.text?.toString() ?: "Misc"
            if (category.isEmpty()) {
                category = "Misc"
            }

            val date: Long = newTransactionTxtDate!!.getDate().time / 1000
            dal.transactionDAO?.insertTransaction(date, title, category, amount)
            finish()
        }
    }

    private val onCancelClicked = View.OnClickListener {
        finish()
    }
}