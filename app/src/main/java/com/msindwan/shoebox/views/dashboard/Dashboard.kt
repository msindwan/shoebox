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
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.msindwan.shoebox.R
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.msindwan.shoebox.views.dashboard.components.FooterMenu
import com.msindwan.shoebox.views.dashboard.fragments.DashboardHome
import com.msindwan.shoebox.views.dashboard.fragments.DashboardTransactionsFragment
import com.msindwan.shoebox.views.transactions.NewTransaction


/**
 * Dashboard activity.
 */
class Dashboard : AppCompatActivity() {

    private var dashboardFooterMenu: FooterMenu? = null
    private var dashboardViewPager: ViewPager? = null

    /**
     * Pager adapter for dashboard fragments.
     */
    class DashboardPagerAdapter(fragmentManager: FragmentManager): FragmentPagerAdapter(fragmentManager) {
        override fun getCount(): Int {
            return 2
        }

        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> return DashboardHome()
                1 -> return DashboardTransactionsFragment()
            }

            // @todo Add assertion or default to getItem
            return DashboardHome()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)
        setup()
    }

    /**
     * Initializes the view.
     */
    private fun setup() {
        this.supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar!!.setDisplayShowCustomEnabled(true)
        supportActionBar!!.setCustomView(R.layout.action_bar)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        val actionBarParent = supportActionBar?.customView?.parent as Toolbar
        actionBarParent.setPadding(0,0,0,0)
        actionBarParent.setContentInsetsAbsolute(0, 0)
        actionBarParent.findViewById<TextView>(R.id.action_bar_txt_title).text = "Dashboard"

        dashboardFooterMenu = findViewById(R.id.dashboard_footer_menu)
        dashboardFooterMenu?.menuButtonClickHandler = onMenuButtonClickHandler

        // @todo disable pager swipe
        dashboardViewPager = findViewById(R.id.dashboard_view_pager)
        dashboardViewPager?.adapter = DashboardPagerAdapter(supportFragmentManager)
        dashboardViewPager?.setCurrentItem(0, false)
    }

    /**
     * Handles clicking different menu buttons.
     */
    private val onMenuButtonClickHandler = fun (menuItem: FooterMenu.MenuItem) {
        val actionBarParent = supportActionBar?.customView?.parent as Toolbar

        when(menuItem) {
            FooterMenu.MenuItem.TRANSACTIONS -> {
                actionBarParent.findViewById<TextView>(R.id.action_bar_txt_title).text = "Transactions"
                dashboardViewPager?.setCurrentItem(1, false)
                dashboardFooterMenu?.setActiveMenuItem(menuItem)
            }
            FooterMenu.MenuItem.HOME -> {
                actionBarParent.findViewById<TextView>(R.id.action_bar_txt_title).text = "Dashboard"
                dashboardViewPager?.setCurrentItem(0, false)
                dashboardFooterMenu?.setActiveMenuItem(menuItem)
            }
            FooterMenu.MenuItem.ADD -> {
                startActivity(Intent(this, NewTransaction::class.java))
            }
            else -> {

            }
        }
    }
}
