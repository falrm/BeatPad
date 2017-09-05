package com.jonlatane.beatpad.view.tonesequence

import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.view.nonDelayedHorizontalScrollView
import com.jonlatane.beatpad.view.nonDelayedScrollView
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.sdk25.coroutines.onScrollChange


inline fun ViewManager.toneSequenceView(
  theme: Int = 0,
  viewModel: ToneSequenceViewModel,

  //ui: AnkoContext<Any>,
  init: _RelativeLayout.() -> Unit
)
  = //with(ui) {
  ankoView({
    _RelativeLayout(it).apply {
      var holdToEdit: TextView? = null
      var IDSeq = 1
      viewModel.bottomScroller = bottomScroller {
        id = IDSeq++
        onHeldDownChanged = { heldDown ->
          if (heldDown) holdToEdit?.animate()?.alpha(0f)?.translationY(100f)
          else holdToEdit?.animate()?.alpha(1f)?.translationY(0f)
        }
        linearLayout {
          orientation = LinearLayout.HORIZONTAL
          repeat(ToneSequenceUI.STEPS_TO_ALLOCATE) {
            viewModel.bottoms.add(
              view {
                background = context.getDrawable(R.drawable.tone_footer)
              }.lparams {
                width = dimen(R.dimen.subdivision_controller_size)
                height = dimen(R.dimen.subdivision_controller_size)
              }
            )
          }
        }
        scrollingEnabled = false
      }.lparams {
        alignParentBottom()
        alignParentRight()
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = dimen(R.dimen.subdivision_controller_size)
        leftMargin = dip(30)
      }
      holdToEdit = textView {
        text = "Hold To Edit"
        textSize = 15f
        gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
      }.lparams {
        alignParentBottom()
        alignParentRight()
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = dimen(R.dimen.subdivision_controller_size)
        leftMargin = dip(30)
      }
      viewModel.leftScroller = nonDelayedScrollView {
        id = IDSeq++
        linearLayout {
          viewModel.verticalAxis = toneSequenceAxis().lparams {
            width = dip(30)
            height = dip(1000f)
          }
        }
        scrollingEnabled = false
        isVerticalScrollBarEnabled = false
      }.lparams {
        width = dip(30)
        height = ViewGroup.LayoutParams.MATCH_PARENT
        above(viewModel.bottomScroller)
        alignParentLeft()
      }
      viewModel.centerVerticalScroller = nonDelayedScrollView {
        id = IDSeq++
        onScrollChange {
          _, _, scrollY, _, _ ->
          viewModel.leftScroller.scrollY = scrollY
        }
        viewModel.centerHorizontalScroller = nonDelayedHorizontalScrollView {
          onScrollChange {
            _, scrollX, _, _, _ ->
            viewModel.bottomScroller.scrollX = scrollX
          }
          linearLayout {
            orientation = LinearLayout.HORIZONTAL
            repeat(ToneSequenceUI.STEPS_TO_ALLOCATE) {
              viewModel.elements.add(toneSequenceElement {
                this.viewModel = viewModel
              }.lparams {
                width = dimen(R.dimen.subdivision_controller_size)
                height = dip(1000f)
              })
            }
          }
          isHorizontalScrollBarEnabled = false
        }
      }.lparams {
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.MATCH_PARENT
        alignParentRight()
        above(viewModel.bottomScroller)
        rightOf(viewModel.leftScroller)
        alignParentTop()
      }
    }
  }, theme, init)
//}

