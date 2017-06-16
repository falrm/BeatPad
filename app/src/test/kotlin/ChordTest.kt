import com.jonlatane.beatpad.harmony.chord.*
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
	}

	describe("#closestTone") {
		assertThat(Chord(0, Maj).closestTone(0)).isEqualTo(0)
		assertThat(Chord(0, Maj).closestTone(2)).isEqualTo(0)
		assertThat(Chord(0, Maj).closestTone(3)).isEqualTo(4)
	}
})