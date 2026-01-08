package com.mustafakoceerr.justrelax.feature.onboarding.di

import com.mustafakoceerr.justrelax.feature.onboarding.OnboardingViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val onboardingModule = module {
    factoryOf(::OnboardingViewModel)
}