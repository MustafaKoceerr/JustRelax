package com.mustafakoceerr.justrelax.core.domain.usecase.appsetup

import com.mustafakoceerr.justrelax.core.domain.repository.appsetup.AppSetupRepository
import kotlinx.coroutines.flow.Flow

class GetAppSetupStatusUseCase(
    private val appSetupRepository: AppSetupRepository
) {
    operator fun invoke(): Flow<Boolean> = appSetupRepository.isStarterPackInstalled
}