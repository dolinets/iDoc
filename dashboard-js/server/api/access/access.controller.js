'use strict';

var activiti = require('../../components/activiti');

exports.setsLoginPrincipal = function (req, res) {
  var options = {
    path: 'access/setsLoginPrincipal',
    query: req.query,
    headers: {
      'cookie': 'JSESSIONID=' + req.cookies.JSESSIONID,
    }
  };

  activiti.get(options, function (error, statusCode, result) {
    res.statusCode = statusCode;
    res.send(result);
  });
};
