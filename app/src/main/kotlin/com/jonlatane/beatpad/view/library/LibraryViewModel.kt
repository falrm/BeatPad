package com.jonlatane.beatpad.view.library

import android.support.v4.view.ViewPager
import android.view.ViewGroup
import java.io.File

class LibraryViewModel {
  lateinit var layout: ViewGroup
  lateinit var pager: ViewPager
  lateinit var paletteAdapter: LibraryPaletteAdapter
  //var palettes: List<File>
}