var fs = require('fs')
  , path = require('path')

var storeDir = path.join(__dirname, 'tmp') 
if (!fs.existsSync(storeDir)) fs.mkdirSync(storeDir)

module.exports = {

  servers: [
    {
      type: 'http',
      config: {
        port: 8000,
        staticDir: path.join(__dirname, 'pages')
      }
    }, {
      type: 'websockets',
      config: {
        port: 8000
      }
    }, {
      type: 'osc',
      config: {
        port: 57130 
      }
    }
  ],

  connections: { 
    store: storeDir,
  }
}
