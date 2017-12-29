package com.jonlatane.beatpad.model.pattern

import com.jonlatane.beatpad.model.Pattern
import com.jonlatane.beatpad.model.Pattern.Element

class RationalPattern(
  elements: List<Element> = emptyList(),
  /** A value of 4 would indicate sixteenth notes in 4/4 time */
  override var subdivisionsPerBeat: Int = 1
): Pattern {
  override val elements: MutableList<Element>
    = elements.toMutableList()
  override var relativeTo: Int = 0
}