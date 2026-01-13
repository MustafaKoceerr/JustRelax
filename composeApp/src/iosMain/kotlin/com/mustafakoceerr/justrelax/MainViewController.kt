package com.mustafakoceerr.justrelax

import androidx.compose.runtime.*
import androidx.compose.ui.window.ComposeUIViewController
import com.mustafakoceerr.justrelax.core.ui.util.LocalWindowWidthSize
import com.mustafakoceerr.justrelax.core.ui.util.WindowWidthSize
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.*

fun MainViewController(): UIViewController = AdaptiveComposeController()

/**
 * Bu özel UIViewController, iOS'in "Size Class" değişikliklerini dinler
 * ve bunu Compose dünyasına aktarır.
 */
private class AdaptiveComposeController : UIViewController(null, null) {

    private var windowWidthSize by mutableStateOf(WindowWidthSize.COMPACT)

    override fun viewDidLoad() {
        super.viewDidLoad()

        val composeViewController = ComposeUIViewController {
            CompositionLocalProvider(LocalWindowWidthSize provides windowWidthSize) {
                JustRelaxApp()
            }
        }

        addChildViewController(composeViewController)
        view.addSubview(composeViewController.view)
        composeViewController.didMoveToParentViewController(this)

        updateWindowSizeClass()
    }

    override fun traitCollectionDidChange(previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        updateWindowSizeClass()
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun updateWindowSizeClass() {
        val width = view.bounds.useContents { size.width }
        val horizontalSizeClass = traitCollection.horizontalSizeClass

        // NİHAİ DÜZELTME: Kotlin/Native'in çevirdiği doğru enum isimleri.
        val newSize = when (horizontalSizeClass) {
            UIUserInterfaceSizeClassCompact -> WindowWidthSize.COMPACT
            UIUserInterfaceSizeClassRegular -> {
                if (width < 840.0) WindowWidthSize.MEDIUM else WindowWidthSize.EXPANDED
            }

            else -> WindowWidthSize.COMPACT
        }

        if (newSize != windowWidthSize) {
            windowWidthSize = newSize
        }
    }
}