package com.jonlatane.beatpad.view.melody.renderer

import android.graphics.Canvas
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.view.melody.MelodyBeatView
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

interface MelodyBeatRenderer: MelodyBeatColorblockRenderer, MelodyBeatNotationRenderer, AnkoLogger {
  fun MelodyBeatView.renderMelodyBeat(canvas: Canvas) {
    sectionMelodiesOfPartTypeCache.invalidate()
    renderedMelodiesCache.invalidate()
    if(colorblockAlpha > 0f) {
      setupBaseBounds()
      renderColorblockMelodyBeat(canvas)
    }
    if(notationAlpha > 0f) {
      setupBaseBounds()
      renderNotationMelodyBeat(canvas)
    }
  }

  fun setupBaseBounds() {
    bounds.apply {
      top = overallBounds.top
      bottom = overallBounds.bottom
      left = overallBounds.left
      right = overallBounds.right
    }
  }
}