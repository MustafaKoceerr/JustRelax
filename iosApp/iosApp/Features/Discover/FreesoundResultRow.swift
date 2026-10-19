import Shared
import SwiftUI

/// One search result: listen to it, see who made it, and add it.
struct FreesoundResultRow: View {
    let sound: FreesoundSound
    let isPreviewing: Bool
    let isDownloading: Bool
    let isAddDisabled: Bool
    let onPreview: () -> Void
    let onAdd: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Button(action: onPreview) {
                Label {
                    isPreviewing ? Text(.actionStop) : Text(.actionListen)
                } icon: {
                    Image(systemName: isPreviewing ? "stop.fill" : "play.fill")
                }
                .labelStyle(.iconOnly)
                .foregroundStyle(Color(.onPrimaryContainer))
                .frame(width: 44, height: 44)
                .background(Color(.primaryContainer), in: .circle)
            }
            .buttonStyle(.plain)

            VStack(alignment: .leading, spacing: 2) {
                Text(sound.name)
                    .font(.headline)
                    .lineLimit(1)
                Text(FreesoundText.details(of: sound))
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .lineLimit(2)
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            if isDownloading {
                ProgressView()
                    .frame(width: 44, height: 44)
            } else {
                Button(action: onAdd) {
                    Text(.freesoundAdd)
                }
                .buttonStyle(.bordered)
                .buttonBorderShape(.capsule)
                .disabled(isAddDisabled)
            }
        }
        .padding(12)
        .background(Color(.surfaceContainerHigh), in: .rect(cornerRadius: Radius.large))
    }
}
