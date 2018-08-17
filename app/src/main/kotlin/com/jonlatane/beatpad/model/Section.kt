package com.jonlatane.beatpad.model

import java.util.*

data class Section(
  val id: UUID = UUID.randomUUID(),
  val name: String = generateNewSectionName(emptyList()),
  val harmony: Harmony = Harmony(),
  val melodies: MutableSet<Melody<*>> = mutableSetOf()
) {
  companion object {
    fun forList(
      sectionList: List<Section>,
      harmony: Harmony = Harmony(),
      melodies: MutableSet<Melody<*>> = mutableSetOf()
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