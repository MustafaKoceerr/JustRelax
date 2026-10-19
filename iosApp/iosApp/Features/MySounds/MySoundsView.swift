import Shared
import SwiftUI
import UniformTypeIdentifiers

/// My sounds: the user's own files and Freesound finds. Import adds one; swipe to delete.
struct MySoundsView: View {
    let model: MySoundsModel
    @Environment(\.bannerCenter) private var banners
    @Environment(\.navigator) private var navigator
    @State private var picksFile = false

    var body: some View {
        @Bindable var model = model
        content
            .background { AppBackground() }
            .navigationTitle(Text(.mysoundsTitle))
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    if model.state.isImporting {
                        ProgressView()
                    } else {
                        Button {
                            picksFile = true
                        } label: {
                            Label {
                                Text(.mysoundsImport)
                            } icon: {
                                Image(systemName: "plus")
                            }
                        }
                    }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        navigator?.push(.freesound)
                    } label: {
                        Label {
                            Text(.mysoundsSearchOnline)
                        } icon: {
                            Image(systemName: "globe")
                        }
                    }
                }
            }
            .fileImporter(isPresented: $picksFile, allowedContentTypes: [.audio]) { result in
                model.importFile(result)
            }
            .confirmationDialog(Text(.mysoundsDeleteTitle), item: $model.soundToDelete, titleVisibility: .visible) { _ in
                Button(role: .destructive, action: model.confirmDelete) {
                    Text(.actionDelete)
                }
            } message: { sound in
                Text(.mysoundsDeleteText(sound.name))
            }
            .task { await model.followState(banners: banners, navigator: navigator) }
    }

    @ViewBuilder private var content: some View {
        if model.state.isLoading {
            Color.clear
        } else if model.state.sounds.isEmpty {
            ContentUnavailableView {
                Label {
                    Text(.mysoundsEmptyTitle)
                } icon: {
                    Image(systemName: "music.note.list")
                }
            } description: {
                Text(.mysoundsEmptyDescription)
            } actions: {
                Button {
                    picksFile = true
                } label: {
                    Label {
                        Text(.mysoundsImport)
                    } icon: {
                        Image(systemName: "plus")
                    }
                }
                .buttonStyle(.glassProminent)
            }
        } else {
            List {
                ForEach(model.state.sounds, id: \.id) { sound in
                    HStack(spacing: 14) {
                        SoundIconBadge(icon: sound.icon, diameter: 36)
                        VStack(alignment: .leading, spacing: 2) {
                            Text(sound.name)
                            Text(MySoundsText.details(of: sound))
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                        }
                    }
                    .listRowBackground(Color(.surfaceContainerHigh))
                    .swipeActions {
                        Button(role: .destructive) {
                            model.requestDelete(sound.id)
                        } label: {
                            Label {
                                Text(.actionDelete)
                            } icon: {
                                Image(systemName: "trash")
                            }
                        }
                    }
                }
            }
            .scrollContentBackground(.hidden)
        }
    }
}
