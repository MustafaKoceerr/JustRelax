import SwiftUI

/// Creates the AI model once, after the tab first appears.
struct AiScreen: View {
    @State private var model: AiModel?

    var body: some View {
        if let model {
            AiView(model: model)
        } else {
            Color.clear
                .task { model = AiModel() }
        }
    }
}
