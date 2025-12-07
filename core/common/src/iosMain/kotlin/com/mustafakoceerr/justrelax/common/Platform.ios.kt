package com.mustafakoceerr.justrelax.common

import platform.UIKit.UIDevice

actual fun platform(): String {
    return UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}