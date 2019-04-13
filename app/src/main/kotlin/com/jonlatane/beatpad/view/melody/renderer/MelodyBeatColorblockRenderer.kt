package com.jonlatane.beatpad.view.melody.renderer

import BeatClockPaletteConsumer
import android.graphics.Canvas
import android.graphics.Rect
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Transposable
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.model.melody.RationalMelody
import com.jonlatane.beatpad.output.service.convertPatternIndex
import com.jonlatane.beatpad.util.size
import com.jonlatane.beatpad.view.colorboard.AlphaDrawer
import com.jonlatane.beatpad.view.colorboard.ColorGuide
import com.jonlatane.beatpad.view.melody.MelodyBeatEventHandlerBase
import com.jonlatane.beatpad.view.melody.MelodyBeatView
import com.jonlatane.beatpad.view.melody.MelodyViewModel
import org.jetbrains.anko.warn
import org.jetbrains.anko.withAlpha

interface MelodyBeatColorblockRenderer: BaseMelodyBeatRenderer, MelodyBeatEventHandlerBase {
  val colorblockAlpha: Float
  override var chord: Chord

  fun MelodyBeatView.renderColorblockMelodyBeat(canvas: Canvas) {
    canvas.renderSteps()
    melody?.let { melody ->
      canvas.drawMelody(
        melody,
        stepNoteAlpha = (0xAA * colorblockAlpha).toInt(),
        drawRhythm = true,
        alphaSource = colorblockAlpha
      )
    }

    BeatClockPaletteConsumer.section?.let { section ->
      section.melodies.filter { !it.isDisabled }.filter {
        when(melody?.limitedToNotesInHarmony) {
          null -> false
          true -> it.melody.limitedToNotesInHarmony
          false -> !it.melody.limitedToNotesInHarmony
        }
      }.map { it.melody }.forEach { melody ->
        canvas.drawMelody(
          melody,
          stepNoteAlpha = 66,
          drawColorGuide = false,
          alphaSource = colorblockAlpha
        )
      }
    }
  }
}