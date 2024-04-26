import SwiftUI
import OzoneIos

@main
struct ozoneApp: App {
    init() {
        OzoneIos.MainViewControllerKt.initialize()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
