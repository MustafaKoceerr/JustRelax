import Shared
import Testing
@testable import JustRelax

struct TileCornerTests {
    @Test func aDownloadInProgressShowsFirst() {
        let corner = TileCorner(isDownloading: true, isDownloaded: false, kind: .event, frequency: .often)

        #expect(corner == .downloading)
    }

    @Test func aSoundThatIsNotDownloadedOffersTheDownload() {
        #expect(TileCorner(isDownloading: false, isDownloaded: false, kind: .bed, frequency: nil) == .download)
    }

    @Test func aPlayingEventSoundShowsHowOftenItPlays() {
        #expect(TileCorner(isDownloading: false, isDownloaded: true, kind: .event, frequency: .rare) == .frequency(.rare))
    }

    @Test func aStoppedEventSoundSaysItPlaysNowAndThen() {
        #expect(TileCorner(isDownloading: false, isDownloaded: true, kind: .event, frequency: nil) == .event)
    }

    @Test func aLoopingSoundShowsNothing() {
        #expect(TileCorner(isDownloading: false, isDownloaded: true, kind: .bed, frequency: nil) == .none)
    }
}
