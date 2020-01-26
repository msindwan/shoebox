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

package com.msindwan.shoebox.views

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.threetenabp.AndroidThreeTen
import com.msindwan.shoebox.R
import com.msindwan.shoebox.data.DataAccessLayer
import com.msindwan.shoebox.data.entities.Budget
import com.msindwan.shoebox.data.entities.LocalDateRange
import com.msindwan.shoebox.views.dashboard.Dashboard
import com.msindwan.shoebox.views.setup.Setup


/**
 * App view router.
 */
class AppIndex : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)

        setContentView(R.layout.app_index)

        val dal: DataAccessLayer = DataAccessLayer.getInstance(applicationContext)
        val budgets: List<Budget> =
            dal.budgetDAO.getBudgets(LocalDateRange.currentMonth()).filterNotNull()

        // Skip setup if a budget already exists.
        val nextView: Intent = if (budgets.isNotEmpty()) {
            Intent(applicationContext, Dashboard::class.java)
        } else {
            Intent(applicationContext, Setup::class.java)
        }

        startActivity(nextView)
        finish()
    }
}
