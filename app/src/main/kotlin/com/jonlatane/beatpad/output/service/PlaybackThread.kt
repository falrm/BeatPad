package com.jonlatane.beatpad.output.service

import BeatClockPaletteConsumer.tickPosition
import com.jonlatane.beatpad.R
import org.jetbrains.anko.*
import java.util.*

internal class PlaybackThread : Thread(), AnkoLogger {
  companion object {
    private const val subdivisionsPerBeat = 24 // This is the MIDI beat clock standard
  }

  var stopped = true
  var terminated = false

  override fun run() {
    while (!terminated) {
      try {
        if (!stopped) {
          val start = System.currentTimeMillis()
          val tickTime: Long = 60000L / (BeatClockPaletteConsumer.palette!!.bpm.toInt() * subdivisionsPerBeat)
          verbose { "Tick @${BeatClockPaletteConsumer.tickPosition} (T:${System.currentTimeMillis()}" }
          tryWithRetries { BeatClockPaletteConsumer.tick() }
          while(System.currentTimeMillis() < start + tickTime) {
            Thread.sleep(3L)
          }
        } else {
          BeatClockPaletteConsumer.viewModel?.toolbarView?.playButton?.imageResource = R.drawable.icons8_play_100
          BeatClockPaletteConsumer.clearActiveAttacks()
          synchronized(PlaybackThread) {
            (PlaybackThread as java.lang.Object).wait()
          }
          //Thread.sleep(10)
        }
      } catch (t: Throwable) {
        error( "Error during background playback", t)
      }
    }
  }

  private inline fun tryWithRetries(maxAttempts: Int = 1, action: () -> Unit) {
    var attempts = 0
    while (attempts++ < maxAttempts) {
      try {
        action()
        return
      } catch (t: Throwable) {
        continue
      }
    }
  }
}