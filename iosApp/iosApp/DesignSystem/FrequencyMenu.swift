import Shared
import SwiftUI

/// A small chip with how often an event sound plays; tapping it picks Rare, Normal or Often.
struct FrequencyMenu: View {
    let frequency: EventFrequency
    let onChange: (EventFrequency) -> Void

    var body: some View {
        Menu {
            ForEach(EventFrequency.allCases, id: \.self) { option in
                Button {
                    onChange(option)
                } label: {
                    if option == frequency {
                        Label {
                            Text(option.title)
                        } icon: {
                            Image(systemName: "checkmark")
                        }
                    } else {
                        Text(option.title)
                    }
                }
            }
        } label: {
            HStack(spacing: 2) {
                Text(frequency.title)
                Image(systemName: "chevron.up.chevron.down")
                    .imageScale(.small)
            }
            .font(.footnote)
            .bold()
            .foregroundStyle(Color(.onSecondaryContainer))
            .padding(.horizontal, 8)
            .frame(minHeight: 28)
            .background(Color(.secondaryContainer), in: .capsule)
            .contentShape(.rect)
            .frame(minWidth: 44, minHeight: 44)
        }
        .menuOrder(.fixed)
    }
}

#Preview {
    FrequencyMenu(frequency: .often) { _ in }
}
