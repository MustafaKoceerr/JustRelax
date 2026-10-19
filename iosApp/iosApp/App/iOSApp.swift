import SwiftUI
import Shared

@main
struct iOSApp: App {
    @State private var model: AppModel
    @State private var banners = BannerCenter()

    // Firebase and then Koin start before the first scene; the model needs Koin.
    init() {
        let firebase = FirebaseReporting.configureIfAvailable()
        KoinHelperKt.doInitKoin(analytics: firebase?.0, crashReporter: firebase?.1)
        _model = State(initialValue: AppModel())
    }

    // SwiftUI.Scene: the shared framework has a Scene too (the soundscape).
    var body: some SwiftUI.Scene {
        WindowGroup {
            MainTabView()
                // The rounded system font stands in for Android's Nunito.
                .fontDesign(.rounded)
                .environment(\.bannerCenter, banners)
                .preferredColorScheme(model.theme.colorScheme)
                .task { await model.observe() }
        }
    }
}
