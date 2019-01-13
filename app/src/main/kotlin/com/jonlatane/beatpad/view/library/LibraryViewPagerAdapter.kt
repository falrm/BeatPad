package com.jonlatane.beatpad.view.library

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.relativeLayout


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
//    val inflater = LayoutInflater.from(context)
//    val layout = inflater.inflate(customPagerEnum.getLayoutResId(), collection, false) as ViewGroup
//    collection.addView(layout)
    return when(position) {
      ViewType.PALETTE.ordinal -> {
        collection.run {
          addView(
          recyclerView {
            adapter = LibraryPaletteAdapter(viewModel, this)

          })/*.apply {
            layoutParams = layoutParams.apply { width = matchParent; height = matchParent }
          }*/
        }
      }
      else -> blankView()
    }
  }

  private fun blankView() = viewModel.pager.run {
  }


  override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
    collection.removeView(view as View)
  }

  override fun getCount() = ViewType.values().size
  override fun isViewFromObject(view: View, `object`: Any) =  view === `object`
  override fun getPageTitle(position: Int)  = ViewType.values()[position].title
}