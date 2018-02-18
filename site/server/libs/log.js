var winston = require('winston/lib/winston');

(function(){
  try{
    var app = require('../../client/app');
  }
  catch (e) {
    var app = require('../../public/app');
  }
})();



function getLogger(module) {

    var path = module.filename.split('/').slice(-2).join('/');

    return new winston.Logger({
        transports:[
            new winston.transports.Console({
                colorize: true,
               // level: app.get('env') === 'development' ? 'debug' : 'error',
                label: path
            })
        ]
    })

}

module.exports = getLogger;
