package com.mustafakoceerr.justrelax.feature.saved.domain.usecase

sealed interface SaveMixResult{
    data object Success: SaveMixResult

    sealed interface Error: SaveMixResult{
        data object EmptyName : Error      // İsim boş
        data object NoSounds : Error       // Ses seçilmemiş
        data object NameAlreadyExists : Error // Aynı isimde mix var
    }
}