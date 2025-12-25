package com.mustafakoceerr.justrelax

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.SyncLanguageWithSystemUseCase
import kotlinx.coroutines.launch

/**
 * Sadece MainActivity'nin Android'e özel yaşam döngüsü olaylarını
 * yönetmek için kullanılan ViewModel.
 *
 * Not: Voyager'ın ScreenModel'i yerine standart AndroidX ViewModel kullanıyoruz,
 * çünkü bu bir Screen'e değil, Activity'ye bağlı.
 */
class MainActivityViewModel(
    private val syncLanguageWithSystemUseCase: SyncLanguageWithSystemUseCase
) : ViewModel() {

    /**
     * onResume'da çağrılır.
     */
    fun onResume() {
        viewModelScope.launch {
            try {
                syncLanguageWithSystemUseCase()
            } catch (e: Exception) {
                // Hata olursa logla
            }
        }
    }
}