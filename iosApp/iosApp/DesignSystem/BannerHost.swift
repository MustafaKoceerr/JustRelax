import SwiftUI

/// Shows the banner center's banners above the bottom bars while `isActive`.
private struct BannerHost: ViewModifier {
    let isActive: Bool
    @Environment(\.bannerCenter) private var center
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    func body(content: Content) -> some View {
        content
            .safeAreaInset(edge: .bottom) {
                if isActive, let center, let banner = center.current {
                    MessageBanner(banner: banner) {
                        center.performAction(of: banner)
                    }
                    .padding(.horizontal)
                    .transition(reduceMotion ? .opacity : .move(edge: .bottom).combined(with: .opacity))
                    .task(id: banner.id) {
                        AccessibilityNotification.Announcement(banner.message).post()
                        do {
                            try await Task.sleep(for: banner.duration)
                            center.dismiss(banner)
                        } catch {
                            // The view went away; the banner stays for the next host.
                        }
                    }
                }
            }
            .animation(.default, value: center?.current?.id)
    }
}

extension View {
    /// Shows banners at the bottom of this view; only one host should be active at a time.
    func bannerHost(isActive: Bool = true) -> some View {
        modifier(BannerHost(isActive: isActive))
    }
}
