import com.jonlatane.beatpad.model.*
import com.jonlatane.beatpad.util.to127Int
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.lang.Math.max
import java.util.*
import kotlin.math.floor
import kotlin.properties.Delegates.observable

object BeatClockPaletteConsumer: AnkoLogger {
	var palette: Palette? = null
	var viewModel: PaletteViewModel? by observable(null) { _, _, _ ->

	}
	var tickPosition: Int = 0 // Always relative to ticksPerBeat
	var ticksPerBeat = 24 // Mutable so you can use, say, 36, to play beats against
	                      // input dotted-quarters

	private data class Attack(
		var part: Part? = null,
		var instrument: Instrument? = null,
		var melody: Melody? = null,
	  var note: Note? = null
	) {
		val chosenTones = arrayOfNulls<Int?>(16)
		fun clear() {
			part = null
			instrument = null
			melody = null
			note = null
			for(i in 0 until chosenTones.size) {
				chosenTones[i] = null
			}
		}
		val isUsed get() = part != null
	}
	private val activeAttacks = Vector<Attack>(16)
	private val attackPool = Vector<Attack>(16)

	init {
		for(i in 1..16) {
			attackPool.add(Attack())
		}
	}
	private fun loadCurrentAttacks() {
		val currentBeat: Double = tickPosition.toDouble() / ticksPerBeat
		var currentAttackIndex = 0
		palette?.parts?.map { part ->
			part.melodies.forEach { melody ->
				if(melody.enabled) {
					if(currentAttackIndex >= attackPool.size) {
						attackPool.add(Attack())
					}
					val currentAttack = attackPool[currentAttackIndex]
					melody.populateAttack(currentBeat, part, part.instrument, currentAttack)
					if(currentAttack.isUsed) currentAttackIndex++
				}
			}
		}
	}

	fun tick() {
		palette?.let { palette ->
			val enabledMelodies = palette.parts.flatMap { it.melodies }.filter { it.enabled }
			val totalBeats = enabledMelodies
				.map { it.elements.size.toFloat() / it.subdivisionsPerBeat.toFloat() }
				.reduce(::max)
			loadCurrentAttacks()
			for(attackIndex in 0 until attackPool.size) {
				val attack = attackPool[attackIndex]!!
				if(attack.isUsed) {
					val part = attack.part!!
					val melody = attack.melody!!
					val instrument = attack.instrument!!
					val note = attack.note!!
					// Stop current notes from this attack's melody
					for (activeAttackIndex in 0 until activeAttacks.size) {
						val activeAttack = activeAttacks[activeAttackIndex]!!
						if (activeAttack.melody == attack.melody) {
							activeAttack.chosenTones.forEach {
								it?.let { part.instrument.stop(it) }
							}
							activeAttacks.remove(activeAttack)
							break
						}
					}
					// And play the new notes
					val melodyOffset = viewModel?.orbifold?.chord?.let { chord ->
						attack.melody!!.offsetUnder(chord)
					} ?: 0
					attack.note!!.tones.forEach { tone ->
						val transposedTone = tone + melodyOffset
						val chosenTone = viewModel?.orbifold?.chord?.closestTone(transposedTone) ?: transposedTone
						for(i in 0 until attack.chosenTones.size) {
							if(attack.chosenTones[i] == null) {
								attack.chosenTones[i] = chosenTone
								break
							}
						}
						instrument.play(chosenTone, note.velocity.to127Int)
						viewModel?.let { viewModel ->
							if(melody == viewModel.editingSequence) {
								viewModel.markPlaying(note)
							}
						}
					}
					activeAttacks.add(attack)
				}
				/*
				val activePartAttacks = activeAttacks.getOrPut(part) { mutableMapOf() }
				for((melody, attack) in melodies) {
					activePartAttacks.remove(melody)?.let { lastAttack ->
						lastAttack.chosenTones.forEach {
							part.instrument.stop(it)
						}
					}
					val melodyOffset = viewModel?.orbifold?.chord?.let { chord ->
						melody.offsetUnder(chord)
					} ?: 0
					attack.note.tones.forEach { tone ->
						val transposedTone = tone + melodyOffset
						val chosenTone = viewModel?.orbifold?.chord?.closestTone(transposedTone) ?: transposedTone
						attack.chosenTones.add(chosenTone)
						part.instrument.play(chosenTone, attack.note.velocity.to127Int)
						viewModel?.let { viewModel ->
							if(melody == viewModel.editingSequence) {
								viewModel.markPlaying(attack.note)
							}
						}
					}
					activePartAttacks[melody] = attack
				}*/
			}
			if(tickPosition/ ticksPerBeat >= totalBeats) {
				tickPosition = 0
			} else {
				tickPosition += 1
			}
		} ?: info("Tick called with no Palette available")
	}

	internal fun clearActiveAttacks() {
		for(activeAttack in activeAttacks) {
			if(activeAttack.isUsed) {
				val part = activeAttack.part!!
				activeAttack.chosenTones.forEach {
					it?.let { part.instrument.stop(it) }
				}
			}
		}
		activeAttacks.clear()
	}

	private fun Melody.populateAttack(currentBeat: Double, part: Part, partInstrument: Instrument, attack: Attack) {
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
		when {
			thisTickDistance < nextTickDistance && thisTickDistance < previousTickDistance ->
			{
				val step = elements[indexCandidate]
				when (step) {
					is Melody.Element.Note -> {
						attack.part = part
						attack.instrument = partInstrument
						attack.melody = this
						attack.note = step
					}
					else -> attack.clear()
				}
			}
			else -> attack.clear()
		}
	}
}
