package com.jonlatane.beatpad.util

import android.app.Activity
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.jonlatane.beatpad.MainApplication

fun vibrate(ms: Int, amplitude: Int = 255) {
  val v = MainApplication.instance.getSystemService(Activity.VIBRATOR_SERVICE) as Vibrator
  if (Build.VERSION.SDK_INT >= 26) {
    v.vibrate(VibrationEffect.createOneShot(ms.toLong(), amplitude))
  } else {
    v.vibrate(ms.toLong())
  }
}