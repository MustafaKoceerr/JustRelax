package com.mustafakoceerr.justrelax.di

import android.content.Context
import com.mustafakoceerr.justrelax.core.network.dto.NetworkSound
import com.mustafakoceerr.justrelax.core.network.dto.NetworkSoundCatalog
import com.mustafakoceerr.justrelax.core.network.dto.withClipUrlsResolvedAgainst
import com.mustafakoceerr.justrelax.core.network.source.SoundRemoteDataSource
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Debug builds play a small sound pack from the APK's assets instead of the CDN catalog, so the
 * engine can be developed and heard without uploading anything. Its clips are `asset:///` URLs,
 * which the Android file downloader copies out of the APK.
 */
val buildTypeModule = module {
    single<SoundRemoteDataSource> { AssetSoundCatalog(androidContext()) }
}

private class AssetSoundCatalog(private val context: Context) : SoundRemoteDataSource {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getSounds(): List<NetworkSound> {
        val text = context.assets.open(CATALOG_PATH).bufferedReader().use { it.readText() }
        return json.decodeFromString<NetworkSoundCatalog>(text).sounds.withClipUrlsResolvedAgainst("asset:///$CATALOG_PATH")
    }

    private companion object {
        const val CATALOG_PATH = "debug-pack/sounds-v2.json"
    }
}
