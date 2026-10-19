import SwiftUI

/// Asks for a mix's name: the iOS form of Android's SaveMixDialog.
struct SaveMixAlert: ViewModifier {
    @Binding var isPresented: Bool
    @Binding var name: String
    let onSave: (String) -> Void
    let onCancel: () -> Void

    func body(content: Content) -> some View {
        content.alert(Text(.saveMixDialogTitle), isPresented: $isPresented) {
            TextField(text: $name, prompt: Text(.saveMixDialogNamePlaceholder)) {
                Text(.saveMixDialogNameLabel)
            }
            Button(role: .cancel, action: onCancel) {
                Text(.actionCancel)
            }
            Button {
                onSave(name)
            } label: {
                Text(.actionSave)
            }
            .disabled(!Self.canSave(name))
        } message: {
            Text(.saveMixDialogDescription)
        }
    }

    /// A blank name can't be saved; Android shows an error for it, iOS disables Save.
    static func canSave(_ name: String) -> Bool {
        !name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }
}

extension View {
    func saveMixAlert(
        isPresented: Binding<Bool>,
        name: Binding<String>,
        onSave: @escaping (String) -> Void,
        onCancel: @escaping () -> Void
    ) -> some View {
        modifier(SaveMixAlert(isPresented: isPresented, name: name, onSave: onSave, onCancel: onCancel))
    }
}

#Preview {
    @Previewable @State var isPresented = true
    @Previewable @State var name = ""
    Button("Save mix") { isPresented = true }
        .saveMixAlert(isPresented: $isPresented, name: $name, onSave: { _ in }, onCancel: {})
}
