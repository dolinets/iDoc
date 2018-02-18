﻿'use strict';

var _ = require('lodash');
var activiti = require('../../components/activiti');
var errors = require('../../components/errors');
var environment = require('../../config/environment');
var async = require('async');

function createHttpError(error, statusCode) {
  return {httpError: error, httpStatus: statusCode};
}


exports.getSubjects = function (req, res) {
  var saAccount = req.params.saAccount;
  var nID_SubjectAccountType = req.params.nID_SubjectAccountType;
  var central = environment.activiti_central;

  var options = {
    path: central.prot + '://' + central.host + ':' + central.port + '/' + central.rest + '/subject/getSubjectsBy?saAccount=["'+saAccount+'"]&nID_SubjectAccountType=' + nID_SubjectAccountType,
    json: true,
    doNotUseActivityConfigUrl: true
  };

  activiti.get(options, function (error, statusCode, result) {
    if (error) {
      console.log(result);
      console.log(error);
      res.send(errors.createError(errors.codes.EXTERNAL_SERVICE_ERROR,
        'Can\'t find user by ' + userID, error));
    } else {
      if(!_.isObject(result)){
        console.warn(result);
        result = {}
      }
      if(!result.hasOwnProperty('aSubjectAccount')){
        result['aSubjectAccount'] = [];
      }
      console.log(result);
      res.json(result);
    }
  });
};


exports.changePassword = function (req, res) {
  //console.log(req.body);
  activiti.post({
    path: 'action/task/changePassword',
    query: {
      sLoginOwner: req.body.sLoginOwner
    },
    headers: {
      'Content-Type': 'text/html;charset=utf-8'
    }
  }, function (error, statusCode, result) {
    console.log(error);
    console.log(statusCode);
    console.log(result);
    error ? res.send(error) : res.status(statusCode).json(result);
  }, JSON.stringify({
    sPasswordOld: req.body.sPasswordOld,
    sPasswordNew: req.body.sPasswordNew
  }), false);
};

