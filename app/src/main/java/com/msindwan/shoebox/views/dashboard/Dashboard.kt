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

package com.msindwan.shoebox.views.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.msindwan.shoebox.R
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.msindwan.shoebox.helpers.ActivityHelpers
import com.msindwan.shoebox.views.dashboard.components.FooterMenu
import com.msindwan.shoebox.views.dashboard.fragments.DashboardHome
import com.msindwan.shoebox.views.dashboard.fragments.DashboardTransactionsFragment
import com.msindwan.shoebox.views.dashboard.fragments.DashboardTrendsFragment
import com.msindwan.shoebox.views.dashboard.models.DashboardViewModel
import com.msindwan.shoebox.views.settings.BudgetSchedule
import com.msindwan.shoebox.views.transactions.NewTransaction
import org.threeten.bp.LocalDate


/**
 * Dashboard activity.
 */
class Dashboard : AppCompatActivity() {

    private var dashboardFooterMenu: FooterMenu? = null
    private var dashboardViewPager: ViewPager2? = null
    private var actionBarParent: Toolbar? = null
    private var actionBarTitle: TextView? = null
    private lateinit var dashboardModel: DashboardViewModel

    /**
     * Pager adapter for dashboard fragments.
     */
    private inner class DashboardPagerAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle
    ) :
        FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int {
            return 4
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                FooterMenu.MenuItem.TRANSACTIONS.value -> DashboardTransactionsFragment()
                FooterMenu.MenuItem.TRENDS.value -> DashboardTrendsFragment()
                else -> DashboardHome()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)
        setup()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.action_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.dashboard_menu_budget_schedule) {
            startActivityForResult(
                Intent(this, BudgetSchedule::class.java),
                ActivityHelpers.BUDGET_SCHEDULE_REQUEST_CODE
            )
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (
            requestCode == ActivityHelpers.NEW_TRANSACTION_REQUEST_CODE &&
            resultCode == ActivityHelpers.NEW_TRANSACTION_SUCCESS_RESPONSE_CODE &&
            data != null
        ) {
            val model = ViewModelProviders.of(this).get(DashboardViewModel::class.java)

            val date = data.getLongExtra("date", 0L)
            val title = data.getStringExtra("title")
            val category = data.getStringExtra("category")
            val amount = data.getLongExtra("amount", 0L)

            model.insertTransaction(LocalDate.ofEpochDay(date), title!!, category!!, amount)
        } else if (requestCode == ActivityHelpers.BUDGET_SCHEDULE_REQUEST_CODE) {
            dashboardModel.updateBudget()
        }
    }

    /**
     * Initializes the view.
     */
    private fun setup() {
        supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar!!.setDisplayShowCustomEnabled(true)
        supportActionBar!!.setCustomView(R.layout.action_bar)

        dashboardModel = ViewModelProviders.of(this).get(DashboardViewModel::class.java)
        dashboardModel.getCurrentMenuItem().observe(this, Observer { onMenuButtonClickHandler(it) })

        actionBarParent = supportActionBar?.customView?.parent as Toolbar
        actionBarTitle = actionBarParent?.findViewById(R.id.action_bar_txt_title)

        actionBarParent?.setPadding(0, 0, 0, 0)
        actionBarParent?.setContentInsetsAbsolute(0, 0)
        actionBarTitle?.text = resources.getString(R.string.dashboard)

        dashboardFooterMenu = findViewById(R.id.dashboard_footer_menu)
        dashboardFooterMenu?.menuButtonClickHandler = onMenuButtonClickHandler

        dashboardViewPager = findViewById(R.id.dashboard_view_pager)
        dashboardViewPager?.adapter =
            DashboardPagerAdapter(supportFragmentManager, lifecycle)
        dashboardViewPager?.isUserInputEnabled = false
        dashboardViewPager?.setCurrentItem(FooterMenu.MenuItem.HOME.value, false)
    }

    /**
     * Handles clicking different menu buttons.
     */
    private val onMenuButtonClickHandler = fun(menuItem: FooterMenu.MenuItem) {
        when (menuItem) {
            FooterMenu.MenuItem.TRANSACTIONS -> {
                actionBarTitle?.text = resources.getString(R.string.transactions)
                dashboardViewPager?.setCurrentItem(menuItem.value, false)
                dashboardFooterMenu?.setActiveMenuItem(menuItem)
            }
            FooterMenu.MenuItem.HOME -> {
                actionBarTitle?.text = resources.getString(R.string.dashboard)
                dashboardViewPager?.setCurrentItem(menuItem.value, false)
                dashboardFooterMenu?.setActiveMenuItem(menuItem)
            }
            FooterMenu.MenuItem.ADD -> {
                startActivityForResult(
                    Intent(this, NewTransaction::class.java),
                    ActivityHelpers.NEW_TRANSACTION_REQUEST_CODE
                )
            }
            FooterMenu.MenuItem.TRENDS -> {
                actionBarTitle?.text = resources.getString(R.string.trends)
                dashboardViewPager?.setCurrentItem(menuItem.value, false)
                dashboardFooterMenu?.setActiveMenuItem(menuItem)
            }
        }
    }
}
