package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.storage.PaletteStorage
import java.util.*

data class Section(
  val id: UUID = UUID.randomUUID(),
  var name: String = generateNewSectionName(emptyList()),
  var harmony: Harmony? = PaletteStorage.blankHarmony,
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