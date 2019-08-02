package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.storage.PaletteStorage
import java.util.*

data class Section(
  val id: UUID = UUID.randomUUID(),
  var name: String = generateNewSectionName(emptyList()),
  var harmony: Harmony = PaletteStorage.blankHarmony,
  val melodies: MutableSet<MelodyReference> = mutableSetOf(),
  var relatedSections: MutableSet<UUID> = mutableSetOf()
) {
  sealed class PlaybackType {
    object Disabled: PlaybackType()
    object Indefinite: PlaybackType()
    class Repeat(val repetitions: Int): PlaybackType()
  }
  data class MelodyReference constructor(
    var melody: Melody<*>,
    var volume: Float =  0.5f,
    var playbackType: PlaybackType = PlaybackType.Indefinite,
    /**
     * Indicates the [Melody] should be played back (and displayed) with its notes transposed
     * up/down chromatically by this number of half steps (before chord matching behavior is applied).
     */
    var translateTones: Int = 0,
    /**
     * Indicates the [Melody] should be played back (and displayed) shifted forward or back,
     * in time, by the specified amount. Effectively, shifting the keys of [Melody.changes] by
     * this amount when accessed.
     */
    var translateTime: Int = 0
  ) {
    val isDisabled get() = playbackType == PlaybackType.Disabled
  }
  companion object {
    fun forList(
      sectionList: List<Section>,
      harmony: Harmony,
      melodies: MutableSet<MelodyReference> = mutableSetOf()
    ) = Section(
      name = generateNewSectionName(sectionList),
      harmony = harmony,
      melodies = melodies
    )

    fun generateNewSectionName(sectionList: List<Section>, basis: String = "Section "): String
      = generateNewName(sectionList.map { it.name }, basis)

    fun generateDuplicateSectionName(sectionList: List<Section>, basis: String = "Section "): String
      = generateDuplicateName(sectionList.map { it.name }, basis)

    fun generateNewName(sectionList: List<String>, basis: String = "Section "): String
      = "$basis${(1..100).firstOrNull { index ->
      sectionList.none { it == "$basis$index" }
    }?.toString() ?: "$basis:)"}"

    fun generateDuplicateName(others: List<String>, name: String): String {
      val candidateBasis = name.trimEnd(*('0'..'9').toList().toCharArray()).trimEnd()
      val basis = when {
        name.last().isDigit() -> when {
          others.any { it.startsWith(candidateBasis)} -> name + '-'
          else -> "$candidateBasis "
        }
        else -> "$candidateBasis "
      }
      return generateNewName(others, basis)
    }
  }
}