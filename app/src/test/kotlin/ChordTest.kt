import com.jonlatane.beatpad.harmony.chord.*
import com.jonlatane.beatpad.view.melody.BaseMelodyView
import io.damo.aspen.*
import org.assertj.core.api.Assertions.*

class ChordTest : Test({
	describe("#constructor") {
		test("Major chords") {
			assertThat(Chord(0, Maj).extension).contains(0, 4, 7)
		}
		test("minor chords") {
			assertThat(Chord(0, min).extension).contains(0, 3, 7)
		}
	}

	describe("#getTones") {
		test("Major chords") {
			assertThat(Chord(0, Maj).getTones(0, 12)).contains(0, 4, 7)
			assertThat(Chord(6, Maj).getTones(0, 12)).contains(6, 10, 1)
			assertThat(Chord(0, Maj).getTones(12, 24)).contains(12, 16, 19)
		}
		test("minor chords") {
			assertThat(Chord(0, min).getTones(0, 12)).contains(0, 3, 7)
			assertThat(Chord(6, min).getTones(0, 12)).contains(6, 9, 1)
			assertThat(Chord(6, min).getTones(12, 24)).contains(18, 21, 13)
		}
		test("spans") {
			val chord = Chord(0, intArrayOf(0))
			assertThat(chord.getTones(BaseMelodyView.BOTTOM, BaseMelodyView.TOP).equals(8))
		}
	}

	describe("#closestTone") {
		assertThat(Chord(0, Maj).closestTone(0)).isEqualTo(0)
		assertThat(Chord(0, Maj).closestTone(2)).isEqualTo(0)
		assertThat(Chord(0, Maj).closestTone(3)).isEqualTo(4)
		assertThat(Chord(6, Maj).closestTone(6)).isEqualTo(6)
		assertThat(Chord(5, Maj7).closestTone(7)).isEqualTo(5) // Closest to G in an FMaj7 is F.
		assertThat(Chord(5, Maj7).closestTone(10)).isEqualTo(9) // Closest to Bb in an FMaj7 is A.
	}
})