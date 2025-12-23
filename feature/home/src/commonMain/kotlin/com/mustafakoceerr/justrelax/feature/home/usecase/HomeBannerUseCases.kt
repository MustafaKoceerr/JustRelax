package com.mustafakoceerr.justrelax.feature.home.usecase

///**
// * Banner ile ilgili 3 tane Use Case'in var. Bunlar mantıksal olarak birbirine bağlı.
// * Bunları tek tek ViewModel'e vermek yerine, bir Wrapper (Paket) sınıfı içinde verebiliriz.
// *
// * Neden İyi?
// * ViewModel constructor'ı temizlenir.
// * Banner mantığı toplu durur.
// */
//data class HomeBannerUseCases(
//    val shouldShow: ShouldShowBannerUseCase,
//    val dismiss: DismissBannerUseCase,
//    val downloadAllMissingSounds: DownloadAllMissingSoundsUseCase
//)