package com.jonlatane.beatpad.view.library

import android.content.Context
import android.graphics.Color
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import org.jetbrains.anko.*
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.nestedScrollView


class LibraryViewPagerAdapter(
  val viewModel: LibraryViewModel,
  val context: Context
): PagerAdapter() {
  enum class ViewType(
    val title: String
  ) {
    PALETTE("Palettes"), MELODY("Melodies"), HARMONY("Harmonies")
  }

  override fun instantiateItem(collection: ViewGroup, position: Int): Any {
//    val customPagerEnum = ViewType.values()[position]
//    val inflater = LayoutInflater.from(storageContext)
//    val layout = inflater.inflate(customPagerEnum.getLayoutResId(), collection, false) as ViewGroup
//    collection.addView(layout)
    return when(position) {
      ViewType.PALETTE.ordinal -> {
        collection.run {
          nestedScrollView {
            relativeLayout {
              recyclerView {
                adapter = LibraryPaletteAdapter(viewModel, this)
                //backgroundColor = Color.RED
              }.lparams(matchParent, matchParent)
              floatingActionButton {
                imageResource = android.R.drawable.ic_input_add
              }.lparams {
                //setting button to bottom right of the screen
                margin = dip(10)
                alignParentBottom()
                alignParentEnd()
                alignParentRight()
                gravity = Gravity.BOTTOM or Gravity.END
              }
            }.lparams(matchParent, matchParent)
          }
        }
      }
      else -> blankView()
    }
  }

  private fun blankView() = viewModel.pager.run {
    view()
  }


  override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
    (view as? View)?.let { collection.removeView(it) }
  }

  override fun getCount() = ViewType.values().size
  override fun isViewFromObject(view: View, `object`: Any) =  view === `object`
  override fun getPageTitle(position: Int)  = ViewType.values()[position].title
}