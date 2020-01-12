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

package com.msindwan.shoebox.views.setup

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.msindwan.shoebox.R
import com.msindwan.shoebox.data.DataAccessLayer
import com.msindwan.shoebox.data.entities.Budget
import com.msindwan.shoebox.data.entities.DateRange
import com.msindwan.shoebox.helpers.DateHelpers
import com.msindwan.shoebox.views.dashboard.Dashboard
import com.msindwan.shoebox.widgets.CurrencyInput

/**
 * Initial application setup activity on first install.
 */
class Setup : AppCompatActivity() {

    private var setupTxtBudget: CurrencyInput? = null
    private var setupTxtTitle: TextView? = null
    private var setupBtnNext: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setup)
        setup()
    }

    /**
     * Initializes the view.
     */
    private fun setup() {
        setupTxtBudget = findViewById(R.id.setup_txt_budget)
        setupTxtTitle = findViewById(R.id.setup_txt_title)
        setupBtnNext = findViewById(R.id.setup_btn_next)

        setupBtnNext?.setOnClickListener(onSetupBtnNextClick)
    }

    /**
     * Handles pressing the "next" button.
     */
    private var onSetupBtnNextClick = View.OnClickListener {
        setupBtnNext?.isEnabled = false

        val budget: Long = setupTxtBudget?.getAmount() ?: 0L

        if (budget <= 0L) {
            setupTxtBudget?.setError(resources.getString(R.string.budget_validation))
            setupBtnNext?.isEnabled = true
        } else {
            val currentMonth: DateRange = DateHelpers.getCurrentMonth()
            val dal: DataAccessLayer = DataAccessLayer.getInstance(applicationContext)

            // Create the initial monthly budget and start the dashboard.
            dal.budgetDAO?.upsertBudget(
                currentMonth.startDate,
                Budget.NO_END_DATE,
                budget,
                "USD",
                "M"
            )

            val dashboard = Intent(applicationContext, Dashboard::class.java)
            startActivity(dashboard)
            finish()
        }
    }
}
