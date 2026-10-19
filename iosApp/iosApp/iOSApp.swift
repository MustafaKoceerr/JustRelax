import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    // Firebase and then Koin start before the first scene.
    init() {
        let firebase = FirebaseReporting.configureIfAvailable()
        KoinHelperKt.doInitKoin(analytics: firebase?.0, crashReporter: firebase?.1)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
