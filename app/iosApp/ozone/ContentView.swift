import SwiftUI
import OzoneIos

struct ContentView: View {
    var body: some View {
        ComposeViewControllerToSwiftUI().ignoresSafeArea()
    }
}

struct ComposeViewControllerToSwiftUI: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return OzoneIos.MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
