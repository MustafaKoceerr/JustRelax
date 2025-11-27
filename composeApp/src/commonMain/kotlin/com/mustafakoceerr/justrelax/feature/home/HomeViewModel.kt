package com.mustafakoceerr.justrelax.feature.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.navigation.AppNavigator
import com.mustafakoceerr.justrelax.core.sound.domain.model.Sound
import com.mustafakoceerr.justrelax.core.sound.domain.model.SoundCategory
import com.mustafakoceerr.justrelax.core.sound.domain.player.SoundPlayer
import com.mustafakoceerr.justrelax.core.sound.domain.repository.SoundRepository
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeEffect
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeIntent
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeState
import com.mustafakoceerr.justrelax.feature.settings.SettingsScreen
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel (
    private val repository: SoundRepository,
    private val soundPlayer: SoundPlayer,
    private val navigator: AppNavigator
): ScreenModel{

    // _state'i private tutup dışarıya read-only açıyoruz.
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    // Efektleri (Navigasyon vb.) tek seferlik iletmek için channel kullanılır.
    private val _effect = Channel<HomeEffect>()
    val effect = _effect.receiveAsFlow()

    // Tüm seslerin ham listesini bellekte tutalım (filtreleme için)
    private var allSoundsCache: List<Sound> = emptyList()

    init {
        processIntent(HomeIntent.LoadData)
    }

    fun processIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.LoadData -> loadSounds()
            is HomeIntent.SelectCategory -> selectCategory(intent.category)
            is HomeIntent.ToggleSound -> toggleSound(intent.sound)
            is HomeIntent.ChangeVolume -> changeVolume(intent.soundId, intent.volume)
            is HomeIntent.StopAllSounds -> stopAll()
            is HomeIntent.ToggleMasterPlayPause -> toggleMaster()
            is HomeIntent.SettingsClicked -> navigateToSettings()
        }
    }

    private fun loadSounds(){
        screenModelScope.launch {
            repository.getSounds().collect { sounds->
                allSoundsCache = sounds
                // İlk açılışta mevcut kategoriye göre filtreleme
                filterSoundsByCategory(_state.value.selectedCategory)
            }
        }
    }

    private fun selectCategory(category: SoundCategory){
        _state.update { it.copy(selectedCategory = category) }
        filterSoundsByCategory(category)
    }

    private fun filterSoundsByCategory(category: SoundCategory){
        val filtered = allSoundsCache.filter { it.category == category }
        _state.update { it.copy(sounds = filtered, isLoading = false) }
    }

    private fun toggleSound(sound: Sound){
        val currentActive = _state.value.activeSounds
        if (currentActive.containsKey(sound.id)){
            // çalıyorsa durdur
            soundPlayer.stop(sound.id)
            _state.update {
                it.copy(activeSounds = currentActive - sound.id)
            }
        }else{
            // Çalmıyorsa başlat (varsayılan ses 0.5f)
            screenModelScope.launch {
                soundPlayer.play(sound,0.5f)
                _state.update {
                    it.copy(
                        activeSounds = currentActive + (sound.id to 0.5f),
                        isMasterPlaying = true // Yeni ses açıldıysa master play olur
                    )
                }
            }
        }
    }

    private fun changeVolume(soundId: String, volume: Float){
        // 1. UI State güncelle (slider akıcı olsun)
        _state.update {
            val newMap = it.activeSounds.toMutableMap()
            newMap[soundId] = volume
            it.copy(activeSounds = newMap)
        }
        // 2. player güncelle
        soundPlayer.setVolume(soundId, volume)
    }

    private fun stopAll(){
        soundPlayer.stopAll()
        _state.update { it.copy(activeSounds = emptyMap(), isMasterPlaying = false) }
    }

    private fun toggleMaster(){
// Bu özellik biraz kompleks, şimdilik sadece görsel state'i değiştiriyoruz.
        // Gerçekte: Tüm sesleri pause/resume yapmak native tarafta iterate gerektirir.
        // Şimdilik basitçe state değişimi:
        val newValue = !_state.value.isMasterPlaying
        _state.update { it.copy(isMasterPlaying = newValue)}

        // TODO: SoundPlayer'a pauseAll / resumeAll eklenirse burası güncellenmeli.
        if(!newValue) soundPlayer.stopAll() // Şimdilik pause yerine stopAll yapalım güvenli olsun.
    }

    private fun navigateToSettings(){
        screenModelScope.launch {
            navigator.navigate(SettingsScreen) // SettingsScreen nesnesini feature modülde import etmen gerek
        }
    }

    override fun onDispose() {
        super.onDispose()
        // Ekran ölürken kaynakları serbest bırakmak güvenlidir
        soundPlayer.release()
    }

}// end of the class