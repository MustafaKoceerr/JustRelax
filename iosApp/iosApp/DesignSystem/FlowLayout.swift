import SwiftUI

/// Lays views out in rows, starting a new row when the next view doesn't fit.
struct FlowLayout: Layout {
    var alignment: HorizontalAlignment = .center
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let sizes = subviews.map { $0.sizeThatFits(.unspecified) }
        let maxWidth = proposal.width ?? .infinity
        let rows = Self.rows(widths: sizes.map(\.width), maxWidth: maxWidth, spacing: spacing)
        let width = rows.map { rowWidth(sizes[$0]) }.max() ?? 0
        let height = rows.map { rowHeight(sizes[$0]) }.reduce(0, +) + spacing * CGFloat(max(rows.count - 1, 0))
        return CGSize(width: min(width, maxWidth), height: height)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let sizes = subviews.map { $0.sizeThatFits(.unspecified) }
        var y = bounds.minY
        for row in Self.rows(widths: sizes.map(\.width), maxWidth: bounds.width, spacing: spacing) {
            let free = bounds.width - rowWidth(sizes[row])
            let indent: CGFloat = switch alignment {
            case .leading: 0
            case .trailing: free
            default: free / 2
            }
            var x = bounds.minX + indent
            for index in row {
                subviews[index].place(at: CGPoint(x: x, y: y), proposal: ProposedViewSize(sizes[index]))
                x += sizes[index].width + spacing
            }
            y += rowHeight(sizes[row]) + spacing
        }
    }

    /// Splits views of the given widths into rows no wider than `maxWidth`.
    static func rows(widths: [CGFloat], maxWidth: CGFloat, spacing: CGFloat) -> [Range<Int>] {
        var rows: [Range<Int>] = []
        var start = 0
        var used: CGFloat = 0
        for (index, width) in widths.enumerated() {
            if index > start, used + spacing + width > maxWidth {
                rows.append(start..<index)
                start = index
                used = width
            } else {
                used += (index > start ? spacing : 0) + width
            }
        }
        if start < widths.count {
            rows.append(start..<widths.count)
        }
        return rows
    }

    private func rowWidth(_ sizes: ArraySlice<CGSize>) -> CGFloat {
        sizes.map(\.width).reduce(0, +) + spacing * CGFloat(max(sizes.count - 1, 0))
    }

    private func rowHeight(_ sizes: ArraySlice<CGSize>) -> CGFloat {
        sizes.map(\.height).max() ?? 0
    }
}

#Preview {
    FlowLayout {
        ForEach(["Rain", "Thunder", "Ocean waves", "Forest birds", "Fireplace", "Café", "Wind", "White noise"], id: \.self) { title in
            Text(verbatim: title)
                .padding(.horizontal, 14)
                .padding(.vertical, 8)
                .background(.quaternary, in: .capsule)
        }
    }
    .padding()
}
