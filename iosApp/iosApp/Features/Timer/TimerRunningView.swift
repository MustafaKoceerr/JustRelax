import Shared
import SwiftUI

/// A running timer: a ring that empties, the time left and when the sounds fade out, with Cancel
/// and Pause like the Clock app.
struct TimerRunningView: View {
    let model: TimerModel
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var blinks = false

    private var remaining: Int { Int(model.state.remainingSeconds) }
    private var total: Int { Int(model.state.totalSeconds) }
    private var isPaused: Bool { model.state.isPaused }

    var body: some View {
        ViewThatFits(in: .vertical) {
            VStack(spacing: 48) {
                Spacer(minLength: 0)
                ring
                Spacer(minLength: 0)
                controls
            }
            HStack(spacing: 32) {
                ring
                controls
            }
        }
        .padding()
        .onChange(of: isPaused, initial: true) { _, paused in
            guard !reduceMotion else { return }
            withAnimation(paused ? .easeInOut(duration: 0.7).repeatForever() : .default) {
                blinks = paused
            }
        }
    }

    private var ring: some View {
        ZStack {
            Circle()
                .stroke(Color(.surfaceContainerHighest), lineWidth: 12)
            Circle()
                .trim(from: 0, to: TimerText.progress(remaining: remaining, total: total))
                .stroke(remaining <= 5 ? Color.red : Color.accentColor, style: StrokeStyle(lineWidth: 12, lineCap: .round))
                .rotationEffect(.degrees(-90))
                .animation(.linear(duration: 1), value: remaining)
            VStack(spacing: 6) {
                Text(TimerText.preset(minutes: total / 60))
                    .font(.subheadline)
                    .bold()
                    .foregroundStyle(.secondary)
                Text(verbatim: TimerText.countdown(seconds: remaining))
                    .font(.largeTitle.scaled(by: 1.8).weight(.light).monospacedDigit())
                    .minimumScaleFactor(0.5)
                    .lineLimit(1)
                    .opacity(blinks ? 0.25 : 1)
                    .contentTransition(.numericText(countsDown: true))
                if isPaused, reduceMotion {
                    Text(.timerActionPause)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                } else if let endsAt = model.endsAt {
                    Label {
                        Text(endsAt, format: .dateTime.hour().minute())
                    } icon: {
                        Image(systemName: "bell.fill")
                    }
                    .font(.subheadline)
                    .bold()
                    .foregroundStyle(.secondary)
                } else {
                    // Keeps the layout steady while paused.
                    Text(verbatim: " ").font(.subheadline)
                }
            }
            .padding(24)
        }
        .frame(maxWidth: 320, maxHeight: 320)
        .aspectRatio(1, contentMode: .fit)
        .accessibilityElement(children: .combine)
    }

    private var controls: some View {
        HStack {
            Button {
                model.cancel()
            } label: {
                Text(.actionCancel)
                    .frame(width: 80, height: 80)
            }
            .buttonStyle(.glass)
            .buttonBorderShape(.circle)
            .accessibilityIdentifier(TestTags.shared.TIMER_CANCEL)
            Spacer()
            Button {
                model.togglePause()
            } label: {
                Label {
                    isPaused ? Text(.timerActionResume) : Text(.timerActionPause)
                } icon: {
                    Image(systemName: isPaused ? "play.fill" : "pause.fill")
                }
                .labelStyle(.titleOnly)
                .frame(width: 80, height: 80)
            }
            .buttonStyle(.glassProminent)
            .buttonBorderShape(.circle)
        }
        .bold()
        .frame(maxWidth: 360)
    }
}
