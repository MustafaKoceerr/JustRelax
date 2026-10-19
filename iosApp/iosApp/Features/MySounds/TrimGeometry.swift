import CoreGraphics

/// Maps between positions on the waveform and times in the file.
struct TrimGeometry {
    enum Handle { case start, end }

    let width: CGFloat
    let sourceMs: Int64

    func ms(atX x: CGFloat) -> Int64 {
        guard width > 0 else { return 0 }
        let fraction = min(max(x / width, 0), 1)
        return Int64((fraction * CGFloat(sourceMs)).rounded())
    }

    func x(atMs ms: Int64) -> CGFloat {
        guard sourceMs > 0 else { return 0 }
        return width * CGFloat(ms) / CGFloat(sourceMs)
    }

    /// A drag moves whichever handle is nearer to where it starts, as on Android.
    func nearerHandle(toX x: CGFloat, startMs: Int64, endMs: Int64) -> Handle {
        abs(x - self.x(atMs: startMs)) <= abs(x - self.x(atMs: endMs)) ? .start : .end
    }
}
