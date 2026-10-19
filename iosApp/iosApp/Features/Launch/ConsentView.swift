import Shared
import SwiftUI

/// Asks once whether usage statistics and crash reports may be sent; either answer closes it.
struct ConsentView: View {
    let model: ConsentModel
    let onAnswered: () -> Void

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                Image(systemName: "chart.line.uptrend.xyaxis")
                    .font(.system(size: 34))
                    .foregroundStyle(Color(.onPrimaryContainer))
                    .frame(width: 72, height: 72)
                    .background(Color(.primaryContainer), in: .circle)
                    .accessibilityHidden(true)

                Text(.consentTitle)
                    .font(.title2)
                    .bold()
                    .multilineTextAlignment(.center)

                VStack(alignment: .leading, spacing: 16) {
                    Label {
                        Text(.consentPointSent)
                    } icon: {
                        Image(systemName: "checkmark.circle.fill").foregroundStyle(.tint)
                    }
                    Label {
                        Text(.consentPointNever)
                    } icon: {
                        Image(systemName: "lock.fill").foregroundStyle(.tint)
                    }
                }
                .padding(20)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color(.surfaceContainer), in: .rect(cornerRadius: Radius.large))

                Button {
                    model.openPrivacyPolicy()
                } label: {
                    Text(.consentPrivacyPolicy)
                }
                .bold()
                .buttonStyle(.borderless)

                ViewThatFits(in: .horizontal) {
                    HStack(spacing: 12) {
                        declineButton
                        acceptButton
                    }
                    VStack(spacing: 12) {
                        acceptButton
                        declineButton
                    }
                }
                .controlSize(.large)
                .buttonSizing(.flexible)

                Text(.consentLaterHint)
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
            }
            .padding(24)
            .frame(maxWidth: 520)
            .frame(maxWidth: .infinity)
        }
        .scrollBounceBehavior(.basedOnSize)
        .background { AppBackground() }
        .interactiveDismissDisabled()
        .task {
            await model.waitUntilAnswered()
            onAnswered()
        }
    }

    private var acceptButton: some View {
        Button {
            model.answer(granted: true)
        } label: {
            Text(.consentAccept)
                .frame(minWidth: 140)
        }
        .buttonStyle(.glassProminent)
        .accessibilityIdentifier(TestTags.shared.CONSENT_ACCEPT)
    }

    private var declineButton: some View {
        Button {
            model.answer(granted: false)
        } label: {
            Text(.consentDecline)
                .frame(minWidth: 140)
        }
        .buttonStyle(.glass)
        .accessibilityIdentifier(TestTags.shared.CONSENT_DECLINE)
    }
}
