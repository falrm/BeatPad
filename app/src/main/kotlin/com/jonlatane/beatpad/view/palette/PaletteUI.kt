package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.jonlatane.beatpad.PaletteEditorActivity
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.view.colorboard.colorboardView
import com.jonlatane.beatpad.view.harmony.harmonyView
import com.jonlatane.beatpad.view.keyboard.keyboardView
import com.jonlatane.beatpad.view.melody.melodyView
import com.jonlatane.beatpad.view.orbifold.orbifoldView
import org.billthefarmer.mididriver.GeneralMidiConstants
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onLayoutChange
import java.util.concurrent.Executors

class PaletteUI : AnkoComponent<PaletteEditorActivity>, AnkoLogger {
  private val executorService = Executors.newScheduledThreadPool(2)
  val viewModel = PaletteViewModel()
  val previewInstrument = MIDIInstrument().apply {
    channel = 4
    instrument = GeneralMidiConstants.SYNTH_BASS_1
  }
  val sequencerInstrument = MIDIInstrument().apply {
    channel = 5
    instrument = GeneralMidiConstants.SYNTH_BASS_1
  }


  override fun createView(ui: AnkoContext<PaletteEditorActivity>) = with(ui) {

    relativeLayout {
      if (configuration.portrait) {
        portraitLayout()
      } else {
        landscapeLayout(this@with)
      }

      keyboardsLayout()

      viewModel.orbifold.onChordChangedListener = { c: Chord ->
        val tones = c.getTones()
        viewModel.colorboardView.chord = c
        //viewModel.harmonyController.tones = tones
        viewModel.keyboardView.ioHandler.highlightChord(c)
        viewModel.verticalAxis?.chord = c
        viewModel.splatController?.tones = c.getTones()
        viewModel.palette.chord = c
        viewModel.redraw()
      }

      onLayoutChange { view: View?, i: Int, i1: Int, i2: Int, i3: Int, i4: Int, i5: Int, i6: Int, i7: Int ->
        if(viewModel.editingSequence == null) {
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
        viewModel.melodyCenterHorizontalScroller.addOnScrollListener(object: RecyclerView.OnScrollListener() {
          override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            info("onScrolled in melody: ${recyclerView.firstVisibleItemPosition}, ${recyclerView.computeHorizontalScrollOffset()}")
            val otherLayoutManager = viewModel.harmonyViewModel.harmonyElementRecycler!!.layoutManager as LinearLayoutManager
            val offset = -recyclerView.computeHorizontalScrollOffset() % (viewModel.melodyElementAdapter?.elementWidth
              ?: Int.MAX_VALUE)
            otherLayoutManager.scrollToPositionWithOffset(recyclerView.firstVisibleItemPosition, offset)
            viewModel.harmonyViewModel.harmonyView!!.syncScrollingChordText()
          }
        })
        viewModel.harmonyViewModel.harmonyElementRecycler?.addOnScrollListener(object: RecyclerView.OnScrollListener() {
          override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            info("onScrolled in harmony: ${recyclerView.firstVisibleItemPosition}, ${recyclerView.computeHorizontalScrollOffset()}")
            val otherLayoutManager = viewModel.melodyCenterHorizontalScroller.layoutManager as LinearLayoutManager
            val offset = -recyclerView.computeHorizontalScrollOffset() % (viewModel.melodyElementAdapter?.elementWidth
              ?: Int.MAX_VALUE)
            otherLayoutManager.scrollToPositionWithOffset(recyclerView.firstVisibleItemPosition, offset)
          }
        })
      }
    }
  }

  val RecyclerView.firstVisibleItemPosition
    get() = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

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
