package com.mustafakoceerr.justrelax.feature.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.domain.usecase.AdjustVolumeUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.DownloadSoundUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.GetPlayingSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.PlaySoundUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.StopSoundUseCase
import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeEffect
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeIntent
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/*
Bu sınıfın tek görevi (SRP): Orkestrasyon.
Kullanıcıdan gelen emirleri (Intent) alır, ilgili iş birimlerine (UseCase) dağıtır ve sonucu (State/Effect) UI'a bildirir. "Nasıl çalınır?", "Veritabanına nasıl bağlanılır?" gibi detayları ASLA bilmez.
 */

class HomeScreenModel(
    private val soundRepository: SoundRepository,
    // --- Player UseCases ---
    private val playSoundUseCase: PlaySoundUseCase,
    private val stopSoundUseCase: StopSoundUseCase,
    private val getPlayingSoundsUseCase: GetPlayingSoundsUseCase,
    private val adjustVolumeUseCase: AdjustVolumeUseCase,
    // --- Data & Download UseCases ---
    private val downloadSoundUseCase: DownloadSoundUseCase,
    // --- Feature Specific (Legacy/Banner) ---
//    private val bannerUseCases: HomeBannerUseCases
) : ScreenModel {

    // 1. STATE (Single Source of Truth)
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    // 2. EFFECT (One-shot Events)
    private val _effect = Channel<HomeEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        // Başlangıçta verileri dinlemeye başla
        observeSounds()
        observePlayingState()
    }

    /**
     * UI'dan gelen tüm etkileşimlerin giriş kapısı.
     */
    fun processIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.SelectCategory -> selectCategory(intent)
            is HomeIntent.ToggleSound -> toggleSound(intent.sound)
            is HomeIntent.ChangeVolume -> changeVolume(intent)
            is HomeIntent.DownloadAllMissing -> {} //downloadAllMissing()
            is HomeIntent.DismissBanner -> {}//dismissBanner()
            is HomeIntent.SettingsClicked -> sendEffect(HomeEffect.NavigateToSettings)
        }
    }

    // --- Private Observation Methods ---
    private fun observeSounds() {
        screenModelScope.launch {
            soundRepository.getSounds().collectLatest { sounds ->
                // Veri her değiştiğinde Banner durumunu kontrol et
//                val shouldShowBanner = bannerUseCases.shouldShow(sounds)
                val shouldShowBanner = false

                _state.update { currentState ->
                    currentState.copy(
                        allSounds = sounds,
                        // Kategori değişmedi ama liste değiştiyse filtreyi tekrar uygula
                        filteredSounds = sounds.filter { it.categoryId == currentState.selectedCategory.id },
                        showDownloadBanner = shouldShowBanner && !currentState.isDownloadingAll,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun observePlayingState() {
        screenModelScope.launch {
            // Mixer'dan gelen "Şu an şunlar çalıyor" bilgisini dinle
            getPlayingSoundsUseCase().collectLatest { playingIds ->
                _state.update { it.copy(playingSoundIds = playingIds) }
            }
        }
    }

    // --- Intent Handlers ---
    private fun selectCategory(intent: HomeIntent.SelectCategory) {
        _state.update {
            it.copy(
                selectedCategory = intent.category,
                // Listeyi bellekte filtrele (Veritabanına gitmeye gerek yok)
                filteredSounds = it.allSounds.filter { sound -> sound.categoryId == intent.category.id }
            )
        }
    }

    private fun toggleSound(sound: Sound) {
        screenModelScope.launch {
            val isPlaying = _state.value.playingSoundIds.contains(sound.id)

            if (isPlaying) {
                // Senaryo 1: Çalıyorsa Durdur
                stopSoundUseCase(sound.id)
            } else {
                // Senaryo 2: Çalmıyorsa...
                if (sound.isDownloaded) {
                    // 2a. İndirilmişse Çal
                    playDownloadedSound(sound)
                } else {
                    // 2b. İndirilmemişse Önce İndir Sonra Çal
                    downloadAndPlaySound(sound)
                }
            }
        }
    }

    private suspend fun playDownloadedSound(sound: Sound) {
        // Daha önce ayarlanmış bir volume var mı? Yoksa default 0.5f
        val currentVolume = _state.value.soundVolumes[sound.id] ?: 0.5f

        // UseCase çağır
        val result = playSoundUseCase(sound.id, volume = currentVolume)

        // Hata varsa kullanıcıya bildir
        if (result is Resource.Error) {
            _effect.send(HomeEffect.ShowError(result.error))
        }
        // Başarılıysa state (playingSoundIds) zaten observePlayingState() sayesinde güncellenecek.
    }

    private fun downloadAndPlaySound(sound: Sound) {
        screenModelScope.launch {
            // Loading state ekle
            _state.update { it.copy(downloadingSoundIds = it.downloadingSoundIds + sound.id) }

            downloadSoundUseCase(sound.id).collect { status ->
                when (status) {
                    is DownloadStatus.Completed -> {
                        // İndirme bitti, loading'den düş ve çal
                        _state.update { it.copy(downloadingSoundIds = it.downloadingSoundIds - sound.id) }
                        playDownloadedSound(sound)
                    }

                    is DownloadStatus.Error -> {
                        // Hata mesajı göster
                        _state.update { it.copy(downloadingSoundIds = it.downloadingSoundIds - sound.id) }
                        _effect.send(HomeEffect.ShowMessage(status.message))
                    }

                    else -> { /* Progress veya Queued durumları UI'da gösterilebilir */
                    }
                }
            }
        }
    }

//    private fun downloadAllMissing() {
//        screenModelScope.launch {
//            // BannerLogic eski yapıdan geliyor, aynen entegre ediyoruz.
//            // (Burada batchDownloadSoundUseCase gibi bir yapıya ihtiyaç olabilir ama şimdilik bannerUseCases kullanıyoruz)
//            bannerUseCases.downloadAllMissingSounds().collect { status ->
//                // Eski kodundaki logic aynen buraya taşınabilir veya UseCase'e çevrilebilir.
//                // Özetle: _state.isDownloadingAll güncellenecek.
//            }
//        }
//    }

    private fun changeVolume(intent: HomeIntent.ChangeVolume) {
        // 1. UI State'ini anında güncelle (Optimistic Update) - Böylece slider takılmaz.
        _state.update {
            val newVolumes = it.soundVolumes.toMutableMap().apply {
                put(intent.soundId, intent.volume)
            }
            it.copy(soundVolumes = newVolumes)
        }
        // 2. Motor'a haber ver
        // Eğer ses o an çalıyorsa, Mixer anlık olarak sesi değiştirir.
        adjustVolumeUseCase(intent.soundId, intent.volume)
    }

//    private fun dismissBanner() {
//        screenModelScope.launch {
//            bannerUseCases.dismiss()
//            _state.update { it.copy(showDownloadBanner = false) }
//        }
//    }

    private fun sendEffect(effect: HomeEffect) {
        screenModelScope.launch { _effect.send(effect) }
    }
}
