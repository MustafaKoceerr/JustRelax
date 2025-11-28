package com.mustafakoceerr.justrelax.feature.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.sound.domain.model.Sound
import com.mustafakoceerr.justrelax.core.sound.domain.model.SoundCategory
import com.mustafakoceerr.justrelax.core.sound.domain.repository.SoundRepository
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeEffect
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeIntent
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Burada navigasyonu direkt yapmayarak Effect ile ui'a git emri vererek, viewModel'imizin voyager (navigasyon kütüphanesi)'nden bağımsız
 * olmasını sağlıyoruz.
 */
class HomeViewModel(
    private val repository: SoundRepository,
) : ScreenModel {

    // _state'i private tutup dışarıya read-only açıyoruz.
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    // Effectleri iletmek için Channel kullanıyoruz (Hot Stream)
    // Channel.BUFFERED: Alıcı hazır olmasa bile eventi tutar.
    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()
    private var allSoundsCache: List<Sound> = emptyList()

    init {
        processIntent(HomeIntent.LoadData)
    }

    fun processIntent(intent: HomeIntent){
        when(intent){
            is HomeIntent.LoadData -> loadSounds()
            is HomeIntent.SelectCategory -> selectCategory(intent.category)
            is HomeIntent.SettingsClicked -> onSettingsClicked()
        }
    }

    private fun loadSounds(){
        screenModelScope.launch {
            repository.getSounds().collect { sounds->
                allSoundsCache = sounds
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
        _state.update { it.copy(sounds = filtered, isLoading = false)}
    }

    private fun onSettingsClicked(){
        // Navigasyon yapmıyoruz, "Git" emri veriyoruz.
        screenModelScope.launch {
            _effect.send(HomeEffect.NavigateToSettings)
        }
    }

}// end of the class