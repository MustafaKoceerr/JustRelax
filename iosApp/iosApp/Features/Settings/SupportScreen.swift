import SwiftUI

/// Creates the model once, after the screen first appears.
struct SupportScreen: View {
    @State private var model: SupportModel?

    var body: some View {
        if let model {
            SupportView(model: model)
        } else {
            Color.clear
                .task { model = SupportModel() }
        }
    }
}
