var express = require('express');
var path = require('path');
var favicon = require('serve-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
var lessMiddleware = require('less-middleware');
var index = require('../server/routes/index');
var ejs = require('ejs');
var log = require('../server/libs/log')(module);
var multer = require('multer');
var upload = multer();
var engines = require('consolidate');
var sendmail = require('../server/sendmail');


var app = express();

// view engine setup
   app.set('views', path.join(__dirname + '/'));
   app.set('view engine', 'ejs');

   app.use(favicon(path.join(__dirname, '/', 'favicon.ico')));
   app.use(logger('dev'));
   app.use(bodyParser.json());
   app.use(bodyParser.urlencoded({ extended: false }));
   app.use(cookieParser());
   app.use(lessMiddleware(path.join(__dirname, 'app')));
   app.use(express.static(path.join(__dirname, 'app')));

   app.use('/', index);

   app.post('/', upload.array(), function (req, res, next) {
       log.info(req.body);
           sendmail.StartSendEmail(req.body);
             res.clearCookie();
   });

// catch 404 and forward to error handler
   app.use(function(req, res, next) {
       var err = new Error('Not Found');
           err.status = 404;
         next(err);
   });

// error handler
   app.use(function(err, req, res, next) {
  // set locals, only providing error in development
       res.locals.message = err.message;
       res.locals.error = req.app.get('env') === 'development' ? err : {};
  // render the error page
       res.status(err.status || 500);
       res.render('error');
   });

  module.exports = app;
