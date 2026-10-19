import Shared
import SwiftUI

/// The sleep timer in the scene sheet: presets while idle, the time left while it runs.
struct SleepTimerCard: View {
    let remaining: Int?
    let onPreset: (Int) -> Void
    let onCustom: () -> Void
    let onCancel: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Label {
                VStack(alignment: .leading, spacing: 2) {
                    Text(.sleepTimer)
                        .font(.headline)
                    if let remaining {
                        Text(.sceneTimerEndsIn(TimerText.countdown(seconds: remaining)))
                            .font(.subheadline)
                            .monospacedDigit()
                    } else {
                        Text(.sceneTimerHint)
                            .font(.subheadline)
                    }
                }
            } icon: {
                Image(systemName: "moon.fill")
            }

            if remaining == nil {
                FlowLayout(alignment: .leading) {
                    ForEach([15, 30, 60], id: \.self) { minutes in
                        Button(TimerText.preset(minutes: minutes)) { onPreset(minutes) }
                    }
                    Button(action: onCustom) {
                        Label {
                            Text(.sceneTimerCustom)
                        } icon: {
                            Image(systemName: "slider.horizontal.3")
                        }
                    }
                }
                .buttonStyle(.bordered)
                .buttonBorderShape(.capsule)
                .tint(Color(.onSecondaryContainer))
            } else {
                HStack {
                    Spacer()
                    Button(action: onCustom) {
                        Text(.sceneTimerChange)
                    }
                    .buttonStyle(.borderless)
                    Button(action: onCancel) {
                        Text(.actionCancel)
                    }
                    .buttonStyle(.bordered)
                    .buttonBorderShape(.capsule)
                }
                .tint(Color(.onSecondaryContainer))
            }
        }
        .foregroundStyle(Color(.onSecondaryContainer))
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.secondaryContainer), in: .rect(cornerRadius: Radius.large))
        .accessibilityIdentifier(TestTags.shared.SCENE_SLEEP_TIMER)
    }
}

#Preview {
    VStack {
        SleepTimerCard(remaining: nil, onPreset: { _ in }, onCustom: {}, onCancel: {})
        SleepTimerCard(remaining: 754, onPreset: { _ in }, onCustom: {}, onCancel: {})
    }
    .padding()
}
