import SwiftUI

/// A segmented choice of 2 to 5 sounds in which counts below the locked sounds are disabled, which
/// the system's segmented picker can't do.
struct SoundCountPicker: View {
    let selected: Int
    let minimum: Int
    let onSelect: (Int) -> Void

    var body: some View {
        HStack(spacing: 0) {
            ForEach(MixerText.counts, id: \.self) { count in
                let isSelected = count == selected
                Button {
                    onSelect(count)
                } label: {
                    Text(count, format: .number)
                        .bold(isSelected)
                        .frame(maxWidth: .infinity, minHeight: 38)
                        .background(isSelected ? Color(.surfaceBase) : Color.clear, in: .capsule)
                        .shadow(color: .black.opacity(isSelected ? 0.1 : 0), radius: 3, y: 1)
                        .contentShape(.capsule)
                }
                .buttonStyle(.plain)
                .foregroundStyle(isSelected ? Color(.onPrimaryContainer) : Color.primary)
                .disabled(!MixerText.canPick(count, minimum: minimum))
                .accessibilityAddTraits(isSelected ? .isSelected : [])
            }
        }
        .padding(3)
        .background(Color(.surfaceContainerHighest), in: .capsule)
        .animation(.snappy, value: selected)
    }
}

#Preview {
    SoundCountPicker(selected: 4, minimum: 3) { _ in }
        .padding()
}
