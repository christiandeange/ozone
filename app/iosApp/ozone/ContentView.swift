import SwiftUI
import ios

struct ContentView: View {
    var body: some View {
        ComposeViewControllerToSwiftUI().ignoresSafeArea()
    }
}

struct ComposeViewControllerToSwiftUI: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return ios.MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
