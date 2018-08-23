package com.jonlatane.beatpad.model.harmony

import com.jonlatane.beatpad.model.harmony.chordsequence.*

/**
 * A [Orbifold] is literally just a list of [Orbit]s.
 *
 * Created by jonlatane on 5/26/17.
 */
enum class Orbifold(val title: String, vararg sequences: Orbit)
  : List<Orbit> by listOf(*sequences) {
  super_basic("Super Basic", DimMinDomMajAug),
  basic("Basic", TwoFiveOne),
  intermediate(
    "Intermediate",
    DimMinDomMajAug,
    TwoFiveOne,
    AlternatingMajorMinorThirds
  ),
  advanced(
    "Advanced",
    DimMinDomMajAug,
    FourFiveOne,
    TwoFiveOne,
    AlternatingMajorMinorThirds,
    AlternatingMajorMinorSeconds
  ),
  master(
    "Master",
    DimMinDomMajAug,
    SubdominantOptions,
    FourFiveOne,
    TwoFiveOne,
    TwoFiveOneMinor,
    AlternatingMajorMinorThirds,
    WholeSteps,
    AlternatingMajorMinorSeconds
  ),
  pop(
    "Pop",
    SubdominantOptions,
    WholeSteps,
    AlternatingMajorMinorSeconds
  ),
  funk(
    "Funk",
    FunkDominantStepUp,
    MinorFunk,
    FunkDominantSevens
  ),
  funkyChainsmokingAnimalsSimple(
    "Something Just Like Animal Spirits (Basic)",
    Chainsmokers,
    AnimalSpirits
  ),
  funkyChainsmokingAnimals(
    "Something Just Like Animal Spirits",
    CircleOfFifths,
    Chainsmokers,
    AnimalSpirits,
    SubdominantOptions
  ),
  functional(
    "Big Chords (Intermediate)",
    DimMinDomMajAug,
    FunctionalTwoFiveOne,
    FunctionalAlternatingMajorMinorThirds
  );

  companion object {
    internal val ALL_ORBITS: List<Orbit> = Orbifold.values().flatMap { it }
  }
}
