package com.jonlatane.beatpad.view.harmony

import android.view.ViewManager
import org.jetbrains.anko.custom.ankoView


fun ViewManager.harmonyView(
  viewModel: HarmonyViewModel,
  theme: Int = 0,
  init: HarmonyView.() -> Unit = {}
) = ankoView({ HarmonyView(it, viewModel) }, theme, init)