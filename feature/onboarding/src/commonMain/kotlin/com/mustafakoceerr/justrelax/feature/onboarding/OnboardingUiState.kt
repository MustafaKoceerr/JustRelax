package com.mustafakoceerr.justrelax.feature.onboarding

import com.mustafakoceerr.justrelax.core.ui.util.UiText

enum class OnboardingStatus { LOADING_CONFIG, NO_INTERNET, CHOOSING, DOWNLOADING, COMPLETED }

data class DownloadOption(
    val totalSizeMb: Float,
    val soundCount: Int,
)

data class OnboardingUiState(
    val status: OnboardingStatus = OnboardingStatus.LOADING_CONFIG,
    val starterPack: DownloadOption? = null,
    val fullLibrary: DownloadOption? = null,
    val downloadProgress: Float = 0f,
    val userMessage: UiText? = null,
)
