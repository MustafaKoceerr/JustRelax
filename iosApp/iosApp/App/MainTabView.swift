import SwiftUI

/// The tab bar; each tab keeps its own navigation stack.
struct MainTabView: View {
    @State private var selection = AppTab.home

    var body: some View {
        TabView(selection: $selection) {
            ForEach(AppTab.allCases, id: \.self) { tab in
                Tab(value: tab) {
                    NavigationStack {
                        switch tab {
                        case .home: LibraryScreen()
                        default: TabPlaceholder(tab: tab)
                        }
                    }
                    .bannerHost(isActive: selection == tab)
                } label: {
                    Label(tab.title, systemImage: tab.systemImage)
                        .accessibilityIdentifier(tab.accessibilityIdentifier)
                }
            }
        }
    }
}

/// Stands in for a tab's screen until it is written.
private struct TabPlaceholder: View {
    let tab: AppTab

    var body: some View {
        ContentUnavailableView(
            String(localized: tab.title),
            systemImage: tab.systemImage,
            description: Text(verbatim: "This screen is being rebuilt in SwiftUI.")
        )
        .navigationTitle(Text(tab.title))
    }
}

#Preview {
    MainTabView()
}
