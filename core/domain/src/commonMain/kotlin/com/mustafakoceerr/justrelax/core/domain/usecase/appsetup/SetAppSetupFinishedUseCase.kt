package com.mustafakoceerr.justrelax.core.domain.usecase.appsetup

import com.mustafakoceerr.justrelax.core.domain.repository.appsetup.AppSetupRepository

class SetAppSetupFinishedUseCase(
    private val appSetupRepository: AppSetupRepository
) {
    suspend operator fun invoke() = appSetupRepository.setStarterPackInstalled(true)
}