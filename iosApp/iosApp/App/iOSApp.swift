import SwiftUI
import Shared

@main
struct iOSApp: App {
    @State private var model: AppModel
    @State private var launch: LaunchModel
    @State private var banners = BannerCenter()

    // Firebase and then Koin start before the first scene; the models need Koin.
    init() {
        let firebase = FirebaseReporting.configureIfAvailable()
        KoinHelperKt.doInitKoin(analytics: firebase?.0, crashReporter: firebase?.1)
        _model = State(initialValue: AppModel())
        _launch = State(initialValue: LaunchModel())
    }

    // SwiftUI.Scene: the shared framework has a Scene too (the soundscape).
    var body: some SwiftUI.Scene {
        WindowGroup {
            RootView(launch: launch)
                // The rounded system font stands in for Android's Nunito.
                .fontDesign(.rounded)
                .environment(\.bannerCenter, banners)
                .preferredColorScheme(model.theme.colorScheme)
                .task { await model.observe() }
        }
    }
}
