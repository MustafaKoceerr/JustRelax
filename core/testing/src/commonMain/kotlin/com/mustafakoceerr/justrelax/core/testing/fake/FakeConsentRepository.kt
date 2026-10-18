package com.mustafakoceerr.justrelax.core.testing.fake

import com.mustafakoceerr.justrelax.core.data.repository.ConsentRepository
import com.mustafakoceerr.justrelax.core.model.ConsentStatus
import kotlinx.coroutines.flow.MutableStateFlow

class FakeConsentRepository(initial: ConsentStatus = ConsentStatus.UNKNOWN) : ConsentRepository {
    override val consent = MutableStateFlow(initial)
    var appliedCount = 0
        private set

    override suspend fun setConsent(granted: Boolean) {
        consent.value = if (granted) ConsentStatus.GRANTED else ConsentStatus.DENIED
    }

    override suspend fun applyStoredConsent() {
        appliedCount++
    }
}
