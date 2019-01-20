package com.jonlatane.beatpad.view.library

//import com.jonlatane.beatpad.util.syncPositionTo
import android.support.design.widget.AppBarLayout
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.Gravity
import android.view.View
import com.jonlatane.beatpad.LibraryActivity
import com.jonlatane.beatpad.R
import org.jetbrains.anko.*
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.themedTabLayout
import org.jetbrains.anko.support.v4.viewPager

class LibraryUI : AnkoComponent<LibraryActivity>, AnkoLogger {
  val viewModel = LibraryViewModel()
  lateinit var tabLayout: TabLayout

  override fun createView(ui: AnkoContext<LibraryActivity>) = with(ui) {

    viewModel.layout = coordinatorLayout {
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
      viewModel.pager = viewPager {
        id = View.generateViewId()
        adapter = LibraryViewPagerAdapter(viewModel, context)
      }.lparams(matchParent, matchParent) {
        behavior = AppBarLayout.ScrollingViewBehavior()
      }
    }
    tabLayout.setupWithViewPager(viewModel.pager)
    viewModel.layout
  }
}
