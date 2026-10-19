import SwiftUI

/// The warm background behind every screen, as on Android: a soft gradient from the top down.
struct AppBackground: View {
    var body: some View {
        LinearGradient(
            colors: [Color(.surfaceContainer), Color(.surfaceBase)],
            startPoint: .top,
            endPoint: .bottom
        )
        .ignoresSafeArea()
    }
}

#Preview {
    AppBackground()
}
