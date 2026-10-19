import Shared
import SwiftUI

/// Search for free sounds on Freesound and add them to the library: the Discover tab, and a pushed
/// screen from My sounds.
struct FreesoundView: View {
    @Bindable var model: FreesoundModel
    /// The tab has its own title; pushed from My sounds it is just "Freesound".
    let isTab: Bool
    @Environment(\.bannerCenter) private var banners
    @Environment(\.navigator) private var navigator

    var body: some View {
        content
            .background { AppBackground() }
            .navigationTitle(isTab ? Text(.freesoundDiscoverTitle) : Text(.freesoundTitle))
            .searchable(
                text: $model.query,
                placement: .navigationBarDrawer(displayMode: .always),
                prompt: Text(.freesoundSearchHint)
            )
            .onSubmit(of: .search) { model.search() }
            .task { await model.followState(banners: banners, navigator: navigator) }
            .onDisappear { model.stopPreview() }
    }

    @ViewBuilder private var content: some View {
        if model.state.isSearching {
            ProgressView()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if !model.state.hasSearched {
            welcome
        } else if model.state.results.isEmpty {
            ContentUnavailableView.search
        } else {
            results
        }
    }

    private var welcome: some View {
        ScrollView {
            VStack(spacing: 16) {
                Image(systemName: "globe")
                    .font(.system(size: 44))
                    .foregroundStyle(Color(.onPrimaryContainer))
                    .frame(width: 96, height: 96)
                    .background(Color(.primaryContainer), in: .circle)
                    .accessibilityHidden(true)
                Text(.freesoundIntro)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                Text(.freesoundTry)
                    .font(.headline)
                    .padding(.top, 4)
                FlowLayout {
                    ForEach(FreesoundSuggestion.allCases, id: \.self) { suggestion in
                        Button {
                            model.search(suggestion)
                        } label: {
                            Label {
                                Text(suggestion.title)
                            } icon: {
                                Image(systemName: suggestion.systemImage)
                                    .foregroundStyle(.tint)
                            }
                            .font(.subheadline)
                            .bold()
                            .padding(.horizontal, 14)
                            .frame(minHeight: 40)
                            .overlay { Capsule().strokeBorder(Color(.outlineVariant)) }
                            .frame(minHeight: 44)
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
            .padding()
            .frame(maxWidth: 560)
            .frame(maxWidth: .infinity)
        }
    }

    private var results: some View {
        ScrollView {
            LazyVGrid(columns: [GridItem(.adaptive(minimum: 340), spacing: Spacing.grid)], spacing: Spacing.grid) {
                ForEach(model.state.results, id: \.id) { sound in
                    FreesoundResultRow(
                        sound: sound,
                        isPreviewing: model.isPreviewing(sound),
                        isDownloading: model.isDownloading(sound),
                        isAddDisabled: model.state.downloadingId != nil,
                        onPreview: { model.togglePreview(sound) },
                        onAdd: { model.add(sound) }
                    )
                    .onAppear {
                        if sound.id == model.state.results.last?.id, model.state.hasMore {
                            model.loadMore()
                        }
                    }
                }
            }
            .padding(.horizontal)
            if model.state.isLoadingMore {
                ProgressView()
                    .padding()
            }
        }
    }
}
