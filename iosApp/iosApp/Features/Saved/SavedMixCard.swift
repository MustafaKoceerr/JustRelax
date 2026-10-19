import Shared
import SwiftUI

/// One saved mix: its cover, name, sounds and a play button; a tap plays or pauses it.
struct SavedMixCard: View {
    let mix: SavedMixItem
    let isPlaying: Bool
    var isSelected = false
    let onTap: () -> Void
    let onTogglePlay: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Button(action: onTap) {
                HStack(spacing: 12) {
                    SavedCover(title: mix.title, icons: mix.icons)
                    VStack(alignment: .leading, spacing: 2) {
                        Text(mix.title)
                            .font(.headline)
                        if isPlaying {
                            Label {
                                Text(.soundStatePlaying)
                            } icon: {
                                Image(systemName: "waveform")
                                    .symbolEffect(.variableColor.iterative, isActive: true)
                            }
                            .font(.subheadline)
                            .bold()
                            .foregroundStyle(.tint)
                        } else {
                            Text(SavedText.metadata(soundCount: mix.soundNames.count, date: SavedModel.date(mix.date)))
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                        }
                        Text(mix.soundNames.joined(separator: ", "))
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                            .lineLimit(1)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                }
                .contentShape(.rect)
            }
            .buttonStyle(.plain)

            Button(action: onTogglePlay) {
                Label {
                    isPlaying ? Text(.actionPause) : Text(.actionPlay)
                } icon: {
                    Image(systemName: isPlaying ? "pause.fill" : "play.fill")
                }
                .labelStyle(.iconOnly)
                .frame(width: 48, height: 48)
                .foregroundStyle(isPlaying ? Color(.onPrimary) : Color(.onPrimaryContainer))
                .background(isPlaying ? Color.accentColor : Color(.primaryContainer), in: .circle)
            }
            .buttonStyle(.plain)
        }
        .padding(12)
        .background(isSelected ? Color(.secondaryContainer) : Color(.surfaceContainerHigh), in: .rect(cornerRadius: Radius.large))
        .overlay {
            RoundedRectangle(cornerRadius: Radius.large)
                .strokeBorder(.tint, lineWidth: isPlaying ? 1.5 : 0)
        }
    }
}
