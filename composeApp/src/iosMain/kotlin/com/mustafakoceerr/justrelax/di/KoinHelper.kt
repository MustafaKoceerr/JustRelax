package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.di.initKoin as initCommonKoin

/**
 * Entry point for Koin initialization on iOS.
 *
 * This function is intended to be called from the iOS App Delegate (Swift).
 * It acts as a wrapper around the common [initCommonKoin] function to ensure
 * compatibility with Swift/Objective-C, which may struggle with Kotlin's
 * default arguments and lambda syntax.
 */
fun initKoin() {
    initCommonKoin {
        // iOS-specific modules can be injected here if necessary.
    }
}