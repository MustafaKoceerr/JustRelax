import SwiftUI

/// The screen for a pushed route.
struct AppDestination: View {
    let route: AppRoute

    var body: some View {
        switch route {
        case .freesound:
            FreesoundScreen(isTab: false)
        case .soundEditor:
            // The editor comes with the My sounds screen.
            ContentUnavailableView(String(localized: .mysoundsEditorTitle), systemImage: "waveform")
        }
    }
}
