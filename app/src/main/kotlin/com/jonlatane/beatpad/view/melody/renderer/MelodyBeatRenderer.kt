package com.jonlatane.beatpad.view.melody.renderer

import android.graphics.Canvas
import com.jonlatane.beatpad.view.melody.MelodyBeatView

interface MelodyBeatRenderer: MelodyBeatColorblockRenderer, MelodyBeatNotationRenderer {
  fun MelodyBeatView.renderMelodyBeat(canvas: Canvas) {
    colorblockAlpha.takeIf { it > 0f }.let {
      renderColorblockMelodyBeat(canvas)
    }
    notationAlpha.takeIf { it > 0f }.let {
      renderNotationMelodyBeat(canvas)
    }
  }
}