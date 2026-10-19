package com.mustafakoceerr.justrelax.di

import androidx.lifecycle.ViewModel
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.getOriginalKotlinClass
import kotlin.reflect.KClass

/** [ViewModelHost.get] for Swift, which passes the ViewModel's class (`LibraryViewModel.self`). */
@OptIn(BetaInteropApi::class)
fun ViewModelHost.resolve(viewModelClass: ObjCClass, parameters: List<Any?>): ViewModel {
    @Suppress("UNCHECKED_CAST")
    val kotlinClass = requireNotNull(getOriginalKotlinClass(viewModelClass)) { "$viewModelClass is not a Kotlin class" }
        as KClass<out ViewModel>
    return get(kotlinClass, parameters)
}
