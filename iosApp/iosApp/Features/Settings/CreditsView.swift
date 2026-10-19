import Shared
import SwiftUI

/// The sounds from other authors, each with its licence and a link to its source.
struct CreditsView: View {
    let model: CreditsModel

    var body: some View {
        content
            .background { AppBackground() }
            .navigationTitle(Text(.creditsTitle))
            .task { await model.follow() }
    }

    @ViewBuilder private var content: some View {
        if model.state.isLoading {
            Color.clear
        } else if model.state.credits.isEmpty {
            ContentUnavailableView {
                Label {
                    Text(.creditsEmpty)
                } icon: {
                    Image(systemName: "heart")
                }
            }
        } else {
            List(model.state.credits, id: \.sourceUrl) { credit in
                Button {
                    model.openSource(credit)
                } label: {
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(credit.title)
                            Text(SupportText.credit(credit))
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                        }
                        Spacer()
                        Image(systemName: "arrow.up.right")
                            .font(.footnote)
                            .bold()
                            .foregroundStyle(.tertiary)
                            .accessibilityHidden(true)
                    }
                    .contentShape(.rect)
                }
                .buttonStyle(.plain)
                .listRowBackground(Color(.surfaceContainerHigh))
            }
            .scrollContentBackground(.hidden)
        }
    }
}
