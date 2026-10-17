package com.mustafakoceerr.justrelax.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

// OkHttp defaults allow TLS 1.2 and 1.3 and no cleartext; timeouts come from HttpTimeout.
internal actual fun createHttpClientEngine(): HttpClientEngine = OkHttp.create()
