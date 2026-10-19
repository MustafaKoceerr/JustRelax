import Shared
import SwiftUI

/// Trim an imported file, choose how it plays, name it and file it under a category.
struct SoundEditorView: View {
    @Bindable var model: SoundEditorModel
    @Environment(\.bannerCenter) private var banners
    @Environment(\.navigator) private var navigator

    var body: some View {
        content
            .background { AppBackground() }
            .navigationTitle(Text(.mysoundsEditorTitle))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    if model.state.isSaving {
                        ProgressView()
                    } else {
                        Button(action: model.save) {
                            Text(.actionSave)
                        }
                        .disabled(!model.state.canSave)
                    }
                }
            }
            .task { await model.followState(banners: banners) { navigator?.pop() } }
    }

    @ViewBuilder private var content: some View {
        if model.state.isLoading {
            ProgressView()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if model.state.loadFailed {
            ContentUnavailableView {
                Label {
                    Text(.mysoundsLoadFailed)
                } icon: {
                    Image(systemName: "waveform.slash")
                }
            }
        } else {
            form
        }
    }

    private var form: some View {
        Form {
            Section {
                WaveformTrimmer(
                    peaks: model.state.peaks.map(\.floatValue),
                    sourceMs: model.state.sourceMs,
                    range: model.state.range,
                    onMoveStart: model.moveStart,
                    onMoveEnd: model.moveEnd
                )
                HStack {
                    Text(EditorText.selection(model.state.range))
                        .font(.headline)
                        .monospacedDigit()
                    Spacer()
                    Button(action: model.togglePreview) {
                        Label {
                            model.state.isPreviewing ? Text(.actionStop) : Text(.actionListen)
                        } icon: {
                            Image(systemName: model.state.isPreviewing ? "stop.fill" : "play.fill")
                        }
                    }
                    .buttonStyle(.bordered)
                    .buttonBorderShape(.capsule)
                }
            } footer: {
                if let problem = model.state.trimProblem {
                    Text(EditorText.problem(problem))
                        .foregroundStyle(.red)
                } else {
                    Text(.mysoundsDragHint)
                }
            }
            .listRowBackground(Color(.surfaceContainerHigh))

            Section {
                Picker(selection: $model.kind) {
                    Text(.mysoundsKindBed).tag(SoundKind.bed)
                    Text(.mysoundsKindEvent).tag(SoundKind.event)
                } label: {
                    EmptyView()
                }
                .pickerStyle(.segmented)
                .disabled(model.state.allowedKinds.count < 2)
            } footer: {
                Text(EditorText.kindHint(kind: model.state.kind, allowed: model.state.allowedKinds))
            }
            .listRowBackground(Color(.surfaceContainerHigh))

            Section {
                TextField(text: $model.name) {
                    Text(.mysoundsName)
                }
                Picker(selection: $model.category) {
                    ForEach(SoundCategory.allCases, id: \.self) { category in
                        Label {
                            Text(category.title)
                        } icon: {
                            Image(systemName: category.systemImage)
                        }
                        .tag(Optional(category))
                    }
                } label: {
                    Text(.mysoundsCategory)
                }
                .pickerStyle(.navigationLink)
            }
            .listRowBackground(Color(.surfaceContainerHigh))

            Section {
                LazyVGrid(columns: [GridItem(.adaptive(minimum: 44), spacing: 8)], spacing: 8) {
                    ForEach(SoundIcon.allCases, id: \.self) { icon in
                        let isSelected = icon == model.state.shownIcon
                        Button {
                            model.selectIcon(icon)
                        } label: {
                            SoundIconBadge(icon: icon, isActive: isSelected, diameter: 44)
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel(Text(verbatim: "\(icon)"))
                        .accessibilityAddTraits(isSelected ? .isSelected : [])
                    }
                }
                .padding(.vertical, 4)
            } header: {
                Text(.mysoundsIcon)
            }
            .listRowBackground(Color(.surfaceContainerHigh))
        }
        .scrollContentBackground(.hidden)
    }
}
