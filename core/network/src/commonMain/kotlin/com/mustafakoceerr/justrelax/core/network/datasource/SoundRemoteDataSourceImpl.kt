package com.mustafakoceerr.justrelax.core.network.datasource

import com.mustafakoceerr.justrelax.core.domain.source.SoundRemoteDataSource
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.network.KtorClient
import com.mustafakoceerr.justrelax.core.network.dto.NetworkSound
import com.mustafakoceerr.justrelax.core.network.mapper.NetworkSoundToDomainMapper
import io.ktor.client.call.body
import io.ktor.client.request.get

//Sadece Config'leri (Metadata) getirecek.
import com.mustafakoceerr.justrelax.core.network.BuildConfig
import io.ktor.client.HttpClient

internal class SoundRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val soundMapper: NetworkSoundToDomainMapper
) : SoundRemoteDataSource {

    private val soundsUrl = BuildConfig.SOUNDS_URL

    override suspend fun getSounds(): List<Sound> {
        // Artık 'KtorClient' değil, enjekte edilen 'httpClient' kullanılıyor
        val networkDtos = httpClient.get(soundsUrl).body<List<NetworkSound>>()
        return soundMapper.toModelList(networkDtos)
    }
}