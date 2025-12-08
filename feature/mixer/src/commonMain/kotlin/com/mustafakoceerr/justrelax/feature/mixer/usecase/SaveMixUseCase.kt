package com.mustafakoceerr.justrelax.feature.mixer.usecase

import com.mustafakoceerr.justrelax.core.domain.repository.SavedMixRepository

class SaveMixUseCase(
    private val repository: SavedMixRepository
) {
    /**
     * Validasyon kurallarını uygular ve kayıt işlemini dener.
     * @return SaveMixResult (Success veya Error tipleri)
     */
    suspend operator fun invoke(name: String, sounds: Map<String, Float>): SaveMixResult {
        // 1. Kural: İsim boş olamaz
        if (name.isBlank()) {
            return SaveMixResult.Error.EmptyName
        }

        // 2. Kural: Ses listesi boş olamaz (Mixer ekranında zaten kontrol ediyoruz ama Domain'de de olmalı)
        if (sounds.isEmpty()) {
            return SaveMixResult.Error.NoSounds
        }

        // 3. Kural: Aynı isimde kayıt olamaz
        if (repository.isMixNameExists(name)) {
            return SaveMixResult.Error.NameAlreadyExists
        }

        // Her şey yolunda -> Kaydet
        repository.saveMix(name, sounds)
        return SaveMixResult.Success
    }
}