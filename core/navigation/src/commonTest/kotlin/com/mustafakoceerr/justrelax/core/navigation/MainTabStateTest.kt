package com.mustafakoceerr.justrelax.core.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MainTabStateTest {

    @Test
    fun startsOnHome() {
        assertEquals(MainTab.HOME, MainTabState().selected)
    }

    @Test
    fun selectingATab_makesItCurrent() {
        val state = MainTabState()

        state.select(MainTab.TIMER)

        assertEquals(MainTab.TIMER, state.selected)
    }

    @Test
    fun backOnAnotherTab_returnsToHome() {
        val state = MainTabState(MainTab.MIXER)

        val handled = state.onBack()

        assertTrue(handled)
        assertEquals(MainTab.HOME, state.selected)
    }

    @Test
    fun backOnHome_isNotHandled_soTheAppCanExit() {
        val state = MainTabState()

        assertFalse(state.onBack())
        assertEquals(MainTab.HOME, state.selected)
    }

    @Test
    fun backIsOnlyInterceptedAwayFromHome() {
        assertFalse(MainTabState(MainTab.HOME).interceptsBack)
        assertTrue(MainTabState(MainTab.AI).interceptsBack)
    }
}
