import Shared
import SwiftUI

/// The mini player in the tab bar's accessory: the sounds playing, the sleep timer, play/pause and
/// stop. Tapping the sounds opens the scene sheet.
struct MiniPlayerView: View {
    let scene: SceneModel
    let onOpenScene: () -> Void
    let onOpenTimer: () -> Void
    @Environment(\.tabViewBottomAccessoryPlacement) private var placement

    var body: some View {
        HStack(spacing: 8) {
            Button(action: onOpenScene) {
                HStack(spacing: 8) {
                    iconStack
                    if placement != .inline {
                        Text(scene.miniPlayer.names.joined(separator: ", "))
                            .font(.subheadline)
                            .bold()
                            .lineLimit(1)
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }
                }
                .contentShape(.rect)
            }
            .buttonStyle(.plain)
            .accessibilityLabel(Text(scene.miniPlayer.names.joined(separator: ", ")))
            .accessibilityHint(Text(.scenePanelTitle))

            if placement != .inline {
                timerChip
            }
            Button {
                scene.togglePlayPause()
            } label: {
                Label {
                    scene.miniPlayer.isPlaying ? Text(.actionPause) : Text(.actionPlay)
                } icon: {
                    Image(systemName: scene.miniPlayer.isPlaying ? "pause.fill" : "play.fill")
                }
                .labelStyle(.iconOnly)
                .frame(width: 44, height: 44)
                .contentShape(.rect)
            }
            .buttonStyle(.plain)
            if placement != .inline {
                Button {
                    scene.stopAll()
                } label: {
                    Label {
                        Text(.actionStopAll)
                    } icon: {
                        Image(systemName: "xmark")
                    }
                    .labelStyle(.iconOnly)
                    .foregroundStyle(.secondary)
                    .frame(width: 44, height: 44)
                    .contentShape(.rect)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.leading, 10)
        .padding(.trailing, 4)
    }

    private var iconStack: some View {
        let icons = scene.miniPlayer.icons
        let extra = icons.count - 3
        return HStack(spacing: -9) {
            ForEach(Array(icons.prefix(3).enumerated()), id: \.offset) { _, icon in
                SoundIconBadge(icon: icon, isActive: true, diameter: 28)
                    .overlay { Circle().stroke(.background, lineWidth: 2) }
            }
            if extra > 0 {
                Text(verbatim: "+\(extra)")
                    .font(.caption)
                    .bold()
                    .frame(width: 28, height: 28)
                    .background(Color(.surfaceContainerHighest), in: .circle)
                    .overlay { Circle().stroke(.background, lineWidth: 2) }
            }
        }
        .accessibilityHidden(true)
    }

    private var timerChip: some View {
        Button(action: onOpenTimer) {
            Label {
                if let remaining = scene.timerRemaining {
                    Text(verbatim: TimerText.countdown(seconds: remaining))
                        .monospacedDigit()
                } else {
                    Text(.sceneTimerChip)
                }
            } icon: {
                Image(systemName: "moon.fill")
            }
            .font(.footnote)
            .bold()
            .padding(.horizontal, 10)
            .frame(minHeight: 36)
            .background(.tint.opacity(0.12), in: .capsule)
            .frame(minHeight: 44)
        }
        .buttonStyle(.plain)
        .accessibilityLabel(Text(.sleepTimer))
        .accessibilityIdentifier(TestTags.shared.MINI_PLAYER_TIMER)
    }
}
