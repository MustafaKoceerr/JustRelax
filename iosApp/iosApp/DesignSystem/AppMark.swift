import SwiftUI

/// The app's icon (a dusk sun sliced into layers by soft waves) in a circle, for the splash and settings.
struct AppMark: View {
    var diameter: CGFloat = 56

    var body: some View {
        Image(.appMark)
            .resizable()
            .scaledToFill()
            .frame(width: diameter, height: diameter)
            .clipShape(.circle)
            .accessibilityHidden(true)
    }
}

#Preview {
    AppMark(diameter: 160)
}
