package com.jonlatane.beatpad.view.palette.filemanagement

import io.damo.aspen.Test

class PresetPalettesTest : Test({
  describe("Palette Presets") {
    test("load successfully") {
      PresetPalettes.values().map { it.palette }
    }
  }
})