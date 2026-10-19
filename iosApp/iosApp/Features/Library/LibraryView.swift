import Shared
import SwiftUI

/// The Sounds tab: a greeting, the categories and a grid of sound tiles.
struct LibraryView: View {
    let model: LibraryModel
    @Environment(\.bannerCenter) private var banners
    @Environment(\.navigator) private var navigator
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        content
            .navigationTitle(Text(LibraryText.greeting(hour: Calendar.current.component(.hour, from: .now))))
            .navigationSubtitle(model.state.isEmpty ? Text(verbatim: "") : Text(.libraryHint))
            .background { AppBackground() }
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        navigator?.push(.mySounds)
                    } label: {
                        Label {
                            Text(.actionMySounds)
                        } icon: {
                            Image(systemName: "music.note.list")
                        }
                    }
                }
            }
            .task { await model.followState(banners: banners) }
            .task { await model.followVolumes() }
    }

    @ViewBuilder private var content: some View {
        if model.state.isLoading {
            ProgressView()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if model.state.isEmpty {
            ContentUnavailableView {
                Label {
                    Text(.libraryEmptyTitle)
                } icon: {
                    Image(systemName: "music.note.list")
                }
            } description: {
                Text(.libraryEmptyMessage)
            } actions: {
                Button {
                    model.retry()
                } label: {
                    Text(.actionRetry)
                }
                .buttonStyle(.glassProminent)
            }
        } else {
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    categoryRow
                    soundGrid
                        .padding(.horizontal)
                        .id(model.state.selectedCategory)
                        .transition(reduceMotion ? .opacity : .opacity.combined(with: .offset(y: 8)))
                }
                .animation(.easeOut(duration: 0.25), value: model.state.selectedCategory)
            }
        }
    }

    private var categoryRow: some View {
        ScrollView(.horizontal) {
            HStack(spacing: 8) {
                ForEach(model.categories, id: \.self) { category in
                    CategoryChip(category: category, isSelected: category == model.state.selectedCategory) {
                        model.select(category)
                    }
                }
            }
            .scrollTargetLayout()
        }
        .scrollTargetBehavior(.viewAligned)
        .scrollIndicators(.hidden)
        .contentMargins(.horizontal, 16, for: .scrollContent)
    }

    private var soundGrid: some View {
        LazyVGrid(columns: [GridItem(.adaptive(minimum: 112), spacing: Spacing.grid)], spacing: Spacing.grid) {
            ForEach(model.sounds, id: \.id) { sound in
                let cards = model.state.sceneCards
                SoundTile(
                    sound: sound,
                    isPlaying: cards.isPlaying(soundId: sound.id),
                    corner: TileCorner(
                        isDownloading: model.state.downloadingSoundIds.contains(sound.id),
                        isDownloaded: sound.isDownloaded,
                        kind: sound.kind,
                        frequency: cards.frequency(soundId: sound.id)
                    ),
                    level: model.volumes.level(of: sound.id),
                    onToggle: { model.toggle(sound) },
                    onVolumeChange: { model.changeVolume(of: sound.id, to: $0) },
                    onFrequencyChange: { model.changeFrequency(of: sound.id, to: $0) }
                )
            }
        }
        .accessibilityIdentifier(TestTags.shared.SOUND_GRID)
    }
}
