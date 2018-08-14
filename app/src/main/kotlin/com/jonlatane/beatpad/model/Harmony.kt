package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.model.harmony.chord.Chord

class Harmony(
  elements: List<Harmony.Element> = emptyList(),
  override var subdivisionsPerBeat: Int = 1
) : Pattern<Harmony.Element> {
  override val elements: MutableList<Harmony.Element> = elements.toMutableList()
  override var tonic: Int = 0
  override fun transpose(interval: Int) = Harmony(
    elements.map { it.transpose(interval) },
    subdivisionsPerBeat
  )

  sealed class Element : Transposable<Element> {
    abstract var chord: Chord
    class Change(
      override var chord: Chord
    ) : Element() {
      override fun transpose(interval: Int): Change {
        return Change(chord.transpose(interval))
      }
    }

    class Sustain(
      var change: Change
    ) : Element() {
      constructor(element: Element): this(when(element) {
        is Sustain -> element.change
        is Change -> element
      })

      override var chord: Chord
      set(value) { change.chord = value }
      get() = change.chord
      override fun transpose(interval: Int): Element {
        return Sustain(change.transpose(interval))
      }
    }

  }
}