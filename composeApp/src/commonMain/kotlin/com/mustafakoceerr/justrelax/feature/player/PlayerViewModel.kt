package com.mustafakoceerr.justrelax.feature.player

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.sound.domain.manager.SoundDownloader
import com.mustafakoceerr.justrelax.core.sound.domain.manager.SoundManager
import com.mustafakoceerr.justrelax.feature.player.mvi.PlayerIntent
import com.mustafakoceerr.justrelax.feature.player.mvi.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val soundManager: SoundManager, // ARTIK PLAYER DEĞİL MANAGER
    private val soundDownloader: SoundDownloader
) : ScreenModel {
    // İndirme durumunu kendi içimizde tutuyoruz (Local State)
    private val _downloadingIds = MutableStateFlow<Set<String>>(emptySet())

    // Manager State ile Downloading State'i birleştiriyoruz (Combine)
    val state: StateFlow<PlayerState> = combine(
        soundManager.state,
        _downloadingIds
    ) { managerState, downloadingIds ->
        PlayerState(
            activeSounds = managerState.activeSounds,
            isMasterPlaying = managerState.isMasterPlaying,
            activeSoundDetails = managerState.activeSoundDetails,
            downloadingSoundIds = downloadingIds // UI'a gidiyor
        )
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerState()
    )

    fun processIntent(intent: PlayerIntent) {
        // Gelen emri direkt Manager'a iletiyoruz
        when (intent) {
            is PlayerIntent.ToggleSound -> handleToggleSound(intent.sound)
            is PlayerIntent.ChangeVolume -> soundManager.changeVolume(intent.soundId, intent.volume)
            is PlayerIntent.ToggleMasterPlayPause -> soundManager.toggleMaster()
            is PlayerIntent.StopAll -> soundManager.stopAll()
        }
    }

    private fun handleToggleSound(sound: Sound) {
        screenModelScope.launch {
            // 1. Zaten çalıyorsa durdur
            if (state.value.activeSounds.containsKey(sound.id)) {
                soundManager.toggleSound(sound)
                return@launch
            }

            // 2. Zaten indiriliyorsa işlem yapma (Çift tıklama koruması)
            if (_downloadingIds.value.contains(sound.id)) return@launch

            // 3. İndirilmiş mi kontrol et
            if (sound.isDownloaded) {
                soundManager.toggleSound(sound)
            } else {
                // 4. İndirme Başlat
                startDownload(sound)
            }
        }
    }

    private suspend fun startDownload(sound: Sound) {
        // Listeye ekle (UI'da spinner döner)
        _downloadingIds.update { it + sound.id }
        val isSuccess = soundDownloader.downloadSound(sound.id)

        // Listeden çıkar (UI normale döner)
        _downloadingIds.update { it - sound.id }

        if (isSuccess){
            // İndirme bitti, veritabanı güncellendi.
            // Ancak elimizdeki 'sound' objesi hala eski (isDownloaded=false).
            // SoundManager'a güncel path lazım.
            // HIZLI ÇÖZÜM: Kullanıcı tekrar tıklasın. (Otomatik çalma için DB'den tekrar çekmek gerekir)
            // Şimdilik UI güncellenecek (Bulut ikonu gidecek), kullanıcı tekrar basınca çalacak.
        }else{
            // Hata mesajı gösterilebilir (Effect ile)
            println("Download failed for ${sound.name}")
        }
    }
}















