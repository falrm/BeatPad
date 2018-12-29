package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.jonlatane.beatpad.PaletteEditorActivity
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.util.firstVisibleItemPosition
//import com.jonlatane.beatpad.util.syncPositionTo
import com.jonlatane.beatpad.view.colorboard.colorboardView
import com.jonlatane.beatpad.view.harmony.harmonyView
import com.jonlatane.beatpad.view.keyboard.keyboardView
import com.jonlatane.beatpad.view.melody.BeatAdapter
import com.jonlatane.beatpad.view.melody.melodyView
import com.jonlatane.beatpad.view.orbifold.orbifoldView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onLayoutChange
import java.util.concurrent.atomic.AtomicBoolean

class PaletteUI : AnkoComponent<PaletteEditorActivity>, AnkoLogger {
  val viewModel = PaletteViewModel()
  lateinit var layout: RelativeLayout

  override fun createView(ui: AnkoContext<PaletteEditorActivity>) = with(ui) {

    layout = relativeLayout {
      if (configuration.portrait) {
        portraitLayout()
      } else {
        landscapeLayout(this@with)
      }

      keyboardsLayout()

      viewModel.orbifold.onChordChangedListener = { chord ->
        if(!viewModel.harmonyViewModel.isEditingChord) {
          viewModel.colorboardView.chord = chord
          viewModel.keyboardView.ioHandler.highlightChord(chord)
          viewModel.melodyViewModel.verticalAxis?.chord = chord
          viewModel.splatController?.tones = chord.getTones()
          viewModel.palette.chord = chord
          viewModel.melodyViewModel.redraw()
          BeatClockPaletteConsumer.chord = chord
        } else {
          viewModel.harmonyViewModel.editingChord = chord
        }
      }

      viewModel.orbifold.onOrbifoldChangeListener = { viewModel.palette.orbifold = it }

      onLayoutChange { view: View?, i: Int, i1: Int, i2: Int, i3: Int, i4: Int, i5: Int, i6: Int, i7: Int ->
        if (viewModel.editingMelody == null) {
          viewModel.melodyView.translationX = viewModel.melodyView.width.toFloat()
        }
      }

      post {
        viewModel.partListView.animate()
          .alpha(1f)
          .start()
        viewModel.melodyView.animate()
          .translationX(viewModel.melodyView.width.toFloat())
          .withEndAction { viewModel.melodyView.alpha = 1f }
          .start()

        // Some tasty un-threadsafe spaghetti for syncing the two RecyclerViews for Harmony and Melody
        val inScrollingStack = AtomicBoolean(false)
        val melodyRecycler = viewModel.melodyViewModel.melodyCenterHorizontalScroller
        val harmonyRecycler = viewModel.harmonyViewModel.harmonyElementRecycler!!
        melodyRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
          override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (!inScrollingStack.getAndSet(true)) {
              verbose { "onScrolled in melody: ${recyclerView.firstVisibleItemPosition}, ${recyclerView.computeHorizontalScrollOffset()}" }
              (melodyRecycler.adapter as BeatAdapter).syncPositionTo(harmonyRecycler)
              viewModel.harmonyViewModel.harmonyView?.post {
                viewModel.harmonyViewModel.harmonyView?.syncScrollingChordText()
              }
            }
            post {
              inScrollingStack.set(false)
            }
          }
        })
        harmonyRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
          override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (!inScrollingStack.getAndSet(true)) {
              verbose { "onScrolled in harmony: ${recyclerView.firstVisibleItemPosition}, ${recyclerView.computeHorizontalScrollOffset()}" }
              // For some reason syncing scrolling this way makes things choppy.
              // This "if" statement is a hacky optimization around an ostensible Android bug
              // (or, more likely, other bug in my code). Performance impact is still seen
              // if you scroll the *harmony* while the *melody* is open.
              if(viewModel.editingMelody != null) {
                (harmonyRecycler.adapter as BeatAdapter).syncPositionTo(melodyRecycler)
              }
              viewModel.harmonyViewModel.harmonyView?.post {
                viewModel.harmonyViewModel.harmonyView?.syncScrollingChordText()
              }
            }
            post {
              inScrollingStack.set(false)
            }
          }
        })
      }
    }
    layout
  }

  private fun _RelativeLayout.portraitLayout() {
    viewModel.orbifold = orbifoldView {
      id = R.id.orbifold
    }.lparams {
      width = matchParent
      height = dip(210f)
      alignParentTop()
    }

    viewModel.sectionListView = sectionListView(viewModel = viewModel) {
      id = R.id.chord_list
    }.lparams {
      below(viewModel.orbifold)
      elevation = 5f
      width = matchParent
      height = wrapContent
    }

    viewModel.harmonyView = harmonyView(viewModel = viewModel) {
      id = R.id.harmony
    }.lparams {
      below(viewModel.sectionListView)
      width = matchParent
      height = wrapContent
    }

    viewModel.toolbarView = paletteToolbar(viewModel = viewModel) {
      id = R.id.toolbar
    }.lparams {
      below(viewModel.harmonyView)
      width = matchParent
      height = wrapContent
    }

    viewModel.partListView = partListView(viewModel = viewModel) {
      id = R.id.part_list
    }.lparams {
      below(viewModel.toolbarView)
      width = matchParent
      height = wrapContent
      alignParentBottom()
    }

    viewModel.partListTransitionView = textView {
      id = View.generateViewId()
      textSize = 25f
      background = context.getDrawable(R.drawable.orbifold_chord)
    }.lparams(dip(30), dip(40)) {
      below(viewModel.toolbarView)
      alignParentLeft()
    }

    viewModel.melodyView = melodyView(viewModel = viewModel) {
      id = R.id.melody
      alpha = 0f
    }.lparams {
      below(viewModel.toolbarView)
      width = matchParent
      height = wrapContent
      alignParentBottom()
    }
  }

  private fun _RelativeLayout.landscapeLayout(ui: AnkoContext<PaletteEditorActivity>) {
    val leftSideWidth = dip(350f)

    viewModel.sectionListView = sectionListView(viewModel = viewModel) {
      id = R.id.chord_list
    }.lparams {
      width = leftSideWidth
      height = wrapContent
      alignParentLeft()
      alignParentTop()
    }

    val isTablet = ui.configuration.smallestScreenWidthDp > 600

    viewModel.orbifold = orbifoldView {
      id = R.id.orbifold
    }.lparams {
      alignParentLeft()
      below(viewModel.sectionListView)
      width = leftSideWidth
      height = if (isTablet) {
        Math.round(leftSideWidth * 1.5f)
      } else {
        matchParent
      }
      elevation = 5f
    }

    if (isTablet) {
      view {
        id = R.id.spacer
        backgroundColor = context.color(R.color.colorPrimaryDark)
      }.lparams {
        alignParentLeft()
        below(viewModel.orbifold)
        width = leftSideWidth
        height = matchParent
        elevation = 5f
      }
    }

    viewModel.toolbarView = paletteToolbar(viewModel = viewModel) {
      id = R.id.toolbar
    }.lparams {
      width = matchParent
      height = wrapContent
      rightOf(viewModel.orbifold)
      alignParentTop()
      alignParentRight()

    }

    viewModel.harmonyView = harmonyView(viewModel = viewModel) {
      id = R.id.harmony
    }.lparams {
      width = matchParent
      height = wrapContent
      rightOf(viewModel.orbifold)
      below(viewModel.toolbarView)
      alignParentRight()
    }

    viewModel.partListView = partListView(viewModel = viewModel) {
      id = R.id.part_list
    }.lparams {
      width = matchParent
      height = wrapContent
      alignParentBottom()
      rightOf(viewModel.orbifold)
      below(viewModel.harmonyView)
      alignParentRight()
    }

    viewModel.partListTransitionView = textView {
      id = View.generateViewId()
      textSize = 25f
      background = context.getDrawable(R.drawable.orbifold_chord)
    }.lparams(0, dip(40)) {
      rightOf(viewModel.orbifold)
      below(viewModel.harmonyView)
    }

    viewModel.melodyView = melodyView(viewModel = viewModel) {
      id = R.id.melody
      alpha = 0f
    }.lparams {
      width = matchParent
      height = wrapContent
      alignParentBottom()
      rightOf(viewModel.orbifold)
      below(viewModel.harmonyView)
      alignParentRight()
    }
  }

  private fun _RelativeLayout.keyboardsLayout() {

    viewModel.keyboardView = keyboardView {
      id = R.id.keyboard
      elevation = 10f
      //alpha = 0f
      //translationY = dimen(R.dimen.key_height_white).toFloat()
    }.lparams {
      height = dimen(R.dimen.key_height_white)
      width = matchParent
      alignParentBottom()
    }

    viewModel.colorboardView = colorboardView {
      id = R.id.colorboard
      elevation = 10f
      //alpha = 0f
      backgroundColor = color(android.R.color.white)
      //translationY = dimen(R.dimen.key_height_white).toFloat()
    }.lparams {
      height = dimen(R.dimen.key_height_white)
      width = matchParent
      above(viewModel.keyboardView)
    }
  }
}
