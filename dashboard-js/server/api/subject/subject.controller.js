'use strict';

var subjectService = require('./subject.service'),
  config = require('../../config/environment'),
  activiti = require('./../../components/activiti')
  , _ = require('lodash');

module.exports.getSubjectOrganJoinTaxList = function (req, res) {
  //TODO remove req and res as input
  subjectService.getSubjectOrganJoinTaxList(req, res, '/subject/getSubjectOrganJoinTax', req.query);
};


module.exports.getSubjectOrganJoin = function (req, res) {
  // {nID_SubjectOrgan:1} for test
  activiti.sendGetRequest(req, res, '/subject/getSubjectOrganJoins', _.extend(req.query, req.params));
};

module.exports.getSubjectStatus = function (req, res) {
  //activiti.sendGetRequest(req, res, '/subject/getSubjectStatus', _.extend(req.query, req.params));
  var callback = function (error, response, body) {
    res.send(body);
    res.end();
  };

  var updatedQuery = req.query;
  var options = {
    path: 'subject/getSubjectStatus',
    query: updatedQuery,
    json: true
  };
  activiti.get(options, callback);
};

module.exports.getOrganAttributes = function (req, res) {
  var apiReq = activiti.buildRequest(req, '/subject/getSubjectOrganJoinAttributes', _.extend(req.query, req.params));
  apiReq.body = req.body;
  apiReq.json = true;
  activiti.executePostRequest(apiReq, res);
};

module.exports.sendPasswordOfUser = function (req, res) {
  var options = {
    path: '/subject/sendPasswordOfUser',
    query: req.query
  };
  activiti.get(options, function (error, statusCode, result) {
    if (error) {
      res.send(error);
    } else {
      res.send(result)
    }
  });
};

module.exports.sendPasswordOfUserCustom = function (req, res) {
  var user = JSON.parse(req.cookies.user);
  var options = {
    path: '/subject/sendPasswordOfUserCustom',
    query: _.extend(req.query, {
      sAdminMail: user.email,
      sAdminLogin: user.id
    })
  };

  activiti.get({path: 'action/identity/getGroups', query: {sLogin: user.id}}, function (error, statusCode, result) {
    if (error) {
      res.send(error);
    } else {
      if (result.indexOf('superadmin') > -1) {
        activiti.get(options, function (error, statusCode, result) {
          if (error) {
            res.send(error);
          } else {
            res.send(result)
          }
        });
      } else {
        res.send({"code":"SYSTEM_ERR","message":"Користувач не є адміністратором"});
      }
    }
  });
};

module.exports.sendPasswordsOfUsersOnServer = function (req, res) {
  var serverData = config.activiti.nID_Server;
  var user = JSON.parse(req.cookies.user);
  var options = {
    path: '/subject/sendPasswordsOfUsersOnServer',
    query: _.extend(req.query, {
      saAdminMail: user.email,
      bReset: false,
      nID_Server: serverData
    })
  };

  activiti.get({path: 'action/identity/getGroups', query: {sLogin: user.id}}, function (error, statusCode, result) {
    if (error) {
      res.send(error);
    } else {
      if (result.indexOf('superadmin') > -1) {
        activiti.get(options, function (error, statusCode, result) {
          if (error) {
            res.send(error);
          } else {
            res.send(result)
          }
        });
      } else {
        res.send({"code":"SYSTEM_ERR","message":"Користувач не є адміністратором"});
      }
    }
  });
};

