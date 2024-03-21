import SwiftUI
import ios

@main
struct ozoneApp: App {
    init() {
        ios.MainViewControllerKt.initialize()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
