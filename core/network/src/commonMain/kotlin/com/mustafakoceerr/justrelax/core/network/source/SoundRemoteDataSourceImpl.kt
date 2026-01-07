package com.mustafakoceerr.justrelax.core.network.source

import com.mustafakoceerr.justrelax.core.domain.source.SoundRemoteDataSource
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.network.dto.NetworkSound
import com.mustafakoceerr.justrelax.core.network.mapper.NetworkSoundToDomainMapper
import com.mustafakoceerr.justrelax.core.network.util.RemoteEndpoints
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

internal class SoundRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val soundMapper: NetworkSoundToDomainMapper
) : SoundRemoteDataSource {

    private val soundsUrl = RemoteEndpoints.soundsConfig

    override suspend fun getSounds(): List<Sound> {
        val networkDtos = httpClient.get(soundsUrl).body<List<NetworkSound>>()
        return soundMapper.toModelList(networkDtos)
    }
}