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
import platform.MediaPlayer.MPRemoteCommandHandlerStatus
import platform.MediaPlayer.MPRemoteCommandHandlerStatusSuccess
import platform.UIKit.UIApplication
import platform.UIKit.beginReceivingRemoteControlEvents


@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosSoundPlayer : SoundPlayer {

    // AKTİF OYUNCULAR (Sahne)
    private val activePlayers = mutableMapOf<String, AVAudioPlayer>()

    private val playerPool = mutableMapOf<String, AVAudioPlayer>()
    private val POOL_LIMIT = 4
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

            // --- EKLENEN: Garanti olsun diye Remote Control'ü açıyoruz ---
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

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun play(sound: Sound, volume: Float) {
        // Zaten aktifse çık
        if (activePlayers.containsKey(sound.id)) return

        val player: AVAudioPlayer?

        // 1. Önce Havuza Bak (Cache Check)
        if (playerPool.containsKey(sound.id)) {
            // Havuzda var! Dosya okumayla uğraşma, direkt al.
            player = playerPool.remove(sound.id)
            player?.currentTime = 0.0 // Başa sar
        } else {
            // 2. Havuzda yok, sıfırdan oluştur (Maliyetli işlem)
            player = createNewPlayer(sound)
        }

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

    // Yardımcı: Yeni Player Oluşturma
    @OptIn(ExperimentalResourceApi::class)
    private suspend fun createNewPlayer(sound: Sound): AVAudioPlayer? {
        return try {
            val bytes = Res.readBytes("files/${sound.audioFileName}")
            val nsData = bytes.usePinned { pinned ->
                NSData.dataWithBytes(pinned.addressOf(0), bytes.size.toULong())
            }
            AVAudioPlayer(data = nsData, error = null).apply {
                numberOfLoops = -1
                // Default volume
                volume = 0.5f
            }
        } catch (e: Exception) {
            println("iOS Sound Error: ${e.message}")
            null
        }
    }

    override fun stop(soundId: String) {
        activePlayers.remove(soundId)?.let { player ->
            // Durdur
            player.stop()
            // HAVUZA GÖNDER (Recycle)
            addToPool(soundId, player)
        }

        if (activePlayers.isEmpty()) {
            isMasterPlaying = false
            updateLockScreenInfo()
        }
    }

    // Havuza Ekleme Mantığı (FIFO / Limitli)
    private fun addToPool(id: String, player: AVAudioPlayer) {
        if (playerPool.size >= POOL_LIMIT) {
            // Havuz doluysa, rastgele birini (veya ilkini) at.
            // Map olduğu için iterator ile ilk key'i bulup siliyoruz.
            val keyToRemove = playerPool.keys.first()
            playerPool.remove(keyToRemove)
            // iOS'ta 'release' yok, referans gidince ARC temizler.
        }
        playerPool[id] = player
    }

    override fun setVolume(soundId: String, volume: Float) {
        activePlayers[soundId]?.volume = volume
    }

    override fun stopAll() {
        // Aktif olanları durdur ve havuza taşı
        activePlayers.forEach { (id, player) ->
            player.stop()
            addToPool(id, player)
        }
        activePlayers.clear()

        isMasterPlaying = false
        updateLockScreenInfo()
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
        // Uygulama kapanırken her şeyi temizle
        activePlayers.values.forEach { it.stop() }
        activePlayers.clear()
        playerPool.clear()
    }

    // --- KRİTİK DÜZELTME: MAIN THREAD ---
    private fun updateLockScreenInfo() {
        // iOS UI güncellemeleri MUTLAKA Main Thread'de olmalıdır.
        // KMP coroutines arka planda çalışabilir, bu yüzden Main'e zorluyoruz.
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

            // 1.0 = Çalıyor, 0.0 = Durdu
            nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] = if (isMasterPlaying) 1.0 else 0.0

            // Süreyi sıfırda tut
            nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = 0.0

            // Bilgiyi sisteme bas
            infoCenter.nowPlayingInfo = nowPlayingInfo
        }
    }
}