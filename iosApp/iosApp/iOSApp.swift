import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    // Koin starts before the first scene.
    init() {
        KoinHelperKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
