package com.mustafakoceerr.justrelax.core.audio

data class MixerState(
    val isMasterPlaying: Boolean = false, // Genel Play/Pause durumu
    val activeSounds: Map<String, ActiveSound> = emptyMap(), // Çalan sesler ve detayları
    val isLoading: Boolean = false
) {
    // Notification başlığı için yardımcı fonksiyon
    fun getNotificationSubtitle(): String {
        return activeSounds.values.joinToString(", ") { it.sound.name }
    }
}

data class ActiveSound(
    val sound: Sound,
    val targetVolume: Float, // Kullanıcının ayarladığı ses
    val currentVolume: Float, // Fade işlemi sırasındaki anlık ses
    val isfadingIn: Boolean = false
)