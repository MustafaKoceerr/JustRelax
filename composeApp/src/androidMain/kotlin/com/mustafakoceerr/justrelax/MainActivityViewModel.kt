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
     * Activity'den manuel olarak çağrılır.
     * Sistem dili ile App'in bildiği dili senkronize eder.
     */
    fun checkSystemLanguage() {
        viewModelScope.launch {
            try {
                // UseCase, sistemin o anki dili ile DataStore'daki dili karşılaştırır.
                // Farklıysa DataStore'u günceller.
                // DataStore güncellenince MainViewModel (Compose tarafı) bunu Flow ile yakalar ve UI güncellenir.
                syncLanguageWithSystemUseCase()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}