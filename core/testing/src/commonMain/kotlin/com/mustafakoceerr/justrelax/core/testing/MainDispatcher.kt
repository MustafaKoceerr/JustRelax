package com.mustafakoceerr.justrelax.core.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * [runTest] with `Dispatchers.Main` bound to the test scheduler, so ViewModel scopes
 * (which run on Main) are driven by virtual time. Create the ViewModel inside [testBody].
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun runMainTest(testBody: suspend TestScope.() -> Unit) = runTest {
    Dispatchers.setMain(StandardTestDispatcher(testScheduler))
    try {
        testBody()
        // Let pending ViewModel work (e.g. stateIn's WhileSubscribed stop timeout) finish
        // in virtual time before Main goes away.
        testScheduler.advanceUntilIdle()
    } finally {
        Dispatchers.resetMain()
    }
}
