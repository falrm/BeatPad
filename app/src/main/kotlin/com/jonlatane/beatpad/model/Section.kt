package com.jonlatane.beatpad.model

import java.util.*

data class Section(
  val id: UUID = UUID.randomUUID(),
  val name: String = generateNewSectionName(emptyList()),
  var harmony: Harmony? = null,
  val melodies: MutableSet<MelodyReference> = mutableSetOf()
) {
  sealed class PlaybackType {
    object Disabled: PlaybackType()
    object Indefinite: PlaybackType()
    class Repeat(val repetitions: Int): PlaybackType()
  }
  class MelodyReference(
    var melody: Melody<*>,
    var volume: Float,
    var playbackType: PlaybackType = PlaybackType.Indefinite
  )
  companion object {
    fun forList(
      sectionList: List<Section>,
      harmony: Harmony? = null,
      melodies: MutableSet<MelodyReference> = mutableSetOf()
    ) = Section(
      name = generateNewSectionName(sectionList),
      harmony = harmony,
      melodies = melodies
    )

    fun generateNewSectionName(sectionList: List<Section>) = "Section ${(1..100).firstOrNull { index ->
      sectionList.none { it.name == "Section $index" }
    }?.toString() ?: ":)"}"
  }
}