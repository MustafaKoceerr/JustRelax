import Shared
import SwiftUI

/// The AI tab: describe a place or a feeling and get a scene built from the downloaded sounds.
struct AiView: View {
    @Bindable var model: AiModel
    @Environment(\.bannerCenter) private var banners
    @Environment(\.scene) private var scene
    @Environment(\.horizontalSizeClass) private var sizeClass

    var body: some View {
        ScrollView {
            if model.state.hasResult {
                result
            } else {
                start
            }
        }
        .scrollDismissesKeyboard(.interactively)
        .animation(.easeInOut, value: model.state.hasResult)
        .safeAreaBar(edge: .bottom) {
            AiComposer(prompt: $model.prompt, canSend: model.canSend, isLoading: model.state.isLoading, onSend: model.send)
        }
        .background { AppBackground() }
        .navigationTitle(Text(.aiScreenTitle))
        .toolbar {
            if model.state.hasResult {
                ToolbarItem(placement: .primaryAction) {
                    Button(action: model.startOver) {
                        Label {
                            Text(.aiActionNew)
                        } icon: {
                            Image(systemName: "plus")
                        }
                    }
                }
            }
        }
        .alert(Text(.aiDataNoticeTitle), isPresented: $model.isDataNoticeShown) {
            Button(action: model.acceptDataNotice) {
                Text(.aiDataNoticeAccept)
            }
            Button(action: model.openPrivacyPolicy) {
                Text(.aiDataNoticePrivacy)
            }
            Button(role: .cancel) {} label: {
                Text(.aiDataNoticeDismiss)
            }
        } message: {
            Text(.aiDataNoticeBody)
        }
        .task { await model.followState(banners: banners) }
        .task { await model.followVolumes() }
    }

    private var start: some View {
        VStack(spacing: 14) {
            AiOrb(isThinking: model.state.isLoading)
            if model.state.isLoading {
                Text(.aiThinking)
                    .font(.title2)
                    .bold()
            } else {
                Text(.aiGreetingTitle)
                    .font(.title2)
                    .bold()
                Text(.aiGreetingBody)
                    .foregroundStyle(.secondary)
            }
            LazyVGrid(
                columns: Array(repeating: GridItem(.flexible(), spacing: 10), count: sizeClass == .regular ? 4 : 2),
                spacing: 10
            ) {
                ForEach(AiSuggestion.allCases, id: \.self) { suggestion in
                    Button {
                        model.send(suggestion)
                    } label: {
                        VStack(spacing: 6) {
                            Image(systemName: suggestion.systemImage)
                                .foregroundStyle(.tint)
                            Text(suggestion.title)
                                .bold()
                        }
                        .frame(maxWidth: .infinity, minHeight: 76)
                        .background(Color(.surfaceContainerHigh), in: .rect(cornerRadius: 20))
                    }
                    .buttonStyle(.plain)
                }
            }
            .opacity(model.state.isLoading ? 0.4 : 1)
            .disabled(model.state.isLoading)
            .padding(.top, 6)
        }
        .multilineTextAlignment(.center)
        .padding()
        .frame(maxWidth: 640)
        .frame(maxWidth: .infinity)
    }

    private var result: some View {
        VStack(spacing: 12) {
            header
            LazyVGrid(columns: [GridItem(.adaptive(minimum: 320), spacing: Spacing.grid)], spacing: Spacing.grid) {
                ForEach(model.state.mixSounds, id: \.id) { sound in
                    SoundLayerRow(
                        name: sound.name,
                        icon: sound.icon,
                        frequency: model.state.sceneCards.frequency(soundId: sound.id),
                        isPlaying: model.state.sceneCards.isPlaying(soundId: sound.id),
                        level: model.volumes.level(of: sound.id),
                        onToggle: { model.toggle(sound.id) },
                        onVolumeChange: { model.changeVolume(of: sound.id, to: $0) },
                        onFrequencyChange: { model.changeFrequency(of: sound.id, to: $0) }
                    )
                }
            }
        }
        .opacity(model.state.isLoading ? 0.5 : 1)
        .padding(.horizontal)
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 8) {
            Label {
                Text(.aiResultLabel)
            } icon: {
                Image(systemName: "sparkles")
            }
            .font(.subheadline)
            .bold()
            .foregroundStyle(.tint)
            Text(model.state.mixName)
                .font(.title2)
                .bold()
            Text(model.state.mixDescription)
                .foregroundStyle(.secondary)
            Text(.aiPromptQuote(model.state.lastPrompt))
                .italic()
                .foregroundStyle(.secondary)
            ViewThatFits(in: .horizontal) {
                HStack { headerButtons }
                VStack(alignment: .leading) { headerButtons }
            }
            .padding(.top, 4)
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.surfaceContainer), in: .rect(cornerRadius: Radius.large))
    }

    @ViewBuilder private var headerButtons: some View {
        Button(action: model.tryAnotherVariation) {
            Label {
                Text(.aiActionTryAnotherVariation)
            } icon: {
                Image(systemName: "arrow.clockwise")
            }
        }
        .buttonStyle(.glassProminent)
        .tint(Color(.primaryContainer))
        .foregroundStyle(Color(.onPrimaryContainer))
        .disabled(model.state.isLoading)
        Button {
            scene?.askToSave(suggestedName: model.state.mixName)
        } label: {
            Label {
                Text(.actionSave)
            } icon: {
                Image(systemName: "bookmark")
            }
        }
        .buttonStyle(.glass)
    }
}
