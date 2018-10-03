package com.jonlatane.beatpad.output.service

import BeatClockPaletteConsumer.tickPosition
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import org.jetbrains.anko.verbose

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
          /*when {
            tickPosition % subdivisionsPerBeat == 0 -> tickPosition / subdivisionsPerBeat
            else -> null
          }?.let { info("Quarter #$it") }*/
          info { "Tick @${BeatClockPaletteConsumer.tickPosition} (T:${System.currentTimeMillis()}" }
          tryWithRetries { BeatClockPaletteConsumer.tick() }
          /*val sleepTime = (tickTime - (System.currentTimeMillis() - start)).let {
            when {
              it < 0 -> 0L
              it > 800 -> 800L
              else -> it
            }
          }
          Thread.sleep(sleepTime)*/
          while(System.currentTimeMillis() < start + tickTime) {
            Thread.sleep(3L)
          }
        } else {
          BeatClockPaletteConsumer.clearActiveAttacks()
          Thread.sleep(10)
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