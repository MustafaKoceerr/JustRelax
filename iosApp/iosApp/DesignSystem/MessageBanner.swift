import SwiftUI

/// How a banner looks: a glass capsule with the message and an optional action.
struct MessageBanner: View {
    let banner: Banner
    let onAction: () -> Void

    var body: some View {
        HStack {
            Text(verbatim: banner.message)
                .font(.subheadline)
                .bold()
                .frame(maxWidth: .infinity, alignment: .leading)
            if let action = banner.action {
                Button(action.title, action: onAction)
                    .bold()
                    .buttonStyle(.borderless)
                    .frame(minHeight: 44)
            }
        }
        .padding(.leading, 20)
        .padding(.trailing, banner.action == nil ? 20 : 12)
        .frame(minHeight: 52)
        .glassEffect(.regular, in: .capsule)
    }
}

#Preview {
    @Previewable @State var center = BannerCenter()
    AppBackground()
        .bannerHost()
        .environment(\.bannerCenter, center)
        .task {
            center.show(Banner(message: "Mix deleted", action: Banner.Action(title: "Undo") {}))
        }
}
