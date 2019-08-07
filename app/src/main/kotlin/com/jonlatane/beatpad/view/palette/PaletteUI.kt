package com.jonlatane.beatpad.view.palette

//import com.jonlatane.beatpad.util.syncPositionTo
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.jonlatane.beatpad.PaletteEditorActivity
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.util.smartrecycler.firstVisibleItemPosition
import com.jonlatane.beatpad.view.colorboard.colorboardView
import com.jonlatane.beatpad.view.harmony.harmonyView
import com.jonlatane.beatpad.view.keyboard.keyboardView
import com.jonlatane.beatpad.view.melody.BeatAdapter
import com.jonlatane.beatpad.view.melody.melodyView
import com.jonlatane.beatpad.view.orbifold.OrbifoldView
import com.jonlatane.beatpad.view.orbifold.orbifoldView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onLayoutChange
import java.util.concurrent.atomic.AtomicBoolean

class PaletteUI constructor(
  val viewModel: PaletteViewModel
) : AnkoComponent<PaletteEditorActivity>, AnkoLogger {
  var leftSideWidth: Int = 0
  var isTablet: Boolean = false
  lateinit var layout: RelativeLayout

  override fun createView(ui: AnkoContext<PaletteEditorActivity>): RelativeLayout = with(ui) {
    layout = relativeLayout {
      isTablet = ui.configuration.smallestScreenWidthDp > 600

      if (configuration.portrait) {
        portraitLayout()
      } else {
        leftSideWidth = dip(350f)
        landscapeLayout()
      }

      keyboardsLayout()

      viewModel.apply {
        orbifold.onChordChangedListener = { chord ->
          toolbarView.orbifoldText.text = chord.name
          toolbarView.orbifoldText.textColor = context.color(OrbifoldView.colorResourceFor(chord))
          val keyboardDrumTrack = (keyboardPart?.instrument as? MIDIInstrument)?.drumTrack == true
          if(!harmonyViewModel.isChoosingHarmonyChord) {
            colorboardView.chord = chord
            if(!keyboardDrumTrack)
              keyboardView.ioHandler.highlightChord(chord)
            //viewModel.melodyViewModel.verticalAxis?.chord = chord
            splatController?.tones = chord.getTones()
            palette.chord = chord
            melodyViewModel.redraw()
            //BeatClockPaletteConsumer.chord = chord
          } else {
            colorboardView.chord = chord
            if(!keyboardDrumTrack) {
              keyboardView.ioHandler.highlightChord(chord)
            }
            //viewModel.melodyViewModel.verticalAxis?.chord = chord
            splatController?.tones = chord.getTones()
            harmonyViewModel.editingChord = chord
          }
        }
        orbifold.onOrbifoldChangeListener = { viewModel.palette.orbifold = it }
        orbifold.keyboard = viewModel.keyboardView
        hideOrbifold(false)
        keyboardView.hide(false)
        colorboardView.hide(false)
      }

      onLayoutChange { _, _, _, _, _, _, _, _, _ ->
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

//        viewModel.keyboardView.hide(false)
//        viewModel.colorboardView.hide(false)

        // Some tasty un-threadsafe spaghetti for syncing the two RecyclerViews for Harmony and Melody
        val inScrollingStack = AtomicBoolean(false)
        val melodyRecycler = viewModel.melodyViewModel.melodyRecyclerView
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
    viewModel.beatScratchToolbar = beatScratchToolbar(viewModel = viewModel) {
      id = View.generateViewId()
      orientation = LinearLayout.HORIZONTAL
    }.lparams {
      elevation = 5f
      width = matchParent
      //TODO: re-enable the toolbar
      height = wrapContent
//      height = 0
      alignParentTop()
    }

    viewModel.sectionListRecyclerHorizontal = sectionListView(viewModel = viewModel) {
      id = R.id.chord_list
    }.lparams {
      below(viewModel.beatScratchToolbar)
      elevation = 5f
      width = matchParent
      height = wrapContent
    }

    viewModel.harmonyView = harmonyView(viewModel = viewModel) {
      id = R.id.harmony
    }.lparams {
      below(viewModel.sectionListRecyclerHorizontal)
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

  private fun _RelativeLayout.landscapeLayout() {

    viewModel.sectionListRecyclerVertical = sectionListView(viewModel = viewModel, orientation = LinearLayoutManager.VERTICAL) {
      id = R.id.chord_list
    }.lparams {
      width = dip(200f)
      height = matchParent
      alignParentLeft()
      alignParentTop()
    }

    viewModel.beatScratchToolbar = beatScratchToolbar(viewModel = viewModel) {
      id = View.generateViewId()
      orientation = LinearLayout.VERTICAL
    }.lparams {
      //TODO: re-enable the toolbar
      width = dip(48)
//      width = 0
      height = matchParent
      rightOf(viewModel.sectionListRecyclerVertical)
      alignParentTop()
    }

    viewModel.toolbarView = paletteToolbar(viewModel = viewModel) {
      id = R.id.toolbar
      orientation = LinearLayout.VERTICAL
    }.lparams {
      width = dip(48)
      height = matchParent
      rightOf(viewModel.beatScratchToolbar)
      alignParentTop()
      alignParentBottom()

    }

    viewModel.harmonyView = harmonyView(
      viewModel = viewModel,
      recyclerLayoutParams = { bottomMargin = dip(10) }
    ) {
      id = R.id.harmony
    }.lparams {
      width = matchParent
      height = wrapContent
      rightOf(viewModel.toolbarView)
      alignParentTop()
      alignParentRight()
    }

    viewModel.partListView = partListView(viewModel = viewModel) {
      id = R.id.part_list
    }.lparams {
      width = matchParent
      height = wrapContent
      alignParentBottom()
      rightOf(viewModel.toolbarView)
      below(viewModel.harmonyView)
      alignParentRight()
    }

    viewModel.partListTransitionView = textView {
      id = View.generateViewId()
      textSize = 25f
      background = context.getDrawable(R.drawable.orbifold_chord)
    }.lparams(0, dip(40)) {
      rightOf(viewModel.toolbarView)
      below(viewModel.harmonyView)
    }

    viewModel.melodyView = melodyView(viewModel = viewModel) {
      id = R.id.melody
      alpha = 0f
    }.lparams {
      width = matchParent
      height = wrapContent
      alignParentBottom()
      rightOf(viewModel.toolbarView)
      below(viewModel.harmonyView)
      alignParentRight()
    }
  }

  private fun _RelativeLayout.keyboardsLayout() = with(context) {
    if(configuration.landscape) {
      viewModel.orbifold = orbifoldView {
        id = R.id.orbifold
      }.lparams {
        alignParentLeft()
        alignParentBottom()

        width = leftSideWidth
        height = dip(300)
        elevation = 5f
      }
    }

    viewModel.keyboardView = keyboardView {
      id = R.id.keyboard
      elevation = 10f
      //alpha = 0f
      //translationY = dimen(R.dimen.key_height_white).toFloat()
    }.lparams {
      if(configuration.landscape) rightOf(viewModel.orbifold)
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
      if(configuration.landscape) rightOf(viewModel.orbifold)
      height = dimen(R.dimen.key_height_white)
      width = matchParent
      above(viewModel.keyboardView)
    }

    if(configuration.portrait) {
      viewModel.orbifold = orbifoldView {
        id = R.id.orbifold
      }.lparams {
        above(viewModel.colorboardView)
        width = matchParent
        height = dip(210f)
      }
    }
  }
}
