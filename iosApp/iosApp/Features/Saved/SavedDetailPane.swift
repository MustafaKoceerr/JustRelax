import Shared
import SwiftUI

/// A wide window's right side: the selected mix with its sounds, or a hint to pick one.
struct SavedDetailPane: View {
    let detail: SavedMixDetail?
    let isPlaying: Bool
    let onTogglePlay: () -> Void
    let onDelete: () -> Void

    var body: some View {
        if let detail {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    HStack(spacing: 16) {
                        SavedCover(title: detail.title, icons: detail.sounds.map(\.icon), size: 96)
                        VStack(alignment: .leading, spacing: 4) {
                            Text(detail.title)
                                .font(.title2)
                                .bold()
                            Text(SavedText.metadata(soundCount: detail.sounds.count, date: SavedModel.date(detail.date)))
                                .foregroundStyle(.secondary)
                        }
                    }
                    HStack {
                        Button(action: onTogglePlay) {
                            Label {
                                isPlaying ? Text(.actionPause) : Text(.actionPlay)
                            } icon: {
                                Image(systemName: isPlaying ? "pause.fill" : "play.fill")
                            }
                        }
                        .buttonStyle(.glassProminent)
                        Button(role: .destructive, action: onDelete) {
                            Label {
                                Text(.actionDelete)
                            } icon: {
                                Image(systemName: "trash")
                            }
                        }
                        .buttonStyle(.glass)
                    }
                    ForEach(Array(detail.sounds.enumerated()), id: \.offset) { _, sound in
                        HStack(spacing: 12) {
                            SoundIconBadge(icon: sound.icon, isActive: true, diameter: 44)
                            VStack(alignment: .leading, spacing: 4) {
                                Text(sound.name).font(.headline)
                                if let frequency = sound.frequency {
                                    Text(frequency.title).font(.subheadline).foregroundStyle(.secondary)
                                } else {
                                    Text(.soundKindBed).font(.subheadline).foregroundStyle(.secondary)
                                }
                                ProgressView(value: Double(sound.volume))
                                    .accessibilityLabel(Text(.volumeLevel))
                            }
                        }
                        .padding(14)
                        .background(Color(.surfaceContainer), in: .rect(cornerRadius: Radius.large))
                    }
                }
                .padding()
            }
        } else {
            ContentUnavailableView {
                Label {
                    Text(.savedDetailPlaceholder)
                } icon: {
                    Image(systemName: "bookmark")
                }
            }
        }
    }
}
