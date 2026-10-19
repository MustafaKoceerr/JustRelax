import SwiftUI
import Shared

@main
struct iOSApp: App {
    // Firebase and then Koin start before the first scene.
    init() {
        let firebase = FirebaseReporting.configureIfAvailable()
        KoinHelperKt.doInitKoin(analytics: firebase?.0, crashReporter: firebase?.1)
    }

    // SwiftUI.Scene: the shared framework has a Scene too (the soundscape).
    var body: some SwiftUI.Scene {
        WindowGroup {
            ContentView()
        }
    }
}
