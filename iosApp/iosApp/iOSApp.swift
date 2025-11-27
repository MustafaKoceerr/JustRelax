import SwiftUI
import ComposeApp // İŞTE SİHİRLİ SATIR BU!

@main
struct iOSApp: App {
    // Koin'i başlatmak için init() metodunu kullanıyoruz.
    init() {
        KoinInitializerKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
