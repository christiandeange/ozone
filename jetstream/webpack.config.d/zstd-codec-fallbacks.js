// zstd-codec contains Node-specific branches which webpack parses even though
// its browser runtime never executes them. Keep those built-ins out of the
// browser bundle while preserving the package's WebAssembly implementation.
config.resolve.fallback = {
  ...(config.resolve.fallback || {}),
  crypto: false,
  fs: false,
  path: false,
};
