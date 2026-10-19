import Shared
import SwiftUI

/// The Mixer tab: pick how many sounds, shuffle, and lock the ones worth keeping.
struct MixerView: View {
    let model: MixerModel
    @Environment(\.bannerCenter) private var banners
    @Environment(\.scene) private var scene
    @Environment(\.horizontalSizeClass) private var sizeClass
    @State private var shuffles = 0

    private var hasMix: Bool { !model.state.mixedSounds.isEmpty }

    var body: some View {
        Group {
            if sizeClass == .regular {
                HStack(alignment: .top, spacing: 24) {
                    controls
                        .frame(width: 360)
                        .padding(.top)
                    ScrollView { mixSection }
                }
                .padding(.horizontal)
            } else {
                ScrollView {
                    VStack(alignment: .leading, spacing: 24) {
                        controls
                        mixSection
                    }
                    .padding(.horizontal)
                }
            }
        }
        .background { AppBackground() }
        .navigationTitle(Text(.mixerScreenTitle))
        .toolbar {
            if hasMix {
                ToolbarItem(placement: .primaryAction) {
                    Button {
                        scene?.askToSave()
                    } label: {
                        Label {
                            Text(.actionSave)
                        } icon: {
                            Image(systemName: "bookmark")
                        }
                    }
                }
            }
        }
        .task { await model.followState(banners: banners) }
        .task { await model.followVolumes() }
    }

    private var controls: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text(.mixerCountTitle)
                .font(.headline)
            SoundCountPicker(
                selected: Int(model.state.selectedSoundCount),
                minimum: Int(model.state.minSelectableCount),
                onSelect: model.selectCount
            )
            Button {
                shuffles += 1
                model.shuffle()
            } label: {
                Label {
                    Text(.actionShuffle)
                } icon: {
                    Image(systemName: "dice.fill")
                        .symbolEffect(.rotate, value: shuffles)
                }
                .frame(maxWidth: .infinity)
            }
            .buttonStyle(.glassProminent)
            .controlSize(.large)
            .disabled(model.state.isGenerating)
        }
        .frame(maxWidth: 420, alignment: .leading)
    }

    @ViewBuilder private var mixSection: some View {
        if hasMix {
            VStack(alignment: .leading, spacing: 12) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(.mixerCurrentMix)
                        .font(.title2)
                        .bold()
                    if !model.state.lockedIds.isEmpty {
                        Text(.mixerLockHint)
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                    }
                }
                LazyVGrid(columns: [GridItem(.adaptive(minimum: 320), spacing: Spacing.grid)], spacing: Spacing.grid) {
                    ForEach(model.state.mixedSounds, id: \.id) { sound in
                        layerRow(sound)
                    }
                }
            }
        } else {
            ContentUnavailableView {
                Label {
                    Text(.mixerEmptyTitle)
                } icon: {
                    Image(systemName: "shuffle")
                }
            } description: {
                Text(.mixerEmptyBody)
            }
        }
    }

    private func layerRow(_ sound: LocalizedSound) -> some View {
        let isLocked = model.state.lockedIds.contains(sound.id)
        return SoundLayerRow(
            name: sound.name,
            icon: sound.icon,
            frequency: model.state.sceneCards.frequency(soundId: sound.id),
            isPlaying: model.state.sceneCards.isPlaying(soundId: sound.id),
            level: model.volumes.level(of: sound.id),
            onToggle: { model.toggle(sound.id) },
            onVolumeChange: { model.changeVolume(of: sound.id, to: $0) },
            onFrequencyChange: { model.changeFrequency(of: sound.id, to: $0) }
        ) {
            Button {
                model.toggleLock(sound.id)
            } label: {
                Label {
                    isLocked ? Text(.mixerUnlock) : Text(.mixerLock)
                } icon: {
                    Image(systemName: isLocked ? "lock.fill" : "lock.open")
                }
                .labelStyle(.iconOnly)
                .foregroundStyle(isLocked ? Color.accentColor : Color.secondary)
                .frame(width: 44, height: 44)
                .background(isLocked ? Color(.primaryContainer) : Color.clear, in: .circle)
            }
            .buttonStyle(.plain)
            .accessibilityAddTraits(isLocked ? .isSelected : [])
        }
    }
}
