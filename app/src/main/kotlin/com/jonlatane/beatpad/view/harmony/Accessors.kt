package com.jonlatane.beatpad.view.harmony

import android.view.ViewManager
import android.widget.RelativeLayout
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.custom.ankoView


fun ViewManager.harmonyView(
  viewModel: PaletteViewModel,
  theme: Int = 0,
  init: HarmonyView.() -> Unit = {}
): HarmonyView = ankoView({ HarmonyView(it, viewModel) }, theme, init)