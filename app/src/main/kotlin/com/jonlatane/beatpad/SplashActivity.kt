package com.jonlatane.beatpad

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.storage.Storage
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import java.net.URI

class SplashActivity : AppCompatActivity(), Storage {
  override val storageContext: Context get() = this
  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.SplashTheme)
    super.onCreate(savedInstanceState)
    // Start home activity
    //Thread.sleep(10000L)
    // Load all the typefaces
    MainApplication.chordTypeface
    MainApplication.chordTypefaceBold
    MainApplication.chordTypefaceRegular

    // Deserialize any Melody data opened.
    if (Intent.ACTION_VIEW == intent.action) {
      MainApplication.intentMelody = intent.data?.let { URI(it.toString()) }?.let { uri ->
        try {
          uri.toEntity("melody", "v1", Melody::class)
        } catch(t: Throwable) {
          toast("Failed to open melody")
          error("Failed to deserialize melody", t)
          null
        }
      }
      MainApplication.intentHarmony = intent.data?.let { URI(it.toString()) }?.let { uri ->
        try {
          uri.toEntity("harmony", "v1", Harmony::class)
        } catch(t: Throwable) {
          toast("Failed to open harmony")
          error("Failed to deserialize harmony", t)
          null
        }
      }
      MainApplication.intentPalette = intent.data?.let { URI(it.toString()) }?.let { uri ->
        try {
          uri.toEntity("palette", "v1", Palette::class).also {
            info("successfully opened palette")
          }
        } catch(t: Throwable) {
          toast("Failed to open palette")
          error("Failed to deserialize palette", t)
          null
        }
      }
    }
    startActivity(
      Intent(this@SplashActivity, PaletteEditorActivity::class.java).also {
        it.action = intent.action
        it.data = intent.data
      }
    )
    // close splash activity
    finish()
  }
}
