@file:OptIn(ExperimentalWasmJsInterop::class)

package sh.christian.ozone.jetstream

// Node's fetch cannot resolve the browser-relative dictionary URL.
// Serve the packaged resource locally in tests. Karma serves the same URL in browsers.
@JsFun("""() => {
  if (!globalThis.process?.versions?.node || globalThis.__jetstreamTestFetch) return;
  globalThis.__jetstreamTestFetch = true;
  const originalFetch = globalThis.fetch;
  globalThis.fetch = async (url, ...args) => {
    if (url === '/files/zstd_dictionary.bin') {
      const fs = await import(/* webpackIgnore: true */ 'node:fs/promises');
      const bytes = await fs.readFile(new URL('./files/zstd_dictionary.bin', import.meta.url));
      return new Response(bytes);
    }
    return originalFetch(url, ...args);
  };
}""")
internal actual external fun prepareJetstreamTestRuntime()
