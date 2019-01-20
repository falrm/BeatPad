import com.jonlatane.beatpad.model.*
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.model.melody.RationalMelody
import com.jonlatane.beatpad.output.service.convertPatternIndex
import com.jonlatane.beatpad.output.service.let
import com.jonlatane.beatpad.util.to127Int
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import kotlinx.io.pool.DefaultPool
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.verbose
import java.lang.Math.max
import java.util.*
import kotlin.math.floor
import kotlin.properties.Delegates.observable

object BeatClockPaletteConsumer : AnkoLogger {
  var palette: Palette? = null
  var viewModel: PaletteViewModel? by observable(null) { _, _, _ ->

  }
  var section: Section? = null
  set(value) {
    field = value
    viewModel?.notifySectionChange()
  }
  var chord: Chord? = null
  val harmony: Harmony? get() = section?.harmony
  private val harmonyPosition: Int?
    get() = harmony?.let { tickPosition.convertPatternIndex(ticksPerBeat, it) }
  private val harmonyChord: Chord?
    get() = (harmony to harmonyPosition).let { harmony, harmonyPosition ->
      harmony.changeBefore(harmonyPosition)
    }
  var tickPosition: Int = 0 // Always relative to ticksPerBeat
  var ticksPerBeat = 24 // Mutable so you can use, say, 36, to play beats against
  // input dotted-quarters

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

    var currentAttackIndex = 0
    chord = (harmonyChord ?: chord)?.also { chord ->
      viewModel?.orbifold?.post {
        if (
          section.harmony != null
          && viewModel?.harmonyViewModel?.isChoosingHarmonyChord != true
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
        val attack = attackPool.borrow()

        if (
          true == (melody as? RationalMelody)
            ?.populateAttack(part, chord, attack, melodyReference.volume)
        ) {
          currentAttackIndex++
          upcomingAttacks += attack
        } else {
          attackPool.recycle(attack)
        }
      }
    }
  }

  fun tick() {
    (palette to section).let { palette: Palette, section: Section ->
      val enabledMelodies = section.melodies.map { it.melody }
      val totalBeats = enabledMelodies
        .map { it.length.toFloat() / it.subdivisionsPerBeat.toFloat() }
        .reduce(::max)
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
   * [currentBeat] could be, for instance, 1.2.
   * That would be be at subdivision 5 (index 4) at 4 subdivisions per beat.
   */
  private fun RationalMelody.populateAttack(
    part: Part,
    chord: Chord?,
    attack: Attack,
    volume: Float
  ): Boolean {
    val offset = chord?.let { offsetUnder(it) } ?: 0
    val currentBeat: Double = tickPosition.toDouble() / ticksPerBeat
    val melodyLength: Double = length.toDouble() / subdivisionsPerBeat
    val positionInMelody: Double = currentBeat % melodyLength

    // This candidate for attack is the closest element index to the current tick
    val indexCandidate = floor(positionInMelody * subdivisionsPerBeat).toInt()
    val realIndexPosition = indexCandidate.toDouble() / subdivisionsPerBeat

    // Now, is the previous or next tick closer to this element index's real value?
    fun distance(tickOffset: Double): Double = Math.abs(
      positionInMelody + (tickOffset / ticksPerBeat) - realIndexPosition
    )

    val thisTickDistance = distance(0.0)
    val nextTickDistance = distance(1.0)
    val previousTickDistance = distance(-1.0)
    return when {
      thisTickDistance < nextTickDistance && thisTickDistance < previousTickDistance -> {
        when {
          isChangeAt(indexCandidate) -> {
            val change = changeBefore(indexCandidate)
            attack.part = part
            attack.instrument = part.instrument
            attack.melody = this
            attack.velocity = change.velocity * volume

            change.tones.forEach { tone ->
              val transposedTone = tone + offset
              val chosenTone = chord?.closestTone(transposedTone)
                ?: transposedTone
              attack.chosenTones.add(chosenTone)
            }

            true
          }
          else -> false
        }
      }
      else -> false
    }
  }
}
