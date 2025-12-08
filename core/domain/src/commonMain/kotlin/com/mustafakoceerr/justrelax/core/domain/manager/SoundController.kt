package com.mustafakoceerr.justrelax.core.domain.manager

// TimerManager sadece bu arayüzü bilecek.
// SoundManager'ın diğer karmaşık işlerini bilmesine gerek yok.
interface SoundController {
    fun stopAll()
    // İleride "pauseAll" veya "resumeAll" gerekirse buraya eklersin.
}