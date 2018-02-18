'use strict';
var _ = require('lodash');
var config = require('../../config/environment');
var activiti = require('../../components/activiti');

module.exports.getCurrentServer = function (req, res) {
  var activiti = config.activiti,
      serverData = {
        nID_Server: activiti.nID_Server,
        host: activiti.host
      };

  if(serverData) {
    res.send(serverData);
    res.end();
  } else {
    res.send();
    res.end();
  }
};

module.exports.getNewServerName = function (req, res) {
  var options = {
    path: 'subject/getServer',
    query: {nID: req.query.id}
  };

  activiti.get(options, function (error, statusCode, result) {
    if (error) {
      res.send();
      res.end();
    } else {
      var sURL = JSON.parse(result).sURL.split('://')[1];
      res.send(sURL);
      res.end();
    }
  });
};
