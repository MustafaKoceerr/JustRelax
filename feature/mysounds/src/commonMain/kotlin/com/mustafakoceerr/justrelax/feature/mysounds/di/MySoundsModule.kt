package com.mustafakoceerr.justrelax.feature.mysounds.di

import com.mustafakoceerr.justrelax.feature.mysounds.MySoundsViewModel
import com.mustafakoceerr.justrelax.feature.mysounds.editor.SoundEditorViewModel
import com.mustafakoceerr.justrelax.feature.mysounds.record.RecorderViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mySoundsModule = module {
    viewModelOf(::MySoundsViewModel)
    viewModelOf(::RecorderViewModel)
    // The file to edit comes from the route: parametersOf(sourcePath, suggestedName).
    viewModel { (sourcePath: String, suggestedName: String?) ->
        SoundEditorViewModel(sourcePath, suggestedName, get(), get(), get())
    }
}
