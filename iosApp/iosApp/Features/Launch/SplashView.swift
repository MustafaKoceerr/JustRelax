import SwiftUI

/// The app mark in a breathing glow while the app gets ready, as on Android.
struct SplashView: View {
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var breathes = false
    @State private var titleShows = false

    var body: some View {
        VStack(spacing: 40) {
            ZStack {
                Circle()
                    .fill(RadialGradient(
                        colors: [Color.accentColor.opacity(0.25), Color.accentColor.opacity(0)],
                        center: .center,
                        startRadius: 0,
                        endRadius: 140
                    ))
                    .frame(width: 280, height: 280)
                    .scaleEffect(breathes ? 1.1 : 0.85)
                AppMark(diameter: 160)
            }
            Text(verbatim: "Just Relax")
                .font(.title)
                .bold()
                .opacity(titleShows ? 1 : 0)
                .offset(y: titleShows || reduceMotion ? 0 : 12)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(.surfaceBase))
        .accessibilityElement(children: .combine)
        .onAppear {
            withAnimation(.easeOut(duration: 0.4)) { titleShows = true }
            guard !reduceMotion else { return }
            withAnimation(.easeInOut(duration: 1.6).repeatForever()) { breathes = true }
        }
    }
}

#Preview {
    SplashView()
}
