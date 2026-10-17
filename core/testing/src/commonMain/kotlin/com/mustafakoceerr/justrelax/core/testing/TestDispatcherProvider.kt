package com.mustafakoceerr.justrelax.core.testing

import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher

/**
 * Tüm dispatcher'ları tek bir test dispatcher'ına yönlendirir; böylece `runTest`
 * sanal zamanı kontrol eder ve testler deterministik çalışır.
 */
class TestDispatcherProvider(
    dispatcher: CoroutineDispatcher = StandardTestDispatcher()
) : DispatcherProvider {
    override val main: CoroutineDispatcher = dispatcher
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
    override val unconfined: CoroutineDispatcher = dispatcher
}
