import SwiftUI

/// Creates My sounds' model once, after the screen first appears.
struct MySoundsScreen: View {
    @State private var model: MySoundsModel?

    var body: some View {
        if let model {
            MySoundsView(model: model)
        } else {
            Color.clear
                .task { model = MySoundsModel() }
        }
    }
}
