package com.mustafakoceerr.justrelax.core.media

import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TrimRulesTest {

    @Test
    fun aBedStartsWithTheWholeFileAndAnEventWithItsFirstTenSeconds() {
        assertEquals(TrimRange(0, 60_000), TrimRules.defaultRange(SoundKind.BED, sourceMs = 60_000))
        assertEquals(TrimRange(0, 10_000), TrimRules.defaultRange(SoundKind.EVENT, sourceMs = 60_000))
        assertEquals(TrimRange(0, 3_000), TrimRules.defaultRange(SoundKind.EVENT, sourceMs = 3_000))
    }

    @Test
    fun shortFilesDefaultToEventsAndCannotBeBeds() {
        assertEquals(SoundKind.EVENT, TrimRules.defaultKind(sourceMs = 14_999))
        assertEquals(SoundKind.BED, TrimRules.defaultKind(sourceMs = 15_000))
        assertEquals(setOf(SoundKind.EVENT), TrimRules.allowedKinds(sourceMs = 4_999))
        assertEquals(setOf(SoundKind.BED, SoundKind.EVENT), TrimRules.allowedKinds(sourceMs = 5_000))
    }

    @Test
    fun handlesStopAtTheFileAndAtTheKindsLengthLimits() {
        val event = TrimRange(20_000, 25_000)

        assertEquals(TrimRange(15_000, 25_000), TrimRules.moveStart(event, 0, SoundKind.EVENT, sourceMs = 60_000))
        assertEquals(TrimRange(24_700, 25_000), TrimRules.moveStart(event, 26_000, SoundKind.EVENT, sourceMs = 60_000))
        assertEquals(TrimRange(20_000, 30_000), TrimRules.moveEnd(event, 59_000, SoundKind.EVENT, sourceMs = 60_000))

        val bed = TrimRange(10_000, 40_000)
        assertEquals(TrimRange(0, 40_000), TrimRules.moveStart(bed, -5, SoundKind.BED, sourceMs = 60_000))
        assertEquals(TrimRange(10_000, 15_000), TrimRules.moveEnd(bed, 12_000, SoundKind.BED, sourceMs = 60_000))
        assertEquals(TrimRange(10_000, 60_000), TrimRules.moveEnd(bed, 90_000, SoundKind.BED, sourceMs = 60_000))
    }

    @Test
    fun changingTheKindFitsTheRangeToTheNewLimits() {
        assertEquals(TrimRange(5_000, 15_000), TrimRules.fit(TrimRange(5_000, 40_000), SoundKind.EVENT, sourceMs = 60_000))
        assertEquals(TrimRange(5_000, 10_000), TrimRules.fit(TrimRange(5_000, 7_000), SoundKind.BED, sourceMs = 60_000))
        assertEquals(TrimRange(1_000, 6_000), TrimRules.fit(TrimRange(4_000, 6_000), SoundKind.BED, sourceMs = 6_000))
    }

    @Test
    fun aRangeIsValidOnlyInsideTheFileAndTheKindsLimits() {
        assertNull(TrimRules.problem(TrimRange(0, 10_000), SoundKind.EVENT, sourceMs = 60_000))
        assertEquals(TrimProblem.TOO_LONG_FOR_EVENT, TrimRules.problem(TrimRange(0, 10_001), SoundKind.EVENT, sourceMs = 60_000))
        assertEquals(TrimProblem.TOO_SHORT_FOR_BED, TrimRules.problem(TrimRange(0, 4_999), SoundKind.BED, sourceMs = 60_000))
        assertEquals(TrimProblem.TOO_SHORT, TrimRules.problem(TrimRange(0, 299), SoundKind.EVENT, sourceMs = 60_000))
        assertEquals(TrimProblem.OUTSIDE_FILE, TrimRules.problem(TrimRange(55_000, 61_000), SoundKind.BED, sourceMs = 60_000))
    }
}
