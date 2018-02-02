import com.jonlatane.beatpad.model.*
import com.jonlatane.beatpad.util.mod12
import com.jonlatane.beatpad.util.to127Int
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.lang.Math.max
import kotlin.math.floor
import kotlin.properties.Delegates.observable

object BeatClockPaletteConsumer : AnkoLogger {
	var palette: Palette? = null
	var viewModel: PaletteViewModel? by observable(null) { _, _, _ ->

	}
	var tickPosition: Int = 0 // Always relative to ticksPerBeat
	var ticksPerBeat = 24 // Mutable so you can use, say, 36, to play beats against
	                      // input dotted-quarters

	private data class Attack(
		val instrument: Instrument,
	  val note: Note
	) {
		val chosenTones = mutableSetOf<Int>()
	}
	private val activeAttacks = mutableMapOf<Part, MutableMap<Melody, Attack>>()
	fun tick() {
		palette?.let { palette ->
			val enabledMelodies = palette.parts.flatMap { it.melodies }.filter { it.enabled }
			val totalBeats = enabledMelodies
				.map { it.elements.size.toFloat() / it.subdivisionsPerBeat.toFloat() }
				.reduce(::max)
			val attacks = palette.currentAttacks
			for((part, melodies) in attacks) {
				val activePartAttacks = activeAttacks.getOrPut(part) { mutableMapOf() }
				for((melody, attack) in melodies) {
					activePartAttacks.remove(melody)?.let { lastAttack ->
						lastAttack.chosenTones.forEach {
							part.instrument.stop(it)
						}
					}
					info("Attack: $attack")
					val melodyOffset = viewModel?.orbifold?.chord?.let { chord ->
						melody.offsetUnder(chord)
					} ?: 0
					attack.note.tones.forEach { tone ->
						val transposedTone = tone + melodyOffset
						val chosenTone = viewModel?.orbifold?.chord?.closestTone(transposedTone) ?: transposedTone
						attack.chosenTones.add(chosenTone)
						part.instrument.play(chosenTone, attack.note.velocity.to127Int)
					}
					activePartAttacks[melody] = attack
				}
			}
			if(tickPosition/ ticksPerBeat >= totalBeats) {
				tickPosition = 0
			} else {
				tickPosition += 1
			}
		} ?: info("Tick called with no Palette available")
	}

	internal fun clearActiveAttacks() {
		for((part, melodyAttacks) in activeAttacks) {
			for((_, attack) in melodyAttacks) {
				attack.chosenTones.forEach {
					part.instrument.stop(it)
				}
			}
		}
		activeAttacks.clear()
	}

	private val Palette.currentAttacks : Map<Part, Map<Melody, Attack>> get() {
		// [currentBeat] is beat 0, 0.04167 (1/24), ..., 0.25, ... 0.75, ..., 1, etc.
		// assuming we're using a MIDI 24-per-quarter clock.
		val currentBeat: Double = tickPosition.toDouble() / ticksPerBeat
		return parts.map { part ->
			part to part.melodies.filter { it.enabled }
				.mapNotNull {melody ->
					melody.attackAt(currentBeat, part.instrument)?.let { attack ->
						melody to attack
					}

				}.toMap()
		}.toMap()
	}

	private fun Melody.attackAt(currentBeat: Double, partInstrument: Instrument): Attack? {
		val melodyLength: Double = elements.size.toDouble() / subdivisionsPerBeat
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
			thisTickDistance < nextTickDistance && thisTickDistance < previousTickDistance ->
			{
				val step = elements[indexCandidate]
				when (step) {
					is Melody.Element.Note -> {
						Attack(
							instrument = partInstrument,
							note = step
						)
					}
					else -> null
				}
			}
			else -> null
		}
	}
}
