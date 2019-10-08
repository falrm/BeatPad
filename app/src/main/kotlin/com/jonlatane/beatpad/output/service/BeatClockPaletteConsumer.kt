import android.annotation.SuppressLint
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.midi.AndroidMidi
import com.jonlatane.beatpad.midi.MidiDevices
import com.jonlatane.beatpad.model.*
import com.jonlatane.beatpad.model.chord.Chord
import com.jonlatane.beatpad.model.dsl.Patterns
import com.jonlatane.beatpad.model.melody.RationalMelody
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.util.to127Int
import com.jonlatane.beatpad.view.palette.BeatScratchToolbar
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import com.jonlatane.beatpad.view.palette.SectionHolder
import io.multifunctions.letCheckNull
import kotlinx.io.pool.DefaultPool
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.verbose
import java.util.*
import kotlin.math.floor
import kotlin.properties.Delegates.observable

/**
 * A platform-agnostic model for a playback thread that plays back [Section],
 * [Melody] and [Harmony] data as the end-user would expect.
 */
object BeatClockPaletteConsumer : Patterns, AnkoLogger {
  var palette: Palette? = null
  set(value) {
    field = value
    MidiDevices.refreshInstruments()
    tickPosition = 0
  }
  enum class PlaybackMode { SECTION, PALETTE }
  var playbackMode = PlaybackMode.SECTION
  @SuppressLint("StaticFieldLeak")
  var viewModel: PaletteViewModel? = null
  var section: Section? = null
  set(value) {
    field = value
    viewModel?.melodyView?.post {
      viewModel?.notifySectionChange()
    }
    //MidiDevices.refreshInstruments()
  }
  val currentSectionDrawable: Int get() = palette?.sections
    ?.indexOf(section)?.let { sectionIndex ->
      SectionHolder.sectionDrawableResource(sectionIndex)
    } ?: R.drawable.orbifold_chord

  val currentSectionColor: Int
    get() = (
      palette?.sections?.indexOf(section)?.let { SectionHolder.sectionColor(it) }
        ?: R.color.subDominant
      ).let { MainApplication.instance.color(it) }

  private var chord: Chord? = null
  val harmony: Harmony? get() = section?.harmony
  private val harmonyPosition: Int?
    get() = harmony?.let { tickPosition.convertPatternIndex(ticksPerBeat, it) }
  private val harmonyChord: Chord?
    get() = (harmony to harmonyPosition).letCheckNull { harmony, harmonyPosition ->
      harmony.changeBefore(harmonyPosition)
    }
  var tickPosition: Int = 0 // Always relative to ticksPerBeat
  const val ticksPerBeat = 24 // MIDI standard is pretty clear about this

  private data class Attack(
    var part: Part? = null,
    var instrument: Instrument? = null,
    var melody: Melody<*>? = null,
    var chosenTones: MutableList<Int> = Vector(16),
    var velocity: Float = 1f
  )

  private val attackPool: DefaultPool<Attack> = object : DefaultPool<Attack>(16) {
    override fun produceInstance() = Attack()
    override fun clearInstance(instance: Attack): Attack = instance.apply { chosenTones.clear() }
  }
  private val activeAttacks = Vector<Attack>(16)
  private val upcomingAttacks = Vector<Attack>(16)

  private fun loadUpcomingAttacks(palette: Palette, section: Section) {
    chord = (harmonyChord ?: chord)?.also { chord ->
      viewModel?.orbifold?.post {
        if (
          viewModel?.harmonyViewModel?.isChoosingHarmonyChord != true
          && chord != viewModel?.orbifold?.chord
        ) {
          viewModel?.orbifold?.disableNextTransitionAnimation()
          //viewModel?.orbifold?.prepareAnimationTo(chord)
          viewModel?.orbifold?.chord = chord
        }
      }
    }
    verbose { "Harmony index: $harmonyPosition; Chord: $chord" }
    palette.parts.map { part ->
      section.melodies.filter {
        it.playbackType != Section.PlaybackType.Disabled
          && part.melodies.contains(it.melody)
      }.forEach { melodyReference ->
        val melody = melodyReference.melody
        upcomingAttacks += (melody as? RationalMelody)
          ?.attacksForCurrentTickPosition(part, chord, melodyReference.volume)
          ?: emptyList()
      }
    }
  }

