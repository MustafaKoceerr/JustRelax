package com.mustafakoceerr.justrelax

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { JustRelaxApp() }
