import SwiftUI

/// A placeholder while the screens are rebuilt in SwiftUI, one by one, on the shared ViewModels.
struct ContentView: View {
    var body: some View {
        ContentUnavailableView(
            "JustRelax",
            systemImage: "waveform",
            description: Text(verbatim: "The iOS screens are being rebuilt in SwiftUI.")
        )
    }
}
