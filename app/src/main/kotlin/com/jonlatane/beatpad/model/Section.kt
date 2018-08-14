package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.view.harmony.HarmonyViewModel.Companion.createBaseHarmony

data class Section(
  val name: String = generateNewSectionName(emptyList()),
  val harmony: Harmony = createBaseHarmony(),
  val melodies: MutableSet<Melody<*>> = mutableSetOf()
) {
  companion object {
    fun forList(
      sectionList: List<Section>,
      harmony: Harmony = createBaseHarmony(),
      melodies: MutableSet<Melody<*>> = mutableSetOf()
    ) = Section(generateNewSectionName(sectionList), harmony, melodies)

    fun generateNewSectionName(sectionList: List<Section>) = "Section ${(1..100).firstOrNull { index ->
      sectionList.none { it.name == "Section $index" }
    }?.toString() ?: ":)"}"
  }
}