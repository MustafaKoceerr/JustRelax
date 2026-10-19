import Shared
import SwiftUI

/// A sound's icon in a circle: filled with the brand colour while the sound plays.
struct SoundIconBadge: View {
    let icon: SoundIcon
    var isActive = false
    var diameter: CGFloat = 40

    var body: some View {
        Image(systemName: icon.systemImage)
            .font(.system(size: diameter * 0.5))
            .foregroundStyle(isActive ? Color(.onPrimary) : Color.secondary)
            .frame(width: diameter, height: diameter)
            .background(isActive ? Color.accentColor : Color(.surfaceContainerHighest), in: .circle)
            .accessibilityHidden(true)
    }
}

#Preview {
    HStack {
        SoundIconBadge(icon: .rain, isActive: true)
        SoundIconBadge(icon: .bird)
        SoundIconBadge(icon: .fireplace, isActive: true, diameter: 44)
    }
    .padding()
}
