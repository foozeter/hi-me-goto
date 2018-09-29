package com.hayashihideo.himegoto

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.view.Menu
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(appbar)
        chips.maxLines = 2
        chips.chipsMarginTop = 24
        chips.chipsMarginBottom = 24
        chips.chipsMarginStart = 24
        chips.chipsMarginEnd = 24
        chips.chipsVerticalSpace = 24
        chips.chipsHorizontalSpace = 24
        chips.setLabels(listOf("android",  "iphoneX", "super market fantasy", "ride on shooting star",
                "the pillows", "please mr.lostman", "blouse drive monster",
                "terminal heaven's rock", "xperia"))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }
}
