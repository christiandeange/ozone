const path = require('path');
const dictionary = path.resolve(__dirname, 'kotlin/files/zstd_dictionary.bin');

config.files.push({ pattern: dictionary, included: false, served: true, watched: false });
config.proxies['/files/zstd_dictionary.bin'] = '/absolute' + dictionary;

