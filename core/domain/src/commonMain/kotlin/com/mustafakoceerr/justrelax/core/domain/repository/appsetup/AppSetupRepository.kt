package com.mustafakoceerr.justrelax.core.domain.repository.appsetup

import kotlinx.coroutines.flow.Flow

interface AppSetupRepository {
    val isStarterPackInstalled: Flow<Boolean>
    suspend fun setStarterPackInstalled(installed: Boolean)
}