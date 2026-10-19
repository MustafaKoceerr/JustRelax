import SwiftUI

/// A settings row's icon on a small brand-coloured tile, as in the iOS Settings app.
struct SettingsIcon: View {
    let systemImage: String

    var body: some View {
        Image(systemName: systemImage)
            .font(.system(size: 15, weight: .semibold))
            .foregroundStyle(Color(.onPrimary))
            .frame(width: 29, height: 29)
            .background(.tint, in: .rect(cornerRadius: 7))
            .accessibilityHidden(true)
    }
}

#Preview {
    SettingsIcon(systemImage: "paintpalette.fill")
}
