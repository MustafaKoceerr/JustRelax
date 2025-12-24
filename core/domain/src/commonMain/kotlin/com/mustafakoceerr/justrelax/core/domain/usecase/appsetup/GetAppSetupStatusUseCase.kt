package com.mustafakoceerr.justrelax.core.domain.usecase.appsetup

import com.mustafakoceerr.justrelax.core.domain.repository.appsetup.AppSetupRepository
import kotlinx.coroutines.flow.Flow

/**
 * Onboarding işleminin tamamlanıp tamamlanmadığını kontrol eder.
 * Router (Navigasyon) kararı için kullanılır.
 */
class GetAppSetupStatusUseCase(
    private val appSetupRepository: AppSetupRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return appSetupRepository.isStarterPackInstalled
    }
}
