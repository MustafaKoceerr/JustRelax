import Observation
import SwiftUI

/// Shows banners one at a time; a banner that arrives while another is showing waits its turn.
@MainActor
@Observable
final class BannerCenter {
    private(set) var current: Banner?
    @ObservationIgnored private var waiting: [Banner] = []

    func show(_ banner: Banner) {
        if current == nil {
            current = banner
        } else {
            waiting.append(banner)
        }
    }

    /// Closes the banner if it is still the one showing, and shows the next.
    func dismiss(_ banner: Banner) {
        guard current?.id == banner.id else { return }
        current = waiting.isEmpty ? nil : waiting.removeFirst()
    }

    func performAction(of banner: Banner) {
        banner.action?.perform()
        dismiss(banner)
    }
}

extension EnvironmentValues {
    /// The app's banner center; nil in previews that don't set one.
    @Entry var bannerCenter: BannerCenter?
}
