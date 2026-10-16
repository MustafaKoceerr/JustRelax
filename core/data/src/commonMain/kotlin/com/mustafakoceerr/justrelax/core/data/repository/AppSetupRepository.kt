package com.mustafakoceerr.justrelax.core.data.repository

import kotlinx.coroutines.flow.Flow

interface AppSetupRepository {
    val isStarterPackInstalled: Flow<Boolean>
    suspend fun setStarterPackInstalled(installed: Boolean)
}