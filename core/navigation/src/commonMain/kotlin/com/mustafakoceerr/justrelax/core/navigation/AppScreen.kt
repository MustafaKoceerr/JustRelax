package com.mustafakoceerr.justrelax.core.navigation

import cafe.adriel.voyager.core.screen.Screen

/**
 * Base interface for all screens within the application.
 *
 * Acts as a marker interface to enforce a common type hierarchy,
 * allowing for future extensions (e.g., logging, analytics) without modifying individual screens.
 */
interface AppScreen : Screen