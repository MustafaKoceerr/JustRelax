import Testing
@testable import JustRelax

struct TrimGeometryTests {
    private let geometry = TrimGeometry(width: 200, sourceMs: 10_000)

    @Test func aPositionMapsToATimeAcrossTheFile() {
        #expect(geometry.ms(atX: 0) == 0)
        #expect(geometry.ms(atX: 100) == 5_000)
        #expect(geometry.ms(atX: 250) == 10_000)
        #expect(geometry.ms(atX: -10) == 0)
    }

    @Test func aTimeMapsBackToAPosition() {
        #expect(geometry.x(atMs: 2_500) == 50)
    }

    @Test func aDragMovesTheNearerHandle() {
        #expect(geometry.nearerHandle(toX: 30, startMs: 1_000, endMs: 8_000) == .start)
        #expect(geometry.nearerHandle(toX: 150, startMs: 1_000, endMs: 8_000) == .end)
    }
}
