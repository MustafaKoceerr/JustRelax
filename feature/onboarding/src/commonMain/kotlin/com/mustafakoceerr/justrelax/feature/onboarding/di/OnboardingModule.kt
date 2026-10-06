package com.mustafakoceerr.justrelax.feature.onboarding.di

import com.mustafakoceerr.justrelax.feature.onboarding.OnboardingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val onboardingModule = module {
    viewModelOf(::OnboardingViewModel)
}