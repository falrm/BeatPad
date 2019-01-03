package com.jonlatane.beatpad.view.library

import android.graphics.Color
import android.support.design.widget.AppBarLayout
import android.support.design.widget.AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
import android.support.design.widget.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.jonlatane.beatpad.LibraryActivity
import com.jonlatane.beatpad.PaletteEditorActivity
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.util.firstVisibleItemPosition
import com.jonlatane.beatpad.util.hide
//import com.jonlatane.beatpad.util.syncPositionTo
import com.jonlatane.beatpad.view.colorboard.colorboardView
import com.jonlatane.beatpad.view.harmony.harmonyView
import com.jonlatane.beatpad.view.keyboard.keyboardView
import com.jonlatane.beatpad.view.melody.BeatAdapter
import com.jonlatane.beatpad.view.melody.melodyView
import com.jonlatane.beatpad.view.orbifold.orbifoldView
import org.jetbrains.anko.*
import org.jetbrains.anko.design.*
import org.jetbrains.anko.sdk25.coroutines.onLayoutChange
import org.jetbrains.anko.support.v4.viewPager
import java.util.concurrent.atomic.AtomicBoolean

class LibraryUI : AnkoComponent<LibraryActivity>, AnkoLogger {
  val viewModel = LibraryViewModel()
  lateinit var viewPager: ViewPager
  lateinit var tabLayout: TabLayout
  lateinit var layout: ViewGroup

  override fun createView(ui: AnkoContext<LibraryActivity>) = with(ui) {

    layout = coordinatorLayout {
      lparams(matchParent, matchParent)

      appBarLayout {
        lparams(matchParent, wrapContent)

        tabLayout = themedTabLayout(R.style.ThemeOverlay_AppCompat_Dark) {
          lparams(matchParent, wrapContent)
          {
            tabGravity = Gravity.FILL
            tabMode = TabLayout.MODE_FIXED
          }
        }
      }
      viewPager = viewPager {
        id = View.generateViewId()
        adapter = LibraryViewPagerAdapter(this, context)
      }.lparams(matchParent, matchParent) {
        behavior = AppBarLayout.ScrollingViewBehavior()
      }
    }
    tabLayout.setupWithViewPager(viewPager)
    layout
  }
}
