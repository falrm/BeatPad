package com.jonlatane.beatpad.view

import android.content.Context
import com.jonlatane.beatpad.view.palette.PaletteViewModel

interface BaseConfiguration {
  val configurationContext: Context
  val viewModel: PaletteViewModel
}