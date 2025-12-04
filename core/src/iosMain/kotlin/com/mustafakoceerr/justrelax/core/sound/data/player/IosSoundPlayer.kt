package com.mustafakoceerr.justrelax.core.sound.data.player

import com.mustafakoceerr.justrelax.core.generated.resources.Res
import com.mustafakoceerr.justrelax.core.sound.domain.model.Sound
import com.mustafakoceerr.justrelax.core.sound.domain.player.SoundPlayer
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.MediaPlayer.MPMediaItemPropertyArtist
import platform.MediaPlayer.MPMediaItemPropertyTitle
import platform.MediaPlayer.MPNowPlayingInfoCenter
import platform.MediaPlayer.MPNowPlayingInfoPropertyElapsedPlaybackTime
import platform.MediaPlayer.MPNowPlayingInfoPropertyPlaybackRate
import platform.MediaPlayer.MPRemoteCommandCenter
import platform.MediaPlayer.MPRemoteCommandHandlerStatusSuccess
import platform.UIKit.UIApplication
import platform.UIKit.beginReceivingRemoteControlEvents

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosSoundPlayer : SoundPlayer {

    // Sadece aktif çalanları tutuyoruz. Havuz (Pool) yok.
    private val activePlayers = mutableMapOf<String, AVAudioPlayer>()

    private var isMasterPlaying = false

    init {
        configureAudioSession()
        configureRemoteCommands()
    }

    private fun configureAudioSession() {
        try {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryPlayback, error = null)
            session.setActive(true, error = null)
            UIApplication.sharedApplication.beginReceivingRemoteControlEvents()
        } catch (e: Exception) {
            println("iOS AudioSession Error: ${e.message}")
        }
    }

    private fun configureRemoteCommands() {
        val commandCenter = MPRemoteCommandCenter.sharedCommandCenter()
        commandCenter.playCommand.addTargetWithHandler { _ ->
            resumeAll()
            MPRemoteCommandHandlerStatusSuccess
        }
        commandCenter.pauseCommand.addTargetWithHandler { _ ->
            pauseAll()
            MPRemoteCommandHandlerStatusSuccess
        }
        commandCenter.nextTrackCommand.setEnabled(false)
        commandCenter.previousTrackCommand.setEnabled(false)
    }

    // --- ATOMIC MIX (Sadeleştirilmiş) ---
    @OptIn(ExperimentalResourceApi::class)
    override suspend fun playMix(sounds: List<Pair<Sound, Float>>) {
        // 1. TEMİZLİK: Aktif olan her şeyi durdur ve sil.
        stopAllInternal()

        // 2. KURULUM: Yeni listeyi oluştur ve başlat.
        // Havuz kontrolü yok, direkt oluşturuyoruz.
        sounds.forEach { (sound, volume) ->
            val player = createNewPlayer(sound)
            if (player != null) {
                player.volume = volume
                player.prepareToPlay()
                player.play()
                activePlayers[sound.id] = player
            }
        }

        // 3. FİNAL
        isMasterPlaying = true
        updateLockScreenInfo()
    }

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun play(sound: Sound, volume: Float) {
        // Zaten çalıyorsa işlem yapma
        if (activePlayers.containsKey(sound.id)) return

        // Yeni oluştur
        val player = createNewPlayer(sound)

        if (player != null) {
            player.volume = volume
            player.prepareToPlay()
            player.play()

            activePlayers[sound.id] = player

            if (!isMasterPlaying) {
                isMasterPlaying = true
                updateLockScreenInfo()
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun createNewPlayer(sound: Sound): AVAudioPlayer? {
        return try {
            val bytes = Res.readBytes("files/${sound.audioFileName}")
            val nsData = bytes.usePinned { pinned ->
                NSData.dataWithBytes(pinned.addressOf(0), bytes.size.toULong())
            }
            AVAudioPlayer(data = nsData, error = null).apply {
                numberOfLoops = -1 // Sonsuz döngü
                volume = 0.5f // Varsayılan, sonra override ediliyor
            }
        } catch (e: Exception) {
            println("iOS Sound Error: ${e.message}")
            null
        }
    }

    override fun stop(soundId: String) {
        activePlayers.remove(soundId)?.let { player ->
            player.stop()
            // player değişkeni scope dışına çıkınca ARC tarafından temizlenir.
        }

        if (activePlayers.isEmpty()) {
            isMasterPlaying = false
            updateLockScreenInfo()
        }
    }

    override fun setVolume(soundId: String, volume: Float) {
        activePlayers[soundId]?.volume = volume
    }

    override fun stopAll() {
        stopAllInternal()
        isMasterPlaying = false
        updateLockScreenInfo()
    }

    // Yardımcı metod: StopAll mantığını hem mix hem stopAll kullanıyor
    private fun stopAllInternal() {
        activePlayers.values.forEach { it.stop() }
        activePlayers.clear()
    }

    override fun pause(soundId: String) {
        activePlayers[soundId]?.pause()
    }

    override fun resume(soundId: String) {
        activePlayers[soundId]?.play()
    }

    override fun pauseAll() {
        isMasterPlaying = false
        activePlayers.values.forEach { it.pause() }
        updateLockScreenInfo()
    }

    override fun resumeAll() {
        isMasterPlaying = true
        activePlayers.values.forEach { it.play() }
        updateLockScreenInfo()
    }

    override fun release() {
        stopAllInternal()
    }

    private fun updateLockScreenInfo() {
        CoroutineScope(Dispatchers.Main).launch {
            val infoCenter = MPNowPlayingInfoCenter.defaultCenter()

            if (activePlayers.isEmpty()) {
                infoCenter.nowPlayingInfo = null
                return@launch
            }

            val title = "Just Relax"
            val artist = "${activePlayers.size} Active Sounds"

            val nowPlayingInfo = mutableMapOf<Any?, Any?>()
            nowPlayingInfo[MPMediaItemPropertyTitle] = title
            nowPlayingInfo[MPMediaItemPropertyArtist] = artist
            nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] = if (isMasterPlaying) 1.0 else 0.0
            nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = 0.0

            infoCenter.nowPlayingInfo = nowPlayingInfo
        }
    }
}