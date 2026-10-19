package com.mustafakoceerr.justrelax.feature.mysounds

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindowScene
import platform.UniformTypeIdentifiers.UTTypeAudio
import platform.darwin.NSObject

/**
 * The system document picker for audio files. It hands over a copy (`asCopy`), so no
 * security-scoped access is needed; the importer copies it again into the app's cache.
 */
@Composable
actual fun rememberAudioPicker(onResult: (uri: String?) -> Unit): () -> Unit {
    val currentOnResult by rememberUpdatedState(onResult)
    // UIKit keeps only a weak reference to the delegate, so the composition holds it.
    val delegate = remember { AudioPickerDelegate { currentOnResult(it) } }
    return remember(delegate) {
        {
            val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeAudio), asCopy = true)
            picker.delegate = delegate
            picker.allowsMultipleSelection = false
            topViewController()?.presentViewController(picker, animated = true, completion = null)
                ?: currentOnResult(null)
        }
    }
}

private class AudioPickerDelegate(private val onResult: (String?) -> Unit) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        onResult((didPickDocumentsAtURLs.firstOrNull() as? NSURL)?.absoluteString)
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onResult(null)
    }
}

private fun topViewController(): UIViewController? {
    val window = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .firstNotNullOfOrNull { it.keyWindow }
    var top = window?.rootViewController
    while (top?.presentedViewController != null) top = top.presentedViewController
    return top
}
