'use strict';

var activiti = require('../../components/activiti');

exports.createTask = function (req, res) {
  var user = JSON.parse(req.cookies.user);
  var options = {
    path: 'action/task/getProcessTemplate',
      query: {
        sID_BP: req.query.sID_BP,
        sLogin: user.id
      }
  };

  res.setHeader('Content-Type', 'application/json;charset=utf-8');

  activiti.get(options, function (error, statusCode, result) {
    if (error) {
      res.send(error);
    } else {
      res.status(statusCode).send(result);
    }
  });
};

exports.submitCreatedTask = function (req, res) {
  var user = JSON.parse(req.cookies.user);
  var referent;
  var options = {
    path: 'action/task/startProcess',
    query: req.query,
    headers: {
      "Content-type": "application/json; charset=utf-8",
      'cookie': 'JSESSIONID=' + req.cookies.JSESSIONID
    }
  };
  options.query.sLogin = user.id;
  if (req.cookies.referent) {
    referent = JSON.parse(req.cookies.referent);
    options.query.sLoginReferent = referent.id;
  }

  activiti.post(options, function (error, statusCode, result) {
    res.statusCode = statusCode;
    res.send(result);
  }, req.body);
};
