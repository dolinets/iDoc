'use strict';

var _ = require('lodash');
var activiti = require('../../components/activiti');


module.exports.getTable = function(req, res) {

  var options = {
    path: 'object/file/getProcessAttach',
    query: req.query
  };

  if (req.query.taskServer) {
    options.taskServer = req.query.taskServer;
    delete req.query.taskServer;
  } else if (req.params.taskServer) {
    options.taskServer = req.params.taskServer;
    delete req.params.taskServer;
  }

  activiti.get(options, function (error, statusCode, result) {
    if (error) {
      res.send(error);
    } else {
      res.status(statusCode).json(result);
    }
  });

};
