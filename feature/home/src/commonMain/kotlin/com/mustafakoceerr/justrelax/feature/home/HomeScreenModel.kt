package com.mustafakoceerr.justrelax.feature.home


import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.usecase.player.AdjustVolumeUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.GetGlobalMixerStateUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.PlaySoundUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.StopSoundUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadSingleSoundUseCase
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.home.domain.usecase.GetCategorizedSoundsUseCase
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeContract
import justrelax.feature.home.generated.resources.Res
import justrelax.feature.home.generated.resources.download_failed
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeScreenModel(
    getCategorizedSoundsUseCase: GetCategorizedSoundsUseCase,
    getGlobalMixerStateUseCase: GetGlobalMixerStateUseCase,
    private val playSoundUseCase: PlaySoundUseCase,
    private val stopSoundUseCase: StopSoundUseCase,
    private val downloadSingleSoundUseCase: DownloadSingleSoundUseCase,
    private val adjustVolumeUseCase: AdjustVolumeUseCase
) : ScreenModel, HomeContract {

    private val _state = MutableStateFlow(HomeContract.State())
    val state = _state.asStateFlow()

    private val _effect = Channel<HomeContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        combine(
            getCategorizedSoundsUseCase(),
            getGlobalMixerStateUseCase()
        ) { soundsResult, playerState ->
            _state.update { currentState ->
                val categories =
                    (soundsResult as? Resource.Success)?.data ?: currentState.categories

                // Eğer henüz bir kategori seçilmemişse ve kategoriler yeni geldiyse,
                // ilk kategoriyi varsayılan olarak seç.
                val selectedCategory = currentState.selectedCategory
                    ?: categories.keys.firstOrNull()

                currentState.copy(
                    isLoading = soundsResult is Resource.Loading,
                    categories = categories,
                    selectedCategory = selectedCategory,
                    playerState = playerState
                )
            }
        }.launchIn(screenModelScope)
    }

    fun onEvent(event: HomeContract.Event) {
        when (event) {
            // YENİ: Kategori seçme event'ini işle
            is HomeContract.Event.OnCategorySelected -> {
                _state.update { it.copy(selectedCategory = event.category) }
            }

            is HomeContract.Event.OnSoundClick -> handleSoundClick(event.sound)
            is HomeContract.Event.OnVolumeChange -> adjustVolumeUseCase(event.soundId, event.volume)
            is HomeContract.Event.OnSettingsClick -> sendEffect(HomeContract.Effect.NavigateToSettings)
        }
    }

    private fun handleSoundClick(sound: Sound) {
        screenModelScope.launch {
            val isPlaying = state.value.playerState.activeSounds.any { it.id == sound.id }

            if (isPlaying) {
                stopSoundUseCase(sound.id)
            } else {
                // KONTROL: Ses indirilmiş mi?
                if (sound.isDownloaded) {
                    // İndirilmişse direkt çal
                    when (val result = playSoundUseCase(sound.id)) {
                        is Resource.Error -> {
                            val errorMessage = UiText.DynamicString(
                                result.error.message ?: "An unknown error occurred."
                            )
                            sendEffect(HomeContract.Effect.ShowSnackbar(errorMessage))
                        }

                        else -> { /* Başarılı */
                        }
                    }
                } else {
                    // İndirilmemişse önce indir (Zaten içinde playSound çağırıyor)
                    downloadSound(sound)
                }
            }
        }
    }

    private fun sendEffect(effectToSend: HomeContract.Effect) {
        screenModelScope.launch {
            _effect.send(effectToSend)
        }
    }

    private fun downloadSound(sound: Sound) = screenModelScope.launch {
        // 1. UI Güncellemesi: İndirme başladığı için spinner'ı aktif et
        _state.update { it.copy(downloadingSoundIds = it.downloadingSoundIds + sound.id) }

        // 2. UseCase Çağrısı: İndirmeyi başlat (IO Thread içinde çalışır)
        val isSuccess = downloadSingleSoundUseCase(sound)

        // 3. UI Güncellemesi: İndirme bitti, spinner'ı kaldır
        _state.update { it.copy(downloadingSoundIds = it.downloadingSoundIds - sound.id) }

        // 4. Sonuç Yönetimi
        if (isSuccess) {
            // Başarılıysa bekletmeden çalmaya başla (Otomatik Play)
            playSoundUseCase(sound.id)
        } else {
            // Hata varsa kullanıcıya bildir
            sendEffect(HomeContract.Effect.ShowSnackbar(UiText.Resource(Res.string.download_failed)))
        }
    }
}