package com.mustafakoceerr.justrelax.feature.onboarding.mvi

enum class OnboardingScreenStatus {
    LOADING_CONFIG,
    NO_INTERNET,
    CHOOSING,
    DOWNLOADING,
    COMPLETED,
    ERROR
}

data class DownloadOption(
    val totalSizeMb: Float,
    val soundCount: Int
)

data class OnboardingState(
    val status: OnboardingScreenStatus = OnboardingScreenStatus.LOADING_CONFIG,
    val initialOption: DownloadOption? = null,
    val allOption: DownloadOption? = null,
    val downloadProgress: Float = 0f
)

sealed interface OnboardingIntent {
    data object RetryLoadingConfig : OnboardingIntent
    data object DownloadInitial : OnboardingIntent
    data object DownloadAll : OnboardingIntent
}

sealed interface OnboardingEffect {
    data object NavigateToMainScreen : OnboardingEffect
    data class ShowError(val message: String) : OnboardingEffect
}