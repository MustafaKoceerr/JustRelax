package com.mustafakoceerr.justrelax.core.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
        // Stop observers, then let pending ViewModel work (e.g. stateIn's WhileSubscribed
        // stop timeout) finish in virtual time before Main goes away.
        backgroundScope.coroutineContext.cancelChildren()
        testScheduler.advanceUntilIdle()
    } finally {
        Dispatchers.resetMain()
    }
}

/**
 * Keeps a subscriber on [flow] for the rest of the test, like the UI would. Needed for state
 * built with `stateIn(WhileSubscribed)`, which only updates while observed.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun TestScope.observe(flow: Flow<*>) {
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.collect {} }
}
