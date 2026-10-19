import Shared
import SwiftUI

/// The tab bar's tabs, in the order of the Android app's bar.
enum AppTab: CaseIterable, Hashable {
    case home, mixer, discover, ai, saved

    var title: LocalizedStringResource {
        switch self {
        case .home: .tabHome
        case .mixer: .tabMixer
        case .discover: .tabDiscover
        case .ai: .tabAi
        case .saved: .tabSaved
        }
    }

    var systemImage: String {
        switch self {
        case .home: "waveform"
        case .mixer: "dice.fill"
        case .discover: "globe"
        case .ai: "sparkles"
        case .saved: "bookmark.fill"
        }
    }

    /// The Android test tag (`tab_home`), so UI tests find a tab the same way in any language.
    var accessibilityIdentifier: String {
        TestTags.shared.tab(name: "\(self)")
    }
}
