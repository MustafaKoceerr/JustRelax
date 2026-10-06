package com.mustafakoceerr.justrelax.core.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BackStackTest {

    @Test
    fun navigate_pushesOnTop() {
        val backStack = mutableListOf<Route>(Route.Main)

        backStack.navigate(Route.Settings)

        assertEquals(listOf<Route>(Route.Main, Route.Settings), backStack)
    }

    @Test
    fun navigatingToTheCurrentRoute_doesNotDuplicateIt() {
        val backStack = mutableListOf<Route>(Route.Main, Route.Settings)

        backStack.navigate(Route.Settings)

        assertEquals(listOf<Route>(Route.Main, Route.Settings), backStack)
    }

    @Test
    fun resetTo_clearsHistory() {
        val backStack = mutableListOf<Route>(Route.Splash, Route.Onboarding)

        backStack.resetTo(Route.Main)

        assertEquals(listOf<Route>(Route.Main), backStack)
    }

    @Test
    fun goBack_popsTheTopRoute() {
        val backStack = mutableListOf<Route>(Route.Main, Route.Settings)

        assertTrue(backStack.goBack())
        assertEquals(listOf<Route>(Route.Main), backStack)
    }

    @Test
    fun goBack_neverRemovesTheRoot() {
        val backStack = mutableListOf<Route>(Route.Main)

        assertFalse(backStack.goBack())
        assertEquals(listOf<Route>(Route.Main), backStack)
    }
}
