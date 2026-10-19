import SwiftUI

/// The prompt field pinned above the tab bar, with the send button and the data notice.
struct AiComposer: View {
    @Binding var prompt: String
    let canSend: Bool
    let isLoading: Bool
    let onSend: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(alignment: .bottom, spacing: 8) {
                TextField(text: $prompt, prompt: Text(.aiPromptPlaceholder), axis: .vertical) {
                    Text(.aiPromptPlaceholder)
                }
                .lineLimit(1...4)
                .submitLabel(.send)
                .onSubmit { if canSend { onSend() } }
                .padding(.horizontal, 18)
                .padding(.vertical, 12)
                .glassEffect(.regular, in: .rect(cornerRadius: 24))

                Button(action: onSend) {
                    Group {
                        if isLoading {
                            ProgressView()
                        } else {
                            Label {
                                Text(.aiActionSend)
                            } icon: {
                                Image(systemName: "arrow.up")
                            }
                            .labelStyle(.iconOnly)
                        }
                    }
                    .bold()
                    .frame(width: 32, height: 32)
                }
                .buttonStyle(.glassProminent)
                .buttonBorderShape(.circle)
                .disabled(!canSend)
            }
            Text(.aiDataNoticeShort)
                .font(.footnote)
                .foregroundStyle(.secondary)
                .padding(.horizontal, 8)
        }
        .padding(.horizontal)
        .padding(.bottom, 8)
    }
}

#Preview {
    @Previewable @State var prompt = ""
    AiComposer(prompt: $prompt, canSend: true, isLoading: false) {}
}
