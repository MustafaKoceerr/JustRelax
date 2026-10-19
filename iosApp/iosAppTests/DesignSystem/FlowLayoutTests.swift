import CoreGraphics
import Testing
@testable import JustRelax

struct FlowLayoutTests {
    @Test func itemsThatFitShareOneRow() {
        let rows = FlowLayout.rows(widths: [40, 40, 40], maxWidth: 140, spacing: 10)

        #expect(rows == [0..<3])
    }

    @Test func anItemThatDoesNotFitStartsANewRow() {
        let rows = FlowLayout.rows(widths: [60, 60, 60], maxWidth: 140, spacing: 10)

        #expect(rows == [0..<2, 2..<3])
    }

    @Test func spacingCountsOnlyBetweenItems() {
        // 50 + 10 + 50 = 110 fits exactly.
        let rows = FlowLayout.rows(widths: [50, 50], maxWidth: 110, spacing: 10)

        #expect(rows == [0..<2])
    }

    @Test func anItemWiderThanTheRowGetsARowOfItsOwn() {
        let rows = FlowLayout.rows(widths: [30, 200, 30], maxWidth: 100, spacing: 8)

        #expect(rows == [0..<1, 1..<2, 2..<3])
    }

    @Test func noItemsMakeNoRows() {
        #expect(FlowLayout.rows(widths: [], maxWidth: 100, spacing: 8).isEmpty)
    }
}
