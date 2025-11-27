import SwiftUI
import ComposeApp // İŞTE SİHİRLİ SATIR BU!

@main
struct iOSApp: App {
    // Koin'i başlatmak için init() metodunu kullanıyoruz.
    init() {
        // Kotlin tarafındaki fonksiyonu çağırıyoruz.
        // Dosya adı "KoinHelper.kt" olduğu için "KoinHelperKt" sınıfı oluşur.
        KoinHelperKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
