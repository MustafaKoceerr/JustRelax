import SwiftUI

/// The screen for a pushed route.
struct AppDestination: View {
    let route: AppRoute

    var body: some View {
        switch route {
        case .mySounds:
            MySoundsScreen()
        case .freesound:
            FreesoundScreen(isTab: false)
        case .soundEditor(let request):
            SoundEditorScreen(request: request)
        }
    }
}
