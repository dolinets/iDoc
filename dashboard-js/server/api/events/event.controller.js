'use strict';

var _ = require('lodash');
var activiti = require('../../components/activiti');

exports.getHistoryEvents = function (req, res) {
  var options = {
    path: 'action/event/getHistoryEventsByProcess',
    query: {}
  };

  options.query.nSize = 10;
  options.query.nStart = ((req.query.page || 0) * options.query.nSize - 1);
  options.query.sID_Order = req.query.sID_Order;

  if (req.query.taskServer) {
    options.taskServer = req.query.taskServer;
    delete req.query.taskServer;
  }

  activiti.get(options, function (error, statusCode, result) {
    if (error) {
      res.send(error);
    } else {
      if (JSON.parse(result)){
        res.json(JSON.parse(result));
      } else {
        res.json(result);
      }
    }
  });
};
