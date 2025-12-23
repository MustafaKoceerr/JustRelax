package com.mustafakoceerr.justrelax.feature.home.usecase

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

//class DismissBannerUseCase(
//    private val settingsRepository: SettingsRepository
//) {
//    @OptIn(ExperimentalTime::class)
//    suspend operator fun invoke() {
//        val now = Clock.System.now().toEpochMilliseconds()
//        settingsRepository.setLastDownloadPromptTime(now)
//    }
//}