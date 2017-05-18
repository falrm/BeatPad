package com.jonlatane.beatpad

import android.support.v7.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    abstract fun updateInstrumentNames()
}