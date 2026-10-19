import Shared
import SwiftUI

/// The tip jar: one person makes the app, and a small tip keeps it going.
struct SupportView: View {
    let model: SupportModel
    @Environment(\.bannerCenter) private var banners

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                Image(systemName: "cup.and.saucer.fill")
                    .font(.system(size: 44))
                    .foregroundStyle(Color(.onPrimaryContainer))
                    .symbolEffect(.bounce, options: .repeat(.periodic(delay: 2)))
                    .frame(width: 96, height: 96)
                    .background(Color(.primaryContainer), in: .circle)
                    .accessibilityHidden(true)
                Text(.supportHeadline)
                    .font(.title2)
                    .bold()
                Text(.supportBody)
                    .foregroundStyle(.secondary)
                if model.state.thankedTier != nil {
                    thanks
                        .transition(.scale.combined(with: .opacity))
                }
                tips
                Text(.supportNote)
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }
            .multilineTextAlignment(.center)
            .padding(24)
            .frame(maxWidth: 560)
            .frame(maxWidth: .infinity)
            .animation(.spring, value: model.state.thankedTier)
        }
        .background { AppBackground() }
        .navigationTitle(Text(.supportTitle))
        .navigationBarTitleDisplayMode(.inline)
        .task { await model.followState(banners: banners) }
    }

    private var thanks: some View {
        VStack(spacing: 6) {
            Image(systemName: "heart.fill")
                .font(.title)
                .foregroundStyle(.tint)
                .symbolEffect(.bounce, value: model.state.thankedTier)
            Text(.supportThanksTitle)
                .font(.headline)
            Text(.supportThanksBody)
        }
        .padding()
        .frame(maxWidth: .infinity)
        .background(Color(.primaryContainer), in: .rect(cornerRadius: Radius.large))
    }

    @ViewBuilder private var tips: some View {
        if model.state.isLoading {
            ProgressView()
        } else if model.state.tips.isEmpty {
            Text(.supportUnavailable)
                .foregroundStyle(.secondary)
        } else {
            VStack(spacing: 10) {
                ForEach(model.state.tips, id: \.tier) { product in
                    let isBuying = model.state.purchasingTier == product.tier
                    Button {
                        model.tip(product.tier)
                    } label: {
                        HStack(spacing: 12) {
                            Image(systemName: product.tier.systemImage)
                                .foregroundStyle(Color(.onSecondaryContainer))
                                .frame(width: 44, height: 44)
                                .background(Color(.secondaryContainer), in: .circle)
                            Text(product.tier.title)
                                .font(.headline)
                                .frame(maxWidth: .infinity, alignment: .leading)
                            if isBuying {
                                ProgressView()
                            } else {
                                Text(verbatim: product.price)
                                    .bold()
                                    .foregroundStyle(Color(.onPrimary))
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(.tint, in: .capsule)
                            }
                        }
                        .padding(14)
                        .background(Color(.surfaceContainerHigh), in: .rect(cornerRadius: Radius.large))
                    }
                    .buttonStyle(.plain)
                    .disabled(model.state.purchasingTier != nil)
                    .opacity(model.state.purchasingTier != nil && !isBuying ? 0.5 : 1)
                }
            }
        }
    }
}
