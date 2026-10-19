import Testing
@testable import JustRelax

struct BannerCenterTests {
    @Test func theFirstBannerShowsAtOnce() {
        let center = BannerCenter()
        let banner = Banner(message: "Saved")

        center.show(banner)

        #expect(center.current?.id == banner.id)
    }

    @Test func laterBannersWaitForTheCurrentOne() {
        let center = BannerCenter()
        let first = Banner(message: "First")
        let second = Banner(message: "Second")

        center.show(first)
        center.show(second)
        #expect(center.current?.id == first.id)

        center.dismiss(first)
        #expect(center.current?.id == second.id)

        center.dismiss(second)
        #expect(center.current == nil)
    }

    @Test func dismissingABannerThatIsNotShowingChangesNothing() {
        let center = BannerCenter()
        let shown = Banner(message: "Shown")
        let gone = Banner(message: "Gone")
        center.show(shown)

        center.dismiss(gone)

        #expect(center.current?.id == shown.id)
    }

    @Test func theActionRunsAndClosesTheBanner() {
        let center = BannerCenter()
        var undone = false
        let banner = Banner(message: "Mix deleted", action: Banner.Action(title: "Undo") { undone = true })
        center.show(banner)

        center.performAction(of: banner)

        #expect(undone)
        #expect(center.current == nil)
    }

    @Test func aBannerWithAnActionStaysLonger() {
        #expect(Banner(message: "Saved").duration == .seconds(4))
        #expect(Banner(message: "Deleted", action: Banner.Action(title: "Undo") {}).duration == .seconds(8))
    }
}
