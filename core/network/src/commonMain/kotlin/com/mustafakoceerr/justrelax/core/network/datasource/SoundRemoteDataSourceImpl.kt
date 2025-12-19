package com.mustafakoceerr.justrelax.core.network.datasource

import com.mustafakoceerr.justrelax.core.domain.source.SoundRemoteDataSource
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.network.KtorClient
import com.mustafakoceerr.justrelax.core.network.dto.NetworkSound
import com.mustafakoceerr.justrelax.core.network.mapper.SoundMapper
import io.ktor.client.call.body
import io.ktor.client.request.get

/**
 * 'SoundRemoteDataSource' arayüzünün Ktor kullanan gerçek implementasyonu.
 * 'internal' olması, bu sınıfın bir implementasyon detayı olduğunu ve dışarıdan
 * erişilmemesi gerektiğini vurgular.
 */
internal class SoundRemoteDataSourceImpl(
    private val soundMapper: SoundMapper // Mapper'ı artık enjekte alıyoruz
) : SoundRemoteDataSource {

    // İleride bu URL'i BuildConfig veya Remote Config'den alacağız.
    // Şimdilik buraya sabit (hardcoded) yazıyoruz.
    private val soundsUrl =
        "https://raw.githubusercontent.com/mustafakoceerr/JustRelax-KMP/main/sounds.json"

    // Todo: Hardcoded string'i değiştir.
    override suspend fun getSounds(): List<Sound> {
        // Hata yönetimi (try-catch) burada DEĞİL, Repository katmanında yapılacak.
        // Bu sınıfın tek görevi isteği atmak ve ham veriyi dönmektir.

        val networkSounds = KtorClient.get(soundsUrl).body<List<NetworkSound>>()
        return soundMapper.toModelList(networkSounds)
    }
}