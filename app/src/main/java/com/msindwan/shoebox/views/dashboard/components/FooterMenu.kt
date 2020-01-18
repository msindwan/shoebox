package com.msindwan.shoebox.views.dashboard.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.msindwan.shoebox.R

class FooterMenu : LinearLayout {
    enum class MenuItem(val value: Int) {
        HOME(0),
        TRANSACTIONS(1),
        ADD(2),
        TRENDS(3),
        SETTINGS(4)
    }

    var menuButtonClickHandler: ((MenuItem) -> Unit)? = null

    private var btnHome: LinearLayout? = null
    private var btnTxns: LinearLayout? = null
    private var btnTrends: LinearLayout? = null
    private var btnSettings: LinearLayout? = null
    private var btnAdd: LinearLayout? = null

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.dashboard_footer_menu, this, true)

        attachButtonHandlers()
    }

    constructor(context: Context) : super(context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.dashboard_footer_menu, this, true)

        attachButtonHandlers()
    }

    private fun attachButtonHandlers() {
        btnHome = findViewById(R.id.dashboard_footer_menu_btn_home)
        btnTxns = findViewById(R.id.dashboard_footer_menu_btn_txns)
        btnTrends = findViewById(R.id.dashboard_footer_menu_btn_trends)
        btnSettings = findViewById(R.id.dashboard_footer_menu_btn_settings)
        btnAdd = findViewById(R.id.dashboard_footer_menu_btn_add)

        btnHome?.setOnClickListener(onButtonClick(btnHome!!))
        btnTxns?.setOnClickListener(onButtonClick(btnTxns!!))
        btnTrends?.setOnClickListener(onButtonClick(btnTrends!!))
        btnSettings?.setOnClickListener(onButtonClick(btnSettings!!))
        btnAdd?.setOnClickListener(onButtonClick(btnAdd!!))
    }

    fun setActiveMenuItem(menuItem: MenuItem) {
        // @todo Make active/inactive button state more apparent visually
        val homeImageButton = btnHome?.getChildAt(0) as ImageView
        homeImageButton.setBackgroundResource(R.drawable.ic_house_inactive_icon)

        val txnsImageButton = btnTxns?.getChildAt(0) as ImageView
        txnsImageButton.setBackgroundResource(R.drawable.ic_transaction_inactive_icon)

        val trendsImageButton = btnTrends?.getChildAt(0) as ImageView
        trendsImageButton.setBackgroundResource(R.drawable.ic_activity_inactive_icon)

        val settingsImageButton = btnSettings?.getChildAt(0) as ImageView
        settingsImageButton.setBackgroundResource(R.drawable.ic_cog_inactive_icon)

        when (menuItem) {
            MenuItem.HOME -> homeImageButton.setBackgroundResource(R.drawable.ic_house_icon)
            MenuItem.TRANSACTIONS -> txnsImageButton.setBackgroundResource(R.drawable.ic_transaction_icon)
            MenuItem.TRENDS -> trendsImageButton.setBackgroundResource(R.drawable.ic_activity_icon)
            MenuItem.SETTINGS -> settingsImageButton.setBackgroundResource(R.drawable.ic_cog_icon)
            MenuItem.ADD -> {
                // TODO: Have active "Add" state
            }
        }
    }

    private fun onButtonClick(view: View) = OnClickListener {
        var menuItem = MenuItem.HOME

        when (view.id) {
            R.id.dashboard_footer_menu_btn_home -> menuItem = MenuItem.HOME
            R.id.dashboard_footer_menu_btn_txns -> menuItem = MenuItem.TRANSACTIONS
            R.id.dashboard_footer_menu_btn_trends -> menuItem = MenuItem.TRENDS
            R.id.dashboard_footer_menu_btn_settings -> menuItem = MenuItem.SETTINGS
            R.id.dashboard_footer_menu_btn_add -> menuItem = MenuItem.ADD
        }

        menuButtonClickHandler?.invoke(menuItem)
    }
}