import SwiftUI

/// Creates the Freesound model once, after the screen first appears.
struct FreesoundScreen: View {
    let isTab: Bool
    @State private var model: FreesoundModel?

    var body: some View {
        if let model {
            FreesoundView(model: model, isTab: isTab)
        } else {
            Color.clear
                .task { model = FreesoundModel() }
        }
    }
}
