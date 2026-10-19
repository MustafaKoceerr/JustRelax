import Shared
import SwiftUI

/// Hour, minute and second wheels, like the Clock app's timer, with Android's presets below.
struct TimerSetupView: View {
    let onStart: (TimerDial) -> Void
    @State private var dial = TimerDial(minutes: 30)

    var body: some View {
        ViewThatFits(in: .vertical) {
            VStack(spacing: 32) {
                Spacer(minLength: 0)
                wheels
                presets
                Spacer(minLength: 0)
                startButton
            }
            HStack(spacing: 24) {
                wheels
                VStack(spacing: 20) {
                    presets
                    startButton
                }
            }
        }
        .padding()
    }

    private var wheels: some View {
        HStack(spacing: 0) {
            wheel($dial.hours, range: 0..<24, unit: .timerUnitHour)
                .accessibilityIdentifier(TestTags.shared.TIMER_WHEEL_HOUR)
            wheel($dial.minutes, range: 0..<60, unit: .timerUnitMinute)
                .accessibilityIdentifier(TestTags.shared.TIMER_WHEEL_MINUTE)
            wheel($dial.seconds, range: 0..<60, unit: .timerUnitSecond)
                .accessibilityIdentifier(TestTags.shared.TIMER_WHEEL_SECOND)
        }
        .frame(maxWidth: 360)
    }

    private func wheel(_ value: Binding<Int>, range: Range<Int>, unit: LocalizedStringResource) -> some View {
        HStack(spacing: 4) {
            Picker(selection: value) {
                ForEach(range, id: \.self) { number in
                    Text(number, format: .number).monospacedDigit()
                }
            } label: {
                Text(unit)
            }
            .pickerStyle(.wheel)
            .frame(maxWidth: 64)
            .clipped()
            Text(unit)
                .font(.footnote)
                .bold()
                .lineLimit(1)
                .fixedSize()
                .accessibilityHidden(true)
        }
        .frame(maxWidth: .infinity)
    }

    private var presets: some View {
        LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 10), count: 3), spacing: 10) {
            ForEach(TimerDial.presets, id: \.self) { minutes in
                let isSelected = dial.matches(minutes: minutes)
                Button {
                    dial = TimerDial(minutes: minutes)
                } label: {
                    Text(TimerText.preset(minutes: minutes))
                        .lineLimit(1)
                        .minimumScaleFactor(0.8)
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.glass)
                .tint(isSelected ? Color(.primaryContainer) : nil)
                .foregroundStyle(isSelected ? Color(.onPrimaryContainer) : Color.primary)
                .bold(isSelected)
                .accessibilityAddTraits(isSelected ? .isSelected : [])
            }
        }
        .frame(maxWidth: 420)
    }

    private var startButton: some View {
        Button {
            onStart(dial)
        } label: {
            Text(.timerActionStart)
                .frame(minWidth: 160)
        }
        .buttonStyle(.glassProminent)
        .controlSize(.extraLarge)
        .disabled(!dial.canStart)
        .accessibilityIdentifier(TestTags.shared.TIMER_START)
    }
}

#Preview {
    TimerSetupView { _ in }
}
