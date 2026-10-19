import SwiftUI

/// The app's icon (the leaf with a sound wave) in a circle, for the splash and settings.
struct AppMark: View {
    var diameter: CGFloat = 56

    var body: some View {
        // The artwork keeps the icon's padding; zoomed in, its cream tile fills the circle.
        Image(.appMark)
            .resizable()
            .scaledToFill()
            .scaleEffect(1.5)
            .frame(width: diameter, height: diameter)
            .clipShape(.circle)
            .accessibilityHidden(true)
    }
}

#Preview {
    AppMark(diameter: 160)
}
