package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.model.harmony.chord.Chord
import java.util.*

data class Harmony(
  // TODO: Is having [tonic] in a Pattern always good?
  override var tonic: Int = 0,
  override val changes: NavigableMap<Int, Harmony.Element> = TreeMap(),
  override var length: Int = 1,
  override var subdivisionsPerBeat: Int = 1
) : Pattern<Harmony.Element> {

  override fun transpose(interval: Int) = Harmony(
    tonic,
    TreeMap(changes.mapValues { it.value.transpose(interval) }),
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

    class NoChange(
      var change: Change
    ) : Element() {
      constructor(element: Element): this(when(element) {
        is NoChange -> element.change
        is Change -> element
      })

      override var chord: Chord
        set(value) { change.chord = value }
        get() = change.chord

      override fun transpose(interval: Int): Element {
        return NoChange(change.transpose(interval))
      }
    }

  }
}