  fun tick() {
    (palette to section).letCheckNull { palette: Palette, section: Section ->
      val totalBeats = harmony?.let { it.length.toFloat() / it.subdivisionsPerBeat } ?: 0f
      loadUpcomingAttacks(palette, section)
      for (attack in upcomingAttacks) {
        val instrument = attack.instrument!!
        // Stop current notes from this attack's melody
        for (activeAttack in activeAttacks) {
          if (activeAttack.melody == attack.melody) {
            verbose { "Ending attack $activeAttack" }
            destroyAttack(activeAttack)
            break
          }
        }
        // And play the new notes

        verbose { "Executing attack $attack" }

        attack.chosenTones.forEach { tone ->
          instrument.play(tone, attack.velocity.to127Int)
        }
        activeAttacks += attack
      }
      if ((tickPosition + 1) / ticksPerBeat >= totalBeats) {
        tickPosition = 0
        if(playbackMode == PlaybackMode.PALETTE) {
          var isNextSection = false
          var nextSection: Section? = null
          loop@ for(candidate in palette.sections) {
            when {
              candidate === section -> isNextSection = true
              isNextSection        -> { nextSection = candidate; break@loop }
            }
          }
          (nextSection ?: palette.sections.first()).let {
            BeatClockPaletteConsumer.section = it
          }
        }
      } else {
        tickPosition += 1
      }
    } ?: info("Tick called with no Palette available")

    upcomingAttacks.clear()
    // Clean up expired attacks
    activeAttacks.forEach { attack ->
      val attackCameFromRunningMelody = section?.melodies
        ?.filter { !it.isDisabled }
        ?.map { it.melody }
        ?.contains(attack.melody) ?: false
      if (!attackCameFromRunningMelody) {
        info("stopping active attack $attack")
        destroyAttack(attack)
      }
    }

    AndroidMidi.flushSendStream()
    viewModel?.harmonyView?.post { viewModel?.playbackTick = tickPosition }
  }

  internal fun clearActiveAttacks() {
    for (activeAttack in listOf(*activeAttacks.toArray(arrayOf<Attack>()))) {
      destroyAttack(activeAttack)
    }
  }

  @Synchronized
  private fun destroyAttack(attack: Attack) {
    attack.chosenTones.forEach { tone ->
      attack.instrument!!.stop(tone)
    }
    attackPool.recycle(attack)
    activeAttacks.remove(attack)
  }

  //private fun Melody<*>.

  /**
   * Based on the current [tickPosition], populates the passed [Attack] object.
   * Returns true if the attack should be played.
   */
  private fun RationalMelody.attacksForCurrentTickPosition(
    part: Part,
    chord: Chord?,
    volume: Float
  ): List<Attack> {
    val currentBeat: Double = tickPosition.toDouble() / ticksPerBeat
    val melodyLength: Double = length.toDouble() / subdivisionsPerBeat
    val positionInMelody: Double = currentBeat % melodyLength

    // This candidate for attack is the closest element index to the current tick
    val indexCandidates = floor(positionInMelody * subdivisionsPerBeat).toInt().let {
      listOf(it, it + 1)
    }
    val indexHits = indexCandidates.filter { indexCandidate ->

      val realIndexPosition = indexCandidate.toDouble() / subdivisionsPerBeat

      // Now, is the previous or next tick closer to this element index's real value?
      fun distance(tickOffset: Double): Double = Math.abs(
        positionInMelody + (tickOffset / ticksPerBeat) - realIndexPosition
      )

      val thisTickDistance = distance(0.0)
      val nextTickDistance = distance(1.0)
      val previousTickDistance = distance(-1.0)
      thisTickDistance <= nextTickDistance && thisTickDistance < previousTickDistance
    }

    return indexHits.mapNotNull {
      val attack = attackPool.borrow()
      when {
        isChangeAt(it % length) -> {
          val change = changeBefore(it % length)
          attack.part = part
          attack.instrument = part.instrument
          attack.melody = this
          attack.velocity = change.velocity * volume

          change.tones.forEach { tone ->
            val playbackTone =  chord?.let { chord -> playbackToneUnder(tone, chord) } ?: tone
            attack.chosenTones.add(playbackTone)
          }
          attack
        }
        else -> null
      }
    }
  }
}
