package com.mustafakoceerr.justrelax.feature.settings.di

import com.mustafakoceerr.justrelax.feature.settings.SettingsViewModel
import com.mustafakoceerr.justrelax.feature.settings.consent.ConsentViewModel
import com.mustafakoceerr.justrelax.feature.settings.credits.CreditsViewModel
import com.mustafakoceerr.justrelax.feature.settings.support.SupportViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule = module {
    viewModelOf(::SettingsViewModel)
    viewModelOf(::CreditsViewModel)
    viewModelOf(::ConsentViewModel)
    viewModelOf(::SupportViewModel)
}