import Observation
import SwiftUI

/// The selected tab and each tab's pushed screens, so any screen can open another.
@MainActor
@Observable
final class AppNavigator {
    var selected = AppTab.home
    var paths: [AppTab: [AppRoute]] = [:]

    /// One tab's pushed screens, for its navigation stack.
    subscript(path tab: AppTab) -> [AppRoute] {
        get { paths[tab, default: []] }
        set { paths[tab] = newValue }
    }

    /// Opens `route` on the tab in view.
    func push(_ route: AppRoute) {
        paths[selected, default: []].append(route)
    }

    func pop() {
        guard paths[selected]?.isEmpty == false else { return }
        paths[selected]?.removeLast()
    }
}

extension EnvironmentValues {
    /// The app's navigator; nil in previews that don't set one.
    @Entry var navigator: AppNavigator?
}
