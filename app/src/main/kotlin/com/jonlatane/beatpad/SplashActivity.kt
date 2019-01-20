package com.jonlatane.beatpad

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.SplashTheme)
    super.onCreate(savedInstanceState)
    // Start home activity
    //Thread.sleep(10000L)
    startActivity(Intent(this@SplashActivity, PaletteEditorActivity::class.java))
    // close splash activity
    finish()
  }
}
