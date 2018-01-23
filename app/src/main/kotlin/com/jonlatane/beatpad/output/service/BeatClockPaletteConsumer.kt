import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.AnkoLogger
import kotlin.properties.Delegates

object BeatClockPaletteConsumer : AnkoLogger {
	val viewModel by Delegates.observable<PaletteViewModel?>(null) { _, _, _ ->

	}
	val palette get() = viewModel?.palette

	fun tick() {
		palette?.let{ palette ->
			val sequence = palette.parts[0].melodies[0]
			val instrument = palette.parts[0].instrument
			try {

				for (step in sequence.elements) {
					println("playing")
					viewModel?.orbifold?.post { viewModel?.markPlaying(step) }
					// Interpret the booleans as "play" or "rest"
					when (step) {
						is Melody.Element.Note -> {
							print("Note: ")
							instrument.stop()
							step.tones.forEach {
								val closestNote = it//chordResolver().closestTone(it)

								println("playing $closestNote")
								instrument.play(closestNote)
							}
						}
						else -> print("Sustain: ")
					}
				}
			} catch (ignored: InterruptedException) {
			} finally {
			}
		}
	}
}
