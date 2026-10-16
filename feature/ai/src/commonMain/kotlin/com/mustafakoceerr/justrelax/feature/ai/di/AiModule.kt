package com.mustafakoceerr.justrelax.feature.ai.di

import com.mustafakoceerr.justrelax.feature.ai.AiViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val aiModule = module {
    viewModelOf(::AiViewModel)
}
