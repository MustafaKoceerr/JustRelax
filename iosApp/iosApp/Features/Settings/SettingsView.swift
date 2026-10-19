import Shared
import SwiftUI

/// Settings: appearance, sounds, privacy and support, grouped as on Android.
struct SettingsView: View {
    @Bindable var model: SettingsModel
    @Environment(\.bannerCenter) private var banners
    @Environment(\.navigator) private var navigator

    var body: some View {
        Form {
            header
            appearance
            sounds
            privacy
            support
        }
        .scrollContentBackground(.hidden)
        .background { AppBackground() }
        .navigationTitle(Text(.settingsTitle))
        .navigationBarTitleDisplayMode(.inline)
        .task { await model.followState(banners: banners) }
    }

    private var header: some View {
        Section {
            VStack(spacing: 4) {
                AppMark(diameter: 64)
                Text(verbatim: "Just Relax")
                    .font(.title3)
                    .bold()
                Text(.settingsTagline)
                    .foregroundStyle(.secondary)
                Text(.settingsVersion(model.state.versionName))
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }
            .frame(maxWidth: .infinity)
            .listRowBackground(Color.clear)
        }
    }

    private var appearance: some View {
        Section {
            LabeledContent {
                Picker(selection: $model.theme) {
                    ForEach(SettingsText.themes, id: \.self) { theme in
                        Text(SettingsText.title(of: theme)).tag(theme)
                    }
                } label: {
                    Text(.settingsThemeTitle)
                }
                .pickerStyle(.segmented)
                .fixedSize()
            } label: {
                Label {
                    Text(.settingsThemeTitle)
                } icon: {
                    SettingsIcon(systemImage: "paintpalette.fill")
                }
            }
            row(.settingsLanguageTitle, icon: "globe", value: languageName, opensOutside: true, action: model.openLanguage)
        } header: {
            Text(.themeSectionTitle)
        }
        .listRowBackground(Color(.surfaceContainerHigh))
    }

    private var sounds: some View {
        Section {
            if model.state.isLibraryComplete {
                Label {
                    Text(.downloadAllCompletedTitle)
                } icon: {
                    SettingsIcon(systemImage: "checkmark.circle.fill")
                }
            } else if model.state.isDownloadingLibrary {
                VStack(alignment: .leading, spacing: 8) {
                    Label {
                        Text(.downloadAllTitle)
                    } icon: {
                        SettingsIcon(systemImage: "icloud.and.arrow.down.fill")
                    }
                    ProgressView(value: Double(model.state.downloadProgress)) {
                        EmptyView()
                    } currentValueLabel: {
                        Text(verbatim: SettingsText.downloadProgress(model.state.downloadProgress))
                    }
                }
            } else {
                row(.downloadAllTitle, subtitle: .downloadAllSubtitleRecommended, icon: "icloud.and.arrow.down.fill", action: model.downloadLibrary)
            }
            row(.settingsMySoundsTitle, subtitle: .settingsMySoundsSubtitle, icon: "music.note.list", pushes: true) {
                navigator?.push(.mySounds)
            }
        } header: {
            Text(.sectionSounds)
        }
        .listRowBackground(Color(.surfaceContainerHigh))
    }

    private var privacy: some View {
        Section {
            Toggle(isOn: $model.isUsageDataEnabled) {
                Label {
                    VStack(alignment: .leading) {
                        Text(.settingsUsageDataTitle)
                        Text(.settingsUsageDataSubtitle)
                            .font(.footnote)
                            .foregroundStyle(.secondary)
                    }
                } icon: {
                    SettingsIcon(systemImage: "chart.line.uptrend.xyaxis")
                }
            }
            row(.privacyPolicyTitle, icon: "hand.raised.fill", opensOutside: true, action: model.openPrivacyPolicy)
            row(.termsConditionsTitle, icon: "doc.text.fill", opensOutside: true, action: model.openTerms)
        } header: {
            Text(.sectionPrivacy)
        }
        .listRowBackground(Color(.surfaceContainerHigh))
    }

    private var support: some View {
        Section {
            if model.state.canTip {
                row(.settingsSupportTitle, subtitle: .settingsSupportSubtitle, icon: "cup.and.saucer.fill", pushes: true) {
                    navigator?.push(.support)
                }
            }
            row(.rateAppTitle, subtitle: .rateAppSubtitle, icon: "star.fill", opensOutside: true, action: model.rateApp)
            row(.feedbackTitle, subtitle: .feedbackSubtitle, icon: "envelope.fill", opensOutside: true, action: model.sendFeedback)
            row(.creditsTitle, subtitle: .creditsSubtitle, icon: "heart.fill", pushes: true) {
                navigator?.push(.credits)
            }
        } header: {
            Text(.sectionSupport)
        }
        .listRowBackground(Color(.surfaceContainerHigh))
    }

    private var languageName: String {
        model.state.language == .system ? String(localized: .languageSystemDefault) : model.state.language.nativeName
    }

    /// A tappable row: a chevron when it opens a screen, an arrow when it leaves the app.
    private func row(
        _ title: LocalizedStringResource,
        subtitle: LocalizedStringResource? = nil,
        icon: String,
        value: String? = nil,
        pushes: Bool = false,
        opensOutside: Bool = false,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            HStack {
                Label {
                    VStack(alignment: .leading) {
                        Text(title)
                        if let subtitle {
                            Text(subtitle)
                                .font(.footnote)
                                .foregroundStyle(.secondary)
                        }
                    }
                } icon: {
                    SettingsIcon(systemImage: icon)
                }
                Spacer()
                if let value {
                    Text(verbatim: value)
                        .foregroundStyle(.secondary)
                }
                if pushes || opensOutside {
                    Image(systemName: pushes ? "chevron.right" : "arrow.up.right")
                        .font(.footnote)
                        .bold()
                        .foregroundStyle(.tertiary)
                        .accessibilityHidden(true)
                }
            }
            .contentShape(.rect)
        }
        .buttonStyle(.plain)
    }
}
