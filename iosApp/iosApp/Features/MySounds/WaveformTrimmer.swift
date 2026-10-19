import Shared
import SwiftUI

/// The file's waveform with two handles; dragging moves whichever handle is nearer.
struct WaveformTrimmer: View {
    let peaks: [Float]
    let sourceMs: Int64
    let range: TrimRange
    let onMoveStart: (Int64) -> Void
    let onMoveEnd: (Int64) -> Void

    @State private var width: CGFloat = 0
    @State private var dragging: TrimGeometry.Handle?

    private var geometry: TrimGeometry { TrimGeometry(width: width, sourceMs: sourceMs) }

    var body: some View {
        Canvas { context, size in
            guard !peaks.isEmpty else { return }
            let step = size.width / CGFloat(peaks.count)
            let barWidth = max(step * 0.6, 1)
            let startX = geometry.x(atMs: range.startMs)
            let endX = geometry.x(atMs: range.endMs)
            for (index, peak) in peaks.enumerated() {
                let x = CGFloat(index) * step + step / 2
                let height = max(CGFloat(peak) * size.height, 2)
                let bar = Path(roundedRect: CGRect(x: x - barWidth / 2, y: (size.height - height) / 2, width: barWidth, height: height), cornerRadius: barWidth / 2)
                let inside = x >= startX && x <= endX
                context.fill(bar, with: inside ? .color(.accentColor) : .color(.primary.opacity(0.25)))
            }
            for handleX in [startX, endX] {
                context.fill(Path(CGRect(x: handleX - 1.5, y: 0, width: 3, height: size.height)), with: .color(Color(.tertiaryAccent)))
                for y in [CGFloat(0), size.height] {
                    context.fill(Path(ellipseIn: CGRect(x: handleX - 7, y: y - 7, width: 14, height: 14)), with: .color(Color(.tertiaryAccent)))
                }
            }
        }
        .frame(height: 140)
        .padding(.vertical, 8)
        .onGeometryChange(for: CGFloat.self) { $0.size.width } action: { width = $0 }
        .contentShape(.rect)
        .gesture(trimDrag)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(Text(.mysoundsDragHint))
        .accessibilityChildren {
            handleElement(label: ClipTimeKt.formatClipTime(ms: range.startMs), ms: range.startMs, onMove: onMoveStart)
            handleElement(label: ClipTimeKt.formatClipTime(ms: range.endMs), ms: range.endMs, onMove: onMoveEnd)
        }
    }

    private var trimDrag: some Gesture {
        DragGesture(minimumDistance: 0)
            .onChanged { drag in
                let handle = dragging ?? geometry.nearerHandle(toX: drag.startLocation.x, startMs: range.startMs, endMs: range.endMs)
                dragging = handle
                let ms = geometry.ms(atX: drag.location.x)
                handle == .start ? onMoveStart(ms) : onMoveEnd(ms)
            }
            .onEnded { _ in dragging = nil }
    }

    /// A handle VoiceOver can move a tenth of a second at a time.
    private func handleElement(label: String, ms: Int64, onMove: @escaping (Int64) -> Void) -> some View {
        Rectangle()
            .accessibilityLabel(Text(verbatim: label))
            .accessibilityAdjustableAction { direction in
                onMove(ms + (direction == .increment ? 100 : -100))
            }
    }
}
