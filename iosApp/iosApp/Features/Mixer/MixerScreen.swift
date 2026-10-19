import SwiftUI

/// Creates the mixer's model once, after the tab first appears.
struct MixerScreen: View {
    @State private var model: MixerModel?

    var body: some View {
        if let model {
            MixerView(model: model)
        } else {
            Color.clear
                .task { model = MixerModel() }
        }
    }
}
