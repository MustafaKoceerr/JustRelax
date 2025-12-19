package com.mustafakoceerr.justrelax.core.domain.repository

import kotlinx.coroutines.flow.Flow

/*
Bu arayüzün değişmesi için tek sebep: Uygulamanın açılış/kurulum (Onboarding) akışının değişmesi.

 */
/**
 * Sorumluluk: Uygulamanın yaşam döngüsü ve kurulum durumlarını yönetmek.
 * Örn: Başlangıç paketi indi mi? Onboarding tamamlandı mı?
 */
interface AppSetupRepository {

    /**
     * "Starter Pack" (Başlangıç sesleri) indirme işlemi tamamlandı mı?
     * UI bu akışı dinler ve kullanıcıyı Setup ekranına veya Ana ekrana yönlendirir.
     */
    val isStarterPackInstalled: Flow<Boolean>

    /**
     * Kurulum tamamlandığında bu flag işaretlenir.
     */
    suspend fun setStarterPackInstalled(installed: Boolean)
}