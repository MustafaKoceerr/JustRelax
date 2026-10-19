import SwiftUI

/// Creates the model once, after the screen first appears.
struct CreditsScreen: View {
    @State private var model: CreditsModel?

    var body: some View {
        if let model {
            CreditsView(model: model)
        } else {
            Color.clear
                .task { model = CreditsModel() }
        }
    }
}
