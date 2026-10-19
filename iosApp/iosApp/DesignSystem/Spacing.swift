import CoreGraphics

/// The few fixed gaps the design relies on; everywhere else the system's spacing applies.
enum Spacing {
    /// Between the sound tiles and between cards in a grid.
    static let grid: CGFloat = 10
    /// Inside a sound tile.
    static let tile: CGFloat = 12
}
