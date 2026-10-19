import Shared
import SwiftUI

/// The Saved tab: the saved mixes as cards; swipe or long-press one to delete it.
struct SavedView: View {
    let model: SavedModel
    let onOpenMixer: () -> Void
    @Environment(\.bannerCenter) private var banners
    @Environment(\.horizontalSizeClass) private var sizeClass

    var body: some View {
        content
            .background { AppBackground() }
            .navigationTitle(Text(.savedScreenTitle))
            .animation(.default, value: model.state.mixes.map(\.id))
            .task { await model.followState(banners: banners) }
    }

    @ViewBuilder private var content: some View {
        if model.state.isLoading {
            ProgressView()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if model.state.mixes.isEmpty {
            ContentUnavailableView {
                Label {
                    Text(.savedEmptyTitle)
                } icon: {
                    Image(systemName: "bookmark")
                }
            } description: {
                Text(.savedEmptyDescription)
            } actions: {
                Button(action: onOpenMixer) {
                    Label {
                        Text(.actionCreateNewMix)
                    } icon: {
                        Image(systemName: "plus")
                    }
                }
                .buttonStyle(.glassProminent)
            }
        } else if sizeClass == .regular {
            HStack(spacing: 0) {
                list(selects: true)
                    .frame(width: 400)
                SavedDetailPane(
                    detail: model.state.selected,
                    isPlaying: model.state.selected.map { model.isPlaying($0.id) } ?? false,
                    onTogglePlay: { model.state.selected.map { model.togglePlay($0.id) } },
                    onDelete: { model.state.selected.map { model.delete($0.id) } }
                )
                .frame(maxWidth: .infinity)
            }
        } else {
            list(selects: false)
        }
    }

    /// `selects`: on a wide window a tap shows the mix beside the list; on a phone it plays it.
    private func list(selects: Bool) -> some View {
        ScrollView {
            LazyVGrid(columns: [GridItem(.adaptive(minimum: 320), spacing: Spacing.grid)], spacing: Spacing.grid) {
                ForEach(model.state.mixes, id: \.id) { mix in
                    SavedMixCard(
                        mix: mix,
                        isPlaying: model.isPlaying(mix.id),
                        isSelected: selects && model.selectedId == mix.id,
                        onTap: { selects ? (model.selectedId = mix.id) : model.togglePlay(mix.id) },
                        onTogglePlay: { model.togglePlay(mix.id) }
                    )
                    .swipeActions {
                        deleteButton(mix.id)
                    }
                    .contextMenu {
                        deleteButton(mix.id)
                    }
                }
            }
            .padding(.horizontal)
        }
        .swipeActionsContainer()
    }

    private func deleteButton(_ mixId: Int64) -> some View {
        Button(role: .destructive) {
            model.delete(mixId)
        } label: {
            Label {
                Text(.actionDelete)
            } icon: {
                Image(systemName: "trash")
            }
        }
    }
}
