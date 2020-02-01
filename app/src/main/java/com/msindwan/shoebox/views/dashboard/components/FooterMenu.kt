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
import android.widget.ImageView
import android.widget.LinearLayout
import com.msindwan.shoebox.R


/**
 * Footer menu component.
 */
class FooterMenu : LinearLayout {

    /**
     * Menu item types.
     */
    enum class MenuItem(val value: Int) {
        HOME(0),
        TRANSACTIONS(1),
        ADD(2),
        TRENDS(3)
    }

    private var btnHome: LinearLayout? = null
    private var btnTxns: LinearLayout? = null
    private var btnTrends: LinearLayout? = null
    private var btnAdd: LinearLayout? = null

    var menuButtonClickHandler: ((MenuItem) -> Unit)? = null

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
        inflater.inflate(R.layout.dashboard_footer_menu, this, true)

        btnHome = findViewById(R.id.dashboard_footer_menu_btn_home)
        btnTxns = findViewById(R.id.dashboard_footer_menu_btn_txns)
        btnTrends = findViewById(R.id.dashboard_footer_menu_btn_trends)
        btnAdd = findViewById(R.id.dashboard_footer_menu_btn_add)

        btnHome?.setOnClickListener(onButtonClick)
        btnTxns?.setOnClickListener(onButtonClick)
        btnTrends?.setOnClickListener(onButtonClick)
        btnAdd?.setOnClickListener(onButtonClick)
    }

    /**
     * Sets the specified menu item state to "Active" and disables the rest.
     *
     * @param menuItem {MenuItem} The menu item to select.
     */
    fun setActiveMenuItem(menuItem: MenuItem) {
        val homeImageButton = btnHome?.getChildAt(0) as ImageView
        homeImageButton.setBackgroundResource(R.drawable.ic_house_inactive_icon)

        val txnsImageButton = btnTxns?.getChildAt(0) as ImageView
        txnsImageButton.setBackgroundResource(R.drawable.ic_transaction_inactive_icon)

        val trendsImageButton = btnTrends?.getChildAt(0) as ImageView
        trendsImageButton.setBackgroundResource(R.drawable.ic_bar_chart_inactive_24px)

        when (menuItem) {
            MenuItem.HOME -> homeImageButton.setBackgroundResource(R.drawable.ic_house_icon)
            MenuItem.TRANSACTIONS -> txnsImageButton.setBackgroundResource(R.drawable.ic_transaction_icon)
            MenuItem.TRENDS -> trendsImageButton.setBackgroundResource(R.drawable.ic_bar_chart_24px)
            MenuItem.ADD -> {
                /* no-op */
            }
        }
    }

    /**
     * Handles clicking on a menu item button.
     */
    private val onButtonClick = OnClickListener {
        var menuItem = MenuItem.HOME

        when (it.id) {
            R.id.dashboard_footer_menu_btn_home -> menuItem = MenuItem.HOME
            R.id.dashboard_footer_menu_btn_txns -> menuItem = MenuItem.TRANSACTIONS
            R.id.dashboard_footer_menu_btn_trends -> menuItem = MenuItem.TRENDS
            R.id.dashboard_footer_menu_btn_add -> menuItem = MenuItem.ADD
        }

        menuButtonClickHandler?.invoke(menuItem)
    }
}