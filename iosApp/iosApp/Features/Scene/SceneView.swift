import Shared
import SwiftUI

/// The scene sheet: the sleep timer and one card per sound playing, with save, play and stop.
struct SceneView: View {
    let scene: SceneModel
    @Environment(\.dismiss) private var dismiss
    @State private var timer: TimerModel?
    @State private var saveName = ""

    var body: some View {
        @Bindable var scene = scene
        NavigationStack {
            ScrollView {
                LazyVStack(spacing: 12) {
                    if scene.state.isVisible {
                        SleepTimerCard(
                            remaining: scene.timerRemaining,
                            onPreset: { scene.startTimer(minutes: $0) },
                            onCustom: { timer = TimerModel() },
                            onCancel: { scene.cancelTimer() }
                        )
                        ForEach(scene.state.layers, id: \.soundId) { layer in
                            SoundLayerRow(
                                name: layer.name,
                                icon: layer.icon,
                                frequency: layer.frequency,
                                isPlaying: scene.state.isPlaying,
                                level: scene.volumes.level(of: layer.soundId),
                                onVolumeChange: { scene.changeVolume(of: layer.soundId, to: $0) },
                                onFrequencyChange: { scene.changeFrequency(of: layer.soundId, to: $0) }
                            )
                            .swipeActions {
                                Button(role: .destructive) {
                                    scene.remove(layer.soundId)
                                } label: {
                                    Label {
                                        Text(.actionRemove)
                                    } icon: {
                                        Image(systemName: "trash")
                                    }
                                }
                            }
                            .accessibilityAction {
                                scene.remove(layer.soundId)
                            } label: {
                                Text(.actionRemove)
                            }
                        }
                    } else {
                        ContentUnavailableView {
                            Text(.sceneEmpty)
                        }
                    }
                }
                .padding(.horizontal)
            }
            .swipeActionsContainer()
            .background { AppBackground() }
            .navigationTitle(Text(.scenePanelTitle))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { toolbar }
            .sheet(item: $timer) { model in
                TimerView(model: model)
            }
            .saveMixAlert(
                isPresented: $scene.isSaveQuestionShown,
                name: $saveName,
                onSave: { scene.save(name: $0) },
                onCancel: {}
            )
            .onChange(of: scene.state.isSaveDialogVisible) { _, isShown in
                if isShown { saveName = scene.state.saveDialogName }
            }
        }
    }

    @ToolbarContentBuilder private var toolbar: some ToolbarContent {
        ToolbarItem(placement: .cancellationAction) {
            Button(role: .close) { dismiss() }
        }
        ToolbarItemGroup(placement: .primaryAction) {
            Button {
                scene.askToSave()
            } label: {
                Label {
                    Text(.actionSaveScene)
                } icon: {
                    Image(systemName: "bookmark")
                }
            }
            .disabled(!scene.state.isVisible)
            Button {
                scene.stopAll()
                dismiss()
            } label: {
                Label {
                    Text(.actionStopAll)
                } icon: {
                    Image(systemName: "stop.fill")
                }
            }
            .disabled(!scene.state.isVisible)
        }
        ToolbarItem(placement: .primaryAction) {
            Button {
                scene.togglePlayPause()
            } label: {
                Label {
                    scene.state.isPlaying ? Text(.actionPause) : Text(.actionPlay)
                } icon: {
                    Image(systemName: scene.state.isPlaying ? "pause.fill" : "play.fill")
                }
            }
            .buttonStyle(.glassProminent)
            .disabled(!scene.state.isVisible)
        }
        .visibilityPriority(.high)
    }
}
