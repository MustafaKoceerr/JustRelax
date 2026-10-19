import Shared

extension ViewModelHost {
    /// The host's ViewModel of `type`, made by Koin with `parameters` the first time it is asked for.
    func viewModel<ViewModel: Lifecycle_viewmodelViewModel>(_ type: ViewModel.Type, parameters: [Any] = []) -> ViewModel {
        guard let viewModel = resolve(viewModelClass: type, parameters: parameters) as? ViewModel else {
            fatalError("Koin made something other than \(type)")
        }
        return viewModel
    }
}
