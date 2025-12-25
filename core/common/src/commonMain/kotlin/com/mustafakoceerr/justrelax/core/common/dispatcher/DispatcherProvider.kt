package com.mustafakoceerr.justrelax.core.common.dispatcher

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Coroutine'lerin hangi thread üzerinde çalışacağını belirleyen arayüz.
 * Bu arayüz sayesinde Test yazarken 'TestDispatcher',
 * Gerçek uygulamada 'StandardDispatchers' kullanabiliriz.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}