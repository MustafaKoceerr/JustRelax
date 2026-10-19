import SwiftUI

/// Creates the model once, after the screen first appears.
struct SettingsScreen: View {
    @State private var model: SettingsModel?

    var body: some View {
        if let model {
            SettingsView(model: model)
        } else {
            Color.clear
                .task { model = SettingsModel() }
        }
    }
}
