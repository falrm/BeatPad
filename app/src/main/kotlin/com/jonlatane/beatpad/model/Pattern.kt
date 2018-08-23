package com.jonlatane.beatpad.model

interface Pattern<T : Transposable<T>> : Transposable<Pattern<T>> {
  val elements: MutableList<T>
  val subdivisionsPerBeat: Int
  var tonic: Int
}