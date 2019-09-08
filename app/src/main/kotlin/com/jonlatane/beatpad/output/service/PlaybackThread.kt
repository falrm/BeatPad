package com.jonlatane.beatpad.output.service

import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.midi.AndroidMidi
import com.jonlatane.beatpad.midi.MidiSynthesizers
import org.jetbrains.anko.*

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
            sleep(3L)
          }
        } else {
          BeatClockPaletteConsumer.viewModel?.paletteToolbar?.playButton?.imageResource = R.drawable.icons8_play_100
          BeatClockPaletteConsumer.clearActiveAttacks()
          AndroidMidi.flushSendStream()
          if(MidiSynthesizers.synthesizers.size > 0) {

          }
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