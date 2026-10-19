import SwiftUI

/// A softly breathing orb with a sparkle; while the AI thinks, rings spread out from it.
struct AiOrb: View {
    let isThinking: Bool
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var breathes = false

    var body: some View {
        ZStack {
            if isThinking, !reduceMotion {
                ForEach(0..<3, id: \.self) { ring in
                    Circle()
                        .stroke(.tint.opacity(0.35), lineWidth: 2)
                        .phaseAnimator([false, true]) { circle, spreads in
                            circle
                                .scaleEffect(spreads ? 1.6 : 1)
                                .opacity(spreads ? 0 : 1)
                        } animation: { _ in
                            .easeOut(duration: 1.8).delay(Double(ring) * 0.6)
                        }
                }
            }
            Circle()
                .fill(RadialGradient(
                    colors: [Color(.primaryContainer), Color.accentColor.opacity(0.35)],
                    center: UnitPoint(x: 0.4, y: 0.35),
                    startRadius: 0,
                    endRadius: 80
                ))
                .scaleEffect(breathes ? 1.04 : 0.96)
            Image(systemName: "sparkles")
                .font(.system(size: 48))
                .foregroundStyle(.tint)
                .symbolEffect(.breathe, isActive: isThinking && !reduceMotion)
        }
        .frame(width: 128, height: 128)
        .accessibilityHidden(true)
        .onAppear {
            guard !reduceMotion else { return }
            withAnimation(.easeInOut(duration: 2.4).repeatForever()) { breathes = true }
        }
    }
}

#Preview {
    VStack(spacing: 40) {
        AiOrb(isThinking: false)
        AiOrb(isThinking: true)
    }
}
