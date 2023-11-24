const webpack = require('webpack');

config.plugins = (config.plugins || []).concat([
    new webpack.ProvidePlugin({
        process: 'process/browser',
        Buffer: ['buffer', 'Buffer']
    })
]);
