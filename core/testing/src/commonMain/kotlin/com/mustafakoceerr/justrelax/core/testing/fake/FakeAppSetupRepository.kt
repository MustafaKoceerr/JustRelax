package com.mustafakoceerr.justrelax.core.testing.fake

import com.mustafakoceerr.justrelax.core.domain.repository.appsetup.AppSetupRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAppSetupRepository(installed: Boolean = false) : AppSetupRepository {

    override val isStarterPackInstalled = MutableStateFlow(installed)

    override suspend fun setStarterPackInstalled(installed: Boolean) {
        isStarterPackInstalled.value = installed
    }
}
