package com.mustafakoceerr.justrelax.feature.settings.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.data.repository.ConsentRepository
import com.mustafakoceerr.justrelax.core.data.repository.LegalRepository
import com.mustafakoceerr.justrelax.core.ui.system.SystemLauncher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Asks once whether usage analytics and crash reports may be sent; either answer opens the app. */
class ConsentViewModel(
    private val consentRepository: ConsentRepository,
    private val legalRepository: LegalRepository,
    private val systemLauncher: SystemLauncher,
) : ViewModel() {

    private val done = MutableStateFlow(false)
    val isDone: StateFlow<Boolean> = done.asStateFlow()

    fun answer(granted: Boolean) {
        viewModelScope.launch {
            consentRepository.setConsent(granted)
            done.value = true
        }
    }

    fun openPrivacyPolicy() {
        viewModelScope.launch { systemLauncher.openUrl(legalRepository.getPrivacyPolicyUrl()) }
    }
}
