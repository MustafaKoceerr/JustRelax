package com.mustafakoceerr.justrelax.feature.saved.di

import com.mustafakoceerr.justrelax.feature.saved.SavedViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val savedModule = module {
    viewModelOf(::SavedViewModel)
}
