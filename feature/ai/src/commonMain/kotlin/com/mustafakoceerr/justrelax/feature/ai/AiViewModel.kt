package com.mustafakoceerr.justrelax.feature.ai

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.audio.SoundManager
import com.mustafakoceerr.justrelax.core.domain.repository.SavedMixRepository
import com.mustafakoceerr.justrelax.core.model.ActiveSoundInfo
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.ai.domain.repository.AiService
import com.mustafakoceerr.justrelax.feature.ai.domain.usecase.ObserveDownloadedSoundsUseCase
import com.mustafakoceerr.justrelax.feature.ai.domain.usecase.PlayAiMixUseCase
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiEffect
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiIntent
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiState
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiUiState
import justrelax.feature.ai.generated.resources.Res
import justrelax.feature.ai.generated.resources.error_generic
import justrelax.feature.ai.generated.resources.error_mix_name_exists
import justrelax.feature.ai.generated.resources.error_no_sounds_found
import justrelax.feature.ai.generated.resources.error_no_sounds_to_save
import justrelax.feature.ai.generated.resources.success_mix_saved
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AiViewModel(
    private val aiService: AiService,
    private val playAiMixUseCase: PlayAiMixUseCase,
    private val observeDownloadedSoundsUseCase: ObserveDownloadedSoundsUseCase,
    // getActiveSoundsUseCase kaldırıldı, doğrudan SoundManager'dan mapleyeceğiz (Daha performanslı)
    private val soundManager: SoundManager,
    private val savedMixRepository: SavedMixRepository
) : ScreenModel {

    private val _state = MutableStateFlow(AiState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeDownloadedSounds()
        observeActiveSounds()
    }

    private fun observeDownloadedSounds() {
        screenModelScope.launch {
            observeDownloadedSoundsUseCase().collect { downloadedSounds ->
                _state.update { it.copy(showDownloadSuggestion = downloadedSounds.size < 20) }
            }
        }
    }

    // DÜZELTME 1: Active Sounds Mapping
    // Önceki kodda UseCase döngü içinde çağrıldığı için liste katlanarak büyüyordu.
    private fun observeActiveSounds() {
        screenModelScope.launch {
            soundManager.state.map { it.activeSounds.values }.collect { activeSounds ->
                // SoundManager'daki 'ActiveSound' objesini UI için 'ActiveSoundInfo'ya çeviriyoruz.
                val activeSoundInfos = activeSounds.map { activeSound ->
                    ActiveSoundInfo(
                        id = activeSound.sound.id,
                        name = activeSound.sound.name,
                        volume = activeSound.currentVolume
                    )
                }

                _state.update {
                    it.copy(
                        activeSoundsInfo = activeSoundInfos,
                        // Eğer hiç ses çalmıyorsa, context'i otomatik kapat
                        isContextEnabled = if (activeSoundInfos.isEmpty()) false else it.isContextEnabled
                    )
                }
            }
        }
    }

    fun processIntent(intent: AiIntent) {
        when (intent) {
            is AiIntent.UpdatePrompt -> _state.update { it.copy(prompt = intent.text) }
            is AiIntent.SelectSuggestion -> _state.update { it.copy(prompt = intent.text) }
            is AiIntent.ToggleContext -> _state.update { it.copy(isContextEnabled = !it.isContextEnabled) }
            is AiIntent.EditPrompt -> _state.update { it.copy(uiState = AiUiState.IDLE) }
            is AiIntent.GenerateMix -> {
                println("TAKİP_3: GenerateMix tetiklendi") // <--- EKLE
                generateMix(isRegenerate = false)
            }
            is AiIntent.RegenerateMix -> generateMix(isRegenerate = true)
            is AiIntent.ClickDownloadSuggestion -> screenModelScope.launch { _effect.send(AiEffect.NavigateToSettings) }
            is AiIntent.ConfirmSaveMix -> saveCurrentMix(intent.mixName)
            is AiIntent.UpdateVolume -> soundManager.onVolumeChange(intent.soundId, intent.volume)
            AiIntent.ShowSaveDialog -> { /* UI handles this */ }
        }
    }

    private fun generateMix(isRegenerate: Boolean) {
        val currentPrompt = state.value.prompt
        // Prompt boşsa veya zaten yükleniyorsa işlem yapma
        if (currentPrompt.isBlank() || state.value.uiState is AiUiState.LOADING) return

        screenModelScope.launch {
            // 1. UI'ı Yükleniyor moduna al
            _state.update { it.copy(uiState = AiUiState.LOADING) }

            soundManager.stopAll()

            val finalPrompt = buildPromptWithContext(currentPrompt, isRegenerate)

            // 2. Servise git
            val result = aiService.generateMix(finalPrompt)

            result.onSuccess { mixResponse ->
                // 3. Başarılı cevap geldi, sesleri çalmayı dene
                val wasSuccessful = playAiMixUseCase(mixResponse)

                if (wasSuccessful) {
                    // --- BAŞARILI SENARYO ---
                    // Sesler çalmaya başladı, UI'ı SUCCESS yap.
                    val generatedSounds = soundManager.state.value.activeSounds.values.map { it.sound }

                    _state.update {
                        it.copy(
                            uiState = AiUiState.SUCCESS(
                                mixName = mixResponse.mixName,
                                mixDescription = mixResponse.description,
                                sounds = generatedSounds
                            )
                        )
                    }
                } else {
                    // --- HATA SENARYOSU 1: Cevap geldi ama sesler bulunamadı/indirilemedi ---
                    val errorMsg = UiText.StringResource(Res.string.error_no_sounds_found)

                    // Hem State'i ERROR yap (Ekranın ortasında hata ikonu vs. göstermek istersen)
                    _state.update { it.copy(uiState = AiUiState.ERROR(errorMsg)) }

                    // HEM DE SNACKBAR FIRLAT (Kullanıcıya bildirim ver)
                    _effect.send(AiEffect.ShowSnackbar(errorMsg))
                }
            }.onFailure { error ->
                // --- HATA SENARYOSU 2: API Hatası / İnternet Yok ---
                error.printStackTrace()

                val errorMsg = UiText.StringResource(Res.string.error_generic)

                _state.update { it.copy(uiState = AiUiState.ERROR(errorMsg)) }

                // SNACKBAR FIRLAT
                _effect.send(AiEffect.ShowSnackbar(errorMsg))
            }
        }
    }
    private fun buildPromptWithContext(prompt: String, isRegenerate: Boolean): String {
        val activeSounds = state.value.activeSoundsInfo
        if (!state.value.isContextEnabled || isRegenerate || activeSounds.isEmpty()) {
            return prompt
        }

        val context = activeSounds.joinToString(", ") { "${it.name} (volume: ${it.volume})" }
        return "Currently playing sounds are: $context. Now, based on this context, handle the following request: \"$prompt\""
    }

    private fun saveCurrentMix(mixName: String) {
        screenModelScope.launch {
            val activeSoundsMap = soundManager.state.value.activeSounds.mapValues { it.value.targetVolume }
            if (activeSoundsMap.isEmpty()) {
                _effect.send(AiEffect.ShowSnackbar(UiText.StringResource(Res.string.error_no_sounds_to_save)))
                return@launch
            }

            if (savedMixRepository.isMixNameExists(mixName)) {
                _effect.send(AiEffect.ShowSnackbar(UiText.StringResource(Res.string.error_mix_name_exists)))
                return@launch
            }

            savedMixRepository.saveMix(mixName, activeSoundsMap)
            _effect.send(AiEffect.ShowSnackbar(UiText.StringResource(Res.string.success_mix_saved, mixName)))
        }
    }
}