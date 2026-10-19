import Shared
import SwiftUI

/// One category in the row above the sound grid.
struct CategoryChip: View {
    let category: SoundCategory
    let isSelected: Bool
    let onSelect: () -> Void

    var body: some View {
        Button(action: onSelect) {
            Label {
                Text(category.title)
            } icon: {
                Image(systemName: category.systemImage)
            }
            .font(.subheadline)
            .bold()
            .foregroundStyle(isSelected ? Color(.onPrimaryContainer) : Color.primary)
            .padding(.horizontal, 14)
            .frame(minHeight: 40)
            .background(isSelected ? Color(.primaryContainer) : Color.clear, in: .capsule)
            .overlay {
                Capsule().strokeBorder(Color(.outlineVariant), lineWidth: isSelected ? 0 : 1)
            }
            .frame(minHeight: 44)
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(isSelected ? .isSelected : [])
        .accessibilityIdentifier(TestTags.shared.CATEGORY_TAB)
    }
}

#Preview {
    HStack {
        CategoryChip(category: .rain, isSelected: true) {}
        CategoryChip(category: .water, isSelected: false) {}
    }
}
