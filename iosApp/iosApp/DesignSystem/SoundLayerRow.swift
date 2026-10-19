import Shared
import SwiftUI

/// One sound of a mix as a card: its icon, name, how it plays and a volume slider. The scene, the
/// mixer and the AI mix all use it.
struct SoundLayerRow<Trailing: View>: View {
    let name: String
    let icon: SoundIcon
    /// Nil for a looping sound; an event sound shows a menu to change it.
    let frequency: EventFrequency?
    let isPlaying: Bool
    let level: SoundVolumes.Level
    /// When set, the icon plays or stops the sound.
    var onToggle: (() -> Void)?
    let onVolumeChange: (Float) -> Void
    let onFrequencyChange: (EventFrequency) -> Void
    @ViewBuilder var trailing: Trailing

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(spacing: 12) {
                iconView
                VStack(alignment: .leading, spacing: 2) {
                    Text(name)
                        .font(.headline)
                    if let frequency {
                        FrequencyMenu(frequency: frequency, onChange: onFrequencyChange)
                            .frame(minHeight: 28)
                    } else {
                        Text(.soundKindBed)
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                trailing
            }
            Slider(value: Binding(get: { level.value }, set: { onVolumeChange($0) }), in: 0...1) {
                Text(.volumeLevel)
            }
            .disabled(!isPlaying)
            .accessibilityIdentifier(TestTags.shared.VOLUME_SLIDER)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(isPlaying ? Color(.surfaceContainerHigh) : Color(.surfaceContainer), in: .rect(cornerRadius: Radius.large))
    }

    @ViewBuilder private var iconView: some View {
        if let onToggle {
            Button(action: onToggle) {
                SoundIconBadge(icon: icon, isActive: isPlaying, diameter: 44)
            }
            .buttonStyle(.plain)
            .accessibilityLabel(isPlaying ? Text(.actionStop) : Text(.actionPlay))
        } else {
            SoundIconBadge(icon: icon, isActive: isPlaying, diameter: 44)
        }
    }
}

extension SoundLayerRow where Trailing == EmptyView {
    init(
        name: String,
        icon: SoundIcon,
        frequency: EventFrequency?,
        isPlaying: Bool,
        level: SoundVolumes.Level,
        onToggle: (() -> Void)? = nil,
        onVolumeChange: @escaping (Float) -> Void,
        onFrequencyChange: @escaping (EventFrequency) -> Void
    ) {
        self.init(
            name: name, icon: icon, frequency: frequency, isPlaying: isPlaying, level: level,
            onToggle: onToggle, onVolumeChange: onVolumeChange, onFrequencyChange: onFrequencyChange
        ) {
            EmptyView()
        }
    }
}
