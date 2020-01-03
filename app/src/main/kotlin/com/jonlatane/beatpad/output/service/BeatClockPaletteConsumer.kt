import android.annotation.SuppressLint
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.midi.AndroidMidi
import com.jonlatane.beatpad.midi.MidiDevices
import com.jonlatane.beatpad.model.*
import com.jonlatane.beatpad.model.chord.Chord
import com.jonlatane.beatpad.model.dsl.Patterns
import com.jonlatane.beatpad.model.melody.RationalMelody
import com.jonlatane.beatpad.output.service.Base24ConversionMap
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.util.to127Int
import com.jonlatane.beatpad.view.palette.BeatScratchToolbar
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import com.jonlatane.beatpad.view.palette.SectionHolder
import io.multifunctions.letCheckNull
import kotlinx.io.pool.DefaultPool
import org.jetbrains.anko.*
import java.util.*
import kotlin.math.abs
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
    viewModel?.staffConfiguration?.soloPart = null
    tickPosition = 0
  }
  enum class PlaybackMode { SECTION, PALETTE }
  var playbackMode = PlaybackMode.PALETTE
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
  private val outgoingAttacks = Vector<Attack>(16)
  private val upcomingAttacks = Vector<Attack>(16)

  private fun loadUpcomingAttacks(palette: Palette, section: Section) {
    chord = (harmonyChord ?: chord)?.also { chord ->
      doAsync {
        viewModel?.apply {
          if (
            !harmonyViewModel.isChoosingHarmonyChord && chord != orbifold.chord
          ) {
            orbifold.disableNextTransitionAnimation()
            //viewModel?.orbifold?.prepareAnimationTo(chord)
            uiThread {
              orbifold.chord = chord
            }
          }
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
        (melody as? RationalMelody)
          ?.attackForCurrentTickPosition(part, chord, melodyReference.volume)?.let {
            upcomingAttacks += it
          }
      }
    }
  }

  private fun cleanUpExpiredAttacks() {
    activeAttacks.forEach { attack ->
      val attackCameFromRunningMelody = section?.melodies
        ?.filter { !it.isDisabled }
        ?.map { it.melody }
        ?.contains(attack.melody) ?: false
      if (!attackCameFromRunningMelody) {
        //info("stopping active attack $attack")
        outgoingAttacks.add(attack)
      }
    }
    outgoingAttacks.forEach { attack ->
      destroyAttack(attack)
    }
    outgoingAttacks.clear()
  }

  private val _activeAttacksCopy = Vector<Attack>()
  private fun RationalMelody.stopCurrentAttacks() {
    _activeAttacksCopy.clear()
    _activeAttacksCopy.addAll(activeAttacks)
    for (activeAttack in _activeAttacksCopy) {
      if (activeAttack.melody == this) {
        //verbose { "Ending attack $activeAttack" }
        destroyAttack(activeAttack)
        break
      }
    }
  }

  private fun getNextSection(): Section {
    var isNextSection = false
    var nextSection: Section? = null
    loop@ for(candidate in palette!!.sections) {
      when {
        candidate === section -> isNextSection = true
        isNextSection        -> { nextSection = candidate; break@loop }
      }
    }
    return (nextSection ?: palette!!.sections.first()).let {
      it
    }
  }

  fun tick() {
    (palette to section).letCheckNull { palette: Palette, section: Section ->
      val totalBeats = harmony?.let { it.length.toFloat() / it.subdivisionsPerBeat } ?: 0f
      loadUpcomingAttacks(palette, section)
      cleanUpExpiredAttacks()
      for (attack in upcomingAttacks) {
        val instrument = attack.instrument!!
        (attack.melody as? RationalMelody)?.stopCurrentAttacks()

        // And play the new notes

//        info("Executing attack $attack")

        attack.chosenTones.forEach { tone ->
          instrument.play(tone, attack.velocity.to127Int)
        }
        activeAttacks += attack
      }
      if ((tickPosition + 1) / ticksPerBeat >= totalBeats) {
        if(playbackMode == PlaybackMode.PALETTE) {
          val nextSection = getNextSection()
          tickPosition = 0
          this.section = nextSection
        } else {
          tickPosition = 0
        }
      } else {
        tickPosition += 1
      }
    } ?: warn("Tick called with no Palette available")

    upcomingAttacks.clear()

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
  private fun RationalMelody.attackForCurrentTickPosition(
    part: Part,
    chord: Chord?,
    volume: Float
  ): Attack? {
    return Base24ConversionMap[subdivisionsPerBeat]?.indexOf(tickPosition % ticksPerBeat)?.takeIf { it >=0 }?.let { correspondingPosition ->
      val currentBeat = tickPosition / ticksPerBeat
      val melodyPosition = currentBeat * subdivisionsPerBeat + correspondingPosition
      val attack = attackPool.borrow()
      when {
        isChangeAt(melodyPosition % length) -> {
          val change = changeBefore(melodyPosition % length)
          attack.part = part
          attack.instrument = part.instrument
          attack.melody = this
          attack.velocity = change.velocity * volume

          change.tones.forEach { tone ->
            val playbackTone = chord?.let { chord -> playbackToneUnder(tone, chord) } ?: tone
            attack.chosenTones.add(playbackTone)
          }
//          info("creating attack for melody=${this.hashCode()} tick=$tickPosition correspondingPosition=$correspondingPosition subdivision=$melodyPosition/$subdivisionsPerBeat beat=$currentBeat with tones ${attack.chosenTones}")
          attack
        }
        else                                       -> null
      }
    }
  }
}
