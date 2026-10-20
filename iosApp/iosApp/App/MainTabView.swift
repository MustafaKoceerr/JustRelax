import SwiftUI

/// The tab bar; each tab keeps its own navigation stack.
struct MainTabView: View {
    @State private var navigator = AppNavigator()
    @State private var scene: SceneModel?
    @State private var showsScene = false
    @State private var timer: TimerModel?
    @State private var saveName = ""
    @Environment(\.bannerCenter) private var banners

    private var sceneID: ObjectIdentifier? { scene.map(ObjectIdentifier.init) }

    var body: some View {
        tabs
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
        .environment(\.navigator, navigator)
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

extension MainTabView {
    private var tabs: some View {
        @Bindable var navigator = navigator
        return TabView(selection: $navigator.selected) {
            ForEach(AppTab.allCases, id: \.self) { tab in
                Tab(value: tab) {
                    TabRoot(tab: tab, navigator: navigator)
                } label: {
                    Label(tab.title, systemImage: tab.systemImage)
                }
                // On the Tab: set on its Label, the id reached the tab bar button only sometimes.
                .accessibilityIdentifier(tab.accessibilityIdentifier)
            }
        }
    }
}

#Preview {
    MainTabView()
}
