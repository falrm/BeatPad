package com.jonlatane.beatpad.view.melody.renderer

import android.graphics.Canvas
import com.jonlatane.beatpad.view.melody.MelodyBeatView
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

interface MelodyBeatRenderer: MelodyBeatColorblockRenderer, MelodyBeatNotationRenderer, AnkoLogger {
  fun MelodyBeatView.renderMelodyBeat(canvas: Canvas) {
    if(colorblockAlpha > 0f) {
      renderColorblockMelodyBeat(canvas)
    }
    if(notationAlpha > 0f) {
      renderNotationMelodyBeat(canvas)
    }
  }
}