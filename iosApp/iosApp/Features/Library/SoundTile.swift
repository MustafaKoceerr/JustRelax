import Shared
import SwiftUI

/// A square sound card: tap to play or stop, slide across a playing card to set its volume, which
/// fills the card from the leading edge.
struct SoundTile: View {
    let sound: LocalizedSound
    let isPlaying: Bool
    let corner: TileCorner
    let level: SoundVolumes.Level
    let onToggle: () -> Void
    let onVolumeChange: (Float) -> Void
    let onFrequencyChange: (EventFrequency) -> Void

    @State private var width: CGFloat = 0
    @State private var dragStart: Float?
    @State private var dragIsVertical = false

    private var shownVolume: Float { isPlaying ? level.value : 0 }

    var body: some View {
        VStack(alignment: .leading) {
            SoundIconBadge(icon: sound.icon, isActive: isPlaying)
                .opacity(sound.isDownloaded ? 1 : 0.4)
            Spacer(minLength: 4)
            Text(sound.name)
                .font(.subheadline)
                .bold()
                .lineLimit(2)
                .opacity(sound.isDownloaded ? 1 : 0.6)
        }
        .padding(Spacing.tile)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
        .background(alignment: .leading) { volumeFill }
        .background(Color(.surfaceContainerHigh))
        .clipShape(.rect(cornerRadius: Radius.large))
        .aspectRatio(1, contentMode: .fit)
        .onGeometryChange(for: CGFloat.self) { $0.size.width } action: { width = $0 }
        .contentShape(.rect(cornerRadius: Radius.large))
        // A tap and a horizontal slide need telling apart, which a Button can't do.
        .onTapGesture(perform: onToggle)
        .simultaneousGesture(volumeDrag, isEnabled: isPlaying)
        .overlay(alignment: .topTrailing) { cornerView }
        .animation(.easeInOut(duration: 0.3), value: isPlaying)
        .sensoryFeedback(.selection, trigger: isPlaying)
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(.isButton)
        .accessibilityLabel(Text(sound.name))
        .accessibilityValue(accessibilityValue)
        .accessibilityAction(.default, onToggle)
        .accessibilityAdjustableAction { direction in
            guard isPlaying else { return }
            let step: Float = direction == .increment ? 0.1 : -0.1
            onVolumeChange(min(max(level.value + step, 0), 1))
        }
        .accessibilityIdentifier(TestTags.shared.SOUND_CARD)
    }

    private var volumeFill: some View {
        ZStack(alignment: .trailing) {
            Rectangle()
                .fill(Color(.primaryContainer))
            if isPlaying {
                Capsule()
                    .fill(.tint)
                    .frame(width: 4, height: 28)
                    .offset(x: 2)
            }
        }
        .frame(width: width * CGFloat(shownVolume))
    }

    @ViewBuilder private var cornerView: some View {
        switch corner {
        case .downloading:
            ProgressView()
                .padding(Spacing.tile)
        case .download:
            Image(systemName: "icloud.and.arrow.down")
                .foregroundStyle(.tint)
                .padding(Spacing.tile)
                .accessibilityLabel(Text(.soundActionDownload))
        case .frequency(let frequency):
            FrequencyMenu(frequency: frequency, onChange: onFrequencyChange)
                .padding(2)
        case .event:
            Image(systemName: "shuffle")
                .font(.footnote)
                .foregroundStyle(.secondary)
                .padding(Spacing.tile)
                .accessibilityLabel(Text(.soundKindEvent))
        case .none:
            EmptyView()
        }
    }

    private var volumeDrag: some Gesture {
        DragGesture(minimumDistance: 10)
            .onChanged { drag in
                if dragStart == nil, !dragIsVertical {
                    // Decide once per drag: a mostly vertical one belongs to the scroll view.
                    if abs(drag.translation.height) > abs(drag.translation.width) {
                        dragIsVertical = true
                        return
                    }
                    dragStart = level.value
                }
                guard let dragStart else { return }
                onVolumeChange(DragVolumeKt.volumeAfterDrag(
                    start: dragStart,
                    dragX: Float(drag.translation.width),
                    width: Float(width)
                ))
            }
            .onEnded { _ in
                dragStart = nil
                dragIsVertical = false
            }
    }

    private var accessibilityValue: String {
        guard isPlaying else { return String(localized: .soundStateStopped) }
        let volume = Double(level.value).formatted(.percent.precision(.fractionLength(0)))
        return "\(String(localized: .soundStatePlaying)), \(volume)"
    }
}
