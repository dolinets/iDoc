'use strict';

var activiti = require('../../components/activiti'),
  config = require('../../config/environment');

exports.setProcessChatMessage = function (req, res) {
  var data = {
    sBody: req.body.sBody
  };

  var options = {
    path: 'chat/process/setProcessChatMessage',
    query: {
      nID_Process_Activiti: req.body.nID_Process_Activiti,
      sLogin: req.body.sLogin,
      sKeyGroup: req.body.sKeyGroup,
      nID_ProcessChatMessage_Parent: req.body.nID_ProcessChatMessage_Parent,
      sLoginReferent:  req.body.sLoginReferent
    },
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  };

  // if (req.cookies.referent){
  //   if (JSON.parse(req.cookies.referent)){
  //     options.query.sLoginReferent = JSON.parse(req.cookies.referent).id;
  //   }
  // }

  if (req.body.taskServer) {
    options.taskServer = req.body.taskServer;
    delete req.body.taskServer;
  }

  activiti.post(options, function (error, statusCode, result) {
    if (error) {
      res.send(error);
    } else {
      res.status(statusCode).json(result);
    }
  }, data);
};

exports.getProcessChat = function (req, res) {
  var options = {
    path: 'chat/process/getProcessChat',
    query: req.query
  };

  if (req.query.taskServer) {
    options.taskServer = req.query.taskServer;
    delete req.query.taskServer;
  }

  activiti.get(options, function (error, statusCode, result) {
    res.statusCode = statusCode;
    res.send(result);
  });
};

exports.updateProcessChatMessage = function (req, res) {
  var data = {
    sBody: req.body.sBody
  };

  var options = {
    path: 'chat/process/updateProcessChatMessage',
    query: {
      nID_Process_Activiti: req.body.nID_Process_Activiti,
      sKeyGroup: req.body.sKeyGroup,
      sLogin: req.body.sLogin,
      nID_ProcessChatMessage: req.body.nID_ProcessChatMessage,
      sLoginReferent:  req.body.sLoginReferent
    },
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  };

  // if (req.cookies.referent){
  //   if (JSON.parse(req.cookies.referent)){
  //     options.query.sLoginReferent = JSON.parse(req.cookies.referent).id;
  //   }
  // }

  if (req.body.taskServer) {
    options.taskServer = req.body.taskServer;
    delete req.body.taskServer;
  }

  activiti.put(options, function (error, statusCode, result) {
    res.statusCode = statusCode;
    res.send(result);
  }, data);
};

exports.deleteProcessChatMessage = function(req, res) {
  var options = {
    path: 'chat/process/deleteProcessChatMessage',
    query: req.query
  };

  if (req.query.taskServer) {
    options.taskServer = req.query.taskServer;
    delete req.query.taskServer;
  }

  activiti.del(options, function(error, statusCode, result) {
    if (error) {
      res.send(error);
    } else {
      res.status(statusCode).json(result);
    }
  });
};
