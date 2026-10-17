package com.mustafakoceerr.justrelax.core.data.repository

import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch

/**
 * Downloads the built-in starter pack in the background on a fresh install, so the library is
 * ready without asking the user anything. A failed download is tried again on the next [install].
 * [scope] outlives any screen.
 */
class StarterPackInstaller(
    private val soundRepository: SoundRepository,
    private val appSetupRepository: AppSetupRepository,
    private val scope: CoroutineScope,
) {
    private var job: Job? = null

    fun install() {
        if (job?.isActive == true) return
        job = scope.launch {
            if (appSetupRepository.isStarterPackInstalled.first()) return@launch
            if (soundRepository.downloadStarterPack().last() == DownloadStatus.Completed) {
                appSetupRepository.setStarterPackInstalled(true)
            }
        }
    }
}
