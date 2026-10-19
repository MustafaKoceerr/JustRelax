import SwiftUI

/// One tab: its screen in a navigation stack that the navigator can push onto.
struct TabRoot: View {
    let tab: AppTab
    @Bindable var navigator: AppNavigator

    var body: some View {
        NavigationStack(path: $navigator[path: tab]) {
            screen
                .navigationDestination(for: AppRoute.self) { route in
                    AppDestination(route: route)
                }
        }
        .bannerHost(isActive: navigator.selected == tab)
    }

    @ViewBuilder private var screen: some View {
        switch tab {
        case .home: LibraryScreen()
        case .mixer: MixerScreen()
        case .discover: FreesoundScreen(isTab: true)
        case .ai: AiScreen()
        case .saved: SavedScreen { navigator.selected = .mixer }
        }
    }
}
