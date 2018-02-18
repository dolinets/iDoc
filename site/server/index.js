
(function(){
  try{
     app = require('../client/app');
  }
  catch (e) {
     app = require('../public/app');
  }
})();

var debug = require('debug')('app');
var https = require('https');
var http = require('http');
var config = require('./config/config');
var port = require('../process.json');
var log = require('./libs/log')(module);
var WebSocketServer = new require('ws');
var Static = require('node-static');
var path = require('path');
var fs = require('fs');


  var port_http = normalizePort(process.env.PORT || port.env.http_port);
    app.set('port', port_http);

    try {
      var options = {
        key: fs.readFileSync(config.keyPath),
        cert: fs.readFileSync(config.certPath)
      };

      var server = https.createServer(options, app);
    }catch (e){
       server = http.createServer(app);
    }
       server.listen(port_http, function () {
           log.info('Express server listening on port ' + port_http);
    });
       server.on('error', onError);
       server.on('listening', onListening);

   function normalizePort(val) {
       var port = parseInt(val, 10);
          if (isNaN(port)) {
    // named pipe
        return val;
       }
          if (port >= 0) {
    // port number
        return port;
       }
        return false;
    }


   function onError(error) {
          if (error.syscall !== 'listen') {
              throw error;
       }
          var bind = typeof port === 'string'
              ? 'Pipe ' + port_http
              : 'Port ' + port_http;

  // handle specific listen errors with friendly messages
        switch (error.code) {
            case 'EACCES':
             console.error(bind + ' requires elevated privileges');
             process.exit(1);
          break;
           case 'EADDRINUSE':
             console.error(bind + ' is already in use');
             process.exit(1);
          break;
       default:
          throw error;
       }
   }

   function onListening() {
       var addr = server.address();
       var bind = typeof addr === 'string'
           ? 'pipe ' + addr
           : 'port ' + addr.port;
       debug('Listening on ' + bind);
   }
