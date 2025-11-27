package com.mustafakoceerr.justrelax.core.sound.data.player

import com.mustafakoceerr.justrelax.core.sound.domain.model.Sound
import com.mustafakoceerr.justrelax.core.sound.domain.player.SoundPlayer
import justrelax.core.generated.resources.Res
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSURL

class IosSoundPlayer : SoundPlayer {
    // Aktif çalan playerları tutuyoruz.
    private var activePlayers = mutableMapOf<String, AVAudioPlayer>()

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun play(
        sound: Sound,
        volume: Float
    ) {
        // Zaten çalıyorsa işlem yapma.
        if (activePlayers.containsKey(sound.id)) return

        try {
            // 1. Dosya yolunu al (Compose Resources)
            // iOS'te bu bize dosyanın Bundle içindeki tam yolunu (String) verir.
            val uriString = Res.getUri("files/${sound.audioFileName}")

            // 2. string yolu NSURL'e çevir
            val url = NSURL.fileURLWithPath(uriString)

            // 3. Player oluştur.
            val player = AVAudioPlayer(contentsOfURL = url, error = null).apply {
                numberOfLoops = -1 // sonsuz döngü (loop)
                this.volume = volume
                prepareToPlay()
                play()
            }

            // 4. Listeye ekle
            activePlayers[sound.id] = player
        } catch (e: Exception) {
            println("iOS Sound Error: ${e.message}")
        }
    }

    override fun stop(soundId: String) {
        activePlayers.remove(soundId)?.let { player ->
            player.stop()
            // AVAudioPlayer'da explicit release yoktur, ARC (Automatic Reference Counting) halleder.
            // Map'ten silmek yeterlidir.
        }
    }

    override fun setVolume(soundId: String, volume: Float) {
        activePlayers[soundId]?.volume = volume
    }

    override fun stopAll() {
        activePlayers.values.forEach { it.stop()}
        activePlayers.clear()
    }

    override fun release() {
        stopAll()
    }


}