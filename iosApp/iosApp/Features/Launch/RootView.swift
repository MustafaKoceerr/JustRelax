import SwiftUI

/// The splash until the app is ready, then the tabs, with the analytics question over them on a
/// first launch.
struct RootView: View {
    let launch: LaunchModel
    @State private var consent: ConsentModel?

    var body: some View {
        ZStack {
            switch launch.phase {
            case .splash:
                SplashView()
                    .transition(.opacity)
            case .main:
                MainTabView()
                    .transition(.opacity)
            }
        }
        .animation(.easeInOut, value: launch.phase)
        .fullScreenCover(item: $consent) { model in
            ConsentView(model: model) {
                consent = nil
            }
        }
        .onChange(of: launch.phase) { _, phase in
            if phase == .main(asksConsent: true) {
                consent = ConsentModel()
            }
        }
        .task {
            await launch.observe()
        }
    }
}
