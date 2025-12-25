package com.mustafakoceerr.justrelax.core.domain.usecase.appsetup

import com.mustafakoceerr.justrelax.core.domain.repository.appsetup.AppSetupRepository


/**
 * Onboarding başarıyla bittiğinde çağrılır.
 */
class SetAppSetupFinishedUseCase(
    private val appSetupRepository: AppSetupRepository
) {
    suspend operator fun invoke() {
        appSetupRepository.setStarterPackInstalled(true)
    }
}