// Serve test resources for browser tests
// Note: __dirname points to build dir, so we derive project root from basePath
const path = require('path');
const projectRoot = config.basePath.replace(/\/build\/.*$/, '');
const resourcesPath = projectRoot + '/src/commonTest/resources';

// Increase timeout for large file tests
config.client = config.client || {};
config.client.mocha = config.client.mocha || {};
config.client.mocha.timeout = 60000;

config.files = config.files || [];
config.files.push({
    pattern: resourcesPath + '/**/*',
    watched: false,
    included: false,
    served: true,
    nocache: false
});

// Proxy requests to serve resources at the expected paths
config.proxies = config.proxies || {};
config.proxies['/test_parsing/'] = '/absolute' + resourcesPath + '/test_parsing/';
config.proxies['/test_transform/'] = '/absolute' + resourcesPath + '/test_transform/';
config.proxies['/nst_files/'] = '/absolute' + resourcesPath + '/nst_files/';
config.proxies['/test_Json.NET/'] = '/absolute' + resourcesPath + '/test_Json.NET/';
config.proxies['/test_json-rust/'] = '/absolute' + resourcesPath + '/test_json-rust/';
config.proxies['/test_json-rustc_serialize/'] = '/absolute' + resourcesPath + '/test_json-rustc_serialize/';
