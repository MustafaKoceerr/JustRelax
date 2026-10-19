import SwiftUI

/// The tab bar; each tab keeps its own navigation stack.
struct MainTabView: View {
    @State private var selection = AppTab.home
    @State private var scene: SceneModel?
    @State private var showsScene = false
    @State private var timer: TimerModel?
    @State private var saveName = ""
    @Environment(\.bannerCenter) private var banners

    private var sceneID: ObjectIdentifier? { scene.map(ObjectIdentifier.init) }

    var body: some View {
        TabView(selection: $selection) {
            ForEach(AppTab.allCases, id: \.self) { tab in
                Tab(value: tab) {
                    NavigationStack {
                        switch tab {
                        case .home: LibraryScreen()
                        case .mixer: MixerScreen()
                        case .ai: AiScreen()
                        case .saved: SavedScreen { selection = .mixer }
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
        .tabBarMinimizeBehavior(.onScrollDown)
        .tabViewBottomAccessory(isEnabled: scene?.miniPlayer.isVisible ?? false) {
            if let scene {
                MiniPlayerView(scene: scene, onOpenScene: { showsScene = true }, onOpenTimer: { timer = TimerModel() })
            }
        }
        .sheet(isPresented: $showsScene) {
            if let scene {
                SceneView(scene: scene)
                    .presentationDetents([.medium, .large])
            }
        }
        .sheet(item: $timer) { model in
            TimerView(model: model)
        }
        .environment(\.scene, scene)
        .saveMixAlert(
            isPresented: Binding(
                get: { scene?.isSaveQuestionShown == true && !showsScene },
                set: { scene?.isSaveQuestionShown = $0 }
            ),
            name: $saveName,
            onSave: { scene?.save(name: $0) },
            onCancel: {}
        )
        .onChange(of: scene?.state.isSaveDialogVisible) { _, isShown in
            if isShown == true, let scene { saveName = scene.state.saveDialogName }
        }
        .onChange(of: scene?.state.isVisible) { _, isVisible in
            if isVisible == false { showsScene = false }
        }
        // The scene lives as long as the tabs, so the mini player always has it.
        .task { if scene == nil { scene = SceneModel() } }
        .task(id: sceneID) { await scene?.followState(banners: banners) }
        .task(id: sceneID) { await scene?.followMiniPlayer() }
        .task(id: sceneID) { await scene?.followTimer() }
        .task(id: sceneID) { await scene?.followVolumes() }
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
