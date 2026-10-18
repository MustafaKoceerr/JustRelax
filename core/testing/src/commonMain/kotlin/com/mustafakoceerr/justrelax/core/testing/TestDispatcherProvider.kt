package com.mustafakoceerr.justrelax.core.testing

import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope

/**
 * Sends every dispatcher to one test dispatcher, so `runTest`'s virtual time drives them
 * and tests stay deterministic.
 */
class TestDispatcherProvider(
    dispatcher: CoroutineDispatcher = StandardTestDispatcher()
) : DispatcherProvider {
    override val main: CoroutineDispatcher = dispatcher
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
    override val unconfined: CoroutineDispatcher = dispatcher
}

/** Dispatchers on this test's scheduler. */
fun TestScope.testDispatchers() = TestDispatcherProvider(StandardTestDispatcher(testScheduler))
