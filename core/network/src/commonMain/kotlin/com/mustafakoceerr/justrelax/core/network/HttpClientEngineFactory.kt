package com.mustafakoceerr.justrelax.core.network


import io.ktor.client.engine.HttpClientEngine

/**
 * Platforma özel Ktor motorunu (Android için OkHttp, iOS için Darwin)
 * sağlayan bir fabrika arayüzü.
 */
internal expect fun createHttpClientEngine(): HttpClientEngine