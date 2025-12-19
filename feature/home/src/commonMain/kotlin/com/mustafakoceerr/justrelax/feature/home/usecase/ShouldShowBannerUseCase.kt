package com.mustafakoceerr.justrelax.feature.home.usecase

import com.mustafakoceerr.justrelax.feature.home.BANNER_SHOW_INTERVAL_MS
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ShouldShowBannerUseCase(
    private val settingsRepository: SettingsRepository
) {
    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(sounds: List<Sound>): Boolean {
        // 1. Kural: Eksik ses var mı?
        val missingCount = sounds.count { !it.isDownloaded }
        if (missingCount == 0) return false

        // 2. Kural: 3 gün geçti mi?
        val lastPrompt = settingsRepository.getLastDownloadPromptTime()
        val now = Clock.System.now().toEpochMilliseconds()

        return lastPrompt == 0L || (now - lastPrompt) > BANNER_SHOW_INTERVAL_MS
    }
}