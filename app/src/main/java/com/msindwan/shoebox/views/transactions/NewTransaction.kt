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
import com.msindwan.shoebox.widgets.DateTimeInput
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.msindwan.shoebox.helpers.ActivityHelpers
import org.threeten.bp.ZoneId


/**
 * New transaction activity.
 */
class NewTransaction : AppCompatActivity() {

    private lateinit var zoneId: ZoneId
    private var txtCategory: EditText? = null
    private var txtAmount: CurrencyInput? = null
    private var txtTitle: EditText? = null
    private var txtDate: DateTimeInput? = null
    private var btnAddTxn: Button? = null
    private var btnCancel: Button? = null

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
        txtCategory = findViewById(R.id.new_transaction_txt_category)
        txtAmount = findViewById(R.id.new_transaction_txt_amount)
        txtTitle = findViewById(R.id.new_transaction_txt_title)

        txtDate = findViewById(R.id.new_transaction_txt_date)
        txtDate?.fragmentManager = supportFragmentManager

        btnAddTxn = findViewById(R.id.new_transaction_btn_add_transaction)
        btnCancel = findViewById(R.id.new_transaction_btn_cancel)

        btnAddTxn?.setOnClickListener(onAddClicked)
        btnCancel?.setOnClickListener(onCancelClicked)
        zoneId = ZoneId.systemDefault()
    }

    /**
     * Handles when the "Add" button is clicked.
     */
    private val onAddClicked = View.OnClickListener {
        btnAddTxn?.isEnabled = false

        val amount: Long = txtAmount?.getAmount() ?: 0
        if (amount <= 0L) {
            txtAmount?.editText?.error =
                resources.getString(R.string.budget_validation)
            btnAddTxn?.isEnabled = true
        } else {
            var title: String = txtTitle?.text?.toString() ?: ""
            if (title.isEmpty()) {
                title = resources.getString(R.string.no_title)
            }

            var category: String = txtCategory?.text?.toString() ?: ""
            if (category.isEmpty()) {
                category = resources.getString(R.string.misc)
            }

            val date = txtDate!!.date!!.atZone(zoneId).toOffsetDateTime().toString()

            val intent = Intent()
            intent.putExtra("date", date)
            intent.putExtra("zoneId", zoneId.id)
            intent.putExtra("title", title)
            intent.putExtra("category", category)
            intent.putExtra("amount", amount)

            setResult(ActivityHelpers.NEW_TRANSACTION_SUCCESS_RESPONSE_CODE, intent)
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