package com.mustafakoceerr.justrelax.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.CreationExtras
import org.koin.core.Koin
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform
import kotlin.reflect.KClass

/**
 * Keeps the ViewModels of one SwiftUI screen. On Android each navigation destination has a
 * ViewModelStore; SwiftUI has none, so a screen's model owns a host and clears it when the screen
 * goes away, which ends the ViewModels (onCleared) as leaving a destination does on Android.
 */
class ViewModelHost(private val koin: Koin = KoinPlatform.getKoin()) {

    private val store = ViewModelStore()

    /** The host's [viewModelClass], made by Koin with [parameters] the first time it is asked for. */
    fun <T : ViewModel> get(viewModelClass: KClass<T>, parameters: List<Any?> = emptyList()): T {
        val factory = object : ViewModelProvider.Factory {
            override fun <VM : ViewModel> create(modelClass: KClass<VM>, extras: CreationExtras): VM =
                koin.get(modelClass) { parametersOf(*parameters.toTypedArray()) }
        }
        return ViewModelProvider.create(store, factory)[viewModelClass]
    }

    fun clear() = store.clear()
}
