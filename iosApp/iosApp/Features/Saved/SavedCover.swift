import Shared
import SwiftUI

/// A saved mix's cover: up to four of its sound icons on a colour picked from its name.
struct SavedCover: View {
    let title: String
    let icons: [SoundIcon]
    var size: CGFloat = 56

    private static let palette: [(fill: ColorResource, icon: ColorResource)] = [
        (.primaryContainer, .onPrimaryContainer),
        (.tertiaryContainer, .onTertiaryContainer),
        (.secondaryContainer, .onSecondaryContainer),
    ]

    static var paletteCount: Int { palette.count }

    /// Stable across launches, unlike `hashValue`.
    static func paletteIndex(for title: String) -> Int {
        title.unicodeScalars.reduce(0) { $0 &+ Int($1.value) } % palette.count
    }

    var body: some View {
        let colours = Self.palette[Self.paletteIndex(for: title)]
        Grid(horizontalSpacing: 2, verticalSpacing: 2) {
            GridRow {
                icon(at: 0)
                icon(at: 1)
            }
            GridRow {
                icon(at: 2)
                icon(at: 3)
            }
        }
        .font(.system(size: size * 0.28))
        .foregroundStyle(Color(colours.icon))
        .frame(width: size, height: size)
        .background(Color(colours.fill), in: .rect(cornerRadius: size * 0.28))
        .accessibilityHidden(true)
    }

    @ViewBuilder private func icon(at index: Int) -> some View {
        if index < icons.count {
            Image(systemName: icons[index].systemImage)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else {
            Color.clear
        }
    }
}

#Preview {
    HStack {
        SavedCover(title: "Evening rain", icons: [.rain, .thunder, .wind])
        SavedCover(title: "Forest morning", icons: [.forest, .bird, .stream, .wind], size: 96)
    }
}
