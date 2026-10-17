package com.mustafakoceerr.justrelax.feature.scene.di

import com.mustafakoceerr.justrelax.feature.scene.SceneViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sceneModule = module {
    viewModelOf(::SceneViewModel)
}
