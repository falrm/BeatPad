package com.jonlatane.beatpad.harmony

import com.jonlatane.beatpad.harmony.chordsequence.*

/**
 * A [Orbifold] is literally just a list of [Orbit]s.
 *
 * Created by jonlatane on 5/26/17.
 */
enum class Orbifold(val title: String, vararg sequences: Orbit) : List<Orbit> by listOf(*sequences) {
	basic("Basic", TwoFiveOne),
	intermediate("Intermediate", DimMinDomMajAug, TwoFiveOne, AlternatingMajorMinorThirds),
	advanced("Advanced", DimMinDomMajAug, FourFiveOne, TwoFiveOne, AlternatingMajorMinorThirds, AlternatingMajorMinorSeconds),
	master("Master", DimMinDomMajAug, SubdominantOptions, FourFiveOne, TwoFiveOne, TwoFiveOneMinor, AlternatingMajorMinorThirds, WholeSteps, AlternatingMajorMinorSeconds),
	chainsmokers("Chainsmokers", CircleOfFifths, Chainsmokers, AlternatingMajorMinorThirds),
	pop("Pop", SubdominantOptions, WholeSteps, AlternatingMajorMinorSeconds),
	funk("Funk", FunkDominantStepUp, MinorFunk, FunkDominantSevens);

	companion object {
		internal val ALL_SEQUENCEs: List<Orbit> = Orbifold.values().flatMap { it }
	}
}
