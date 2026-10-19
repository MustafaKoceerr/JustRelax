import Shared
import SwiftUI

/// The sleep timer sheet: wheels and presets to set it, then a ring that empties as it runs.
struct TimerView: View {
    let model: TimerModel
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            Group {
                if model.state.isSetupMode {
                    TimerSetupView { model.start($0) }
                        .transition(.opacity)
                } else {
                    TimerRunningView(model: model)
                        .transition(.opacity)
                }
            }
            .animation(.easeInOut, value: model.state.isSetupMode)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background { AppBackground() }
            .navigationTitle(Text(.sleepTimer))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(role: .close) { dismiss() }
                }
            }
        }
        .task { await model.follow() }
    }
}
