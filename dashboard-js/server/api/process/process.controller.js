'use strict';

var activiti = require('../../components/activiti');
var NodeCache = require("node-cache");
var cache = new NodeCache();
//var logger = require('../../components/logger').setup();
      //logger.info('Express server listening on %d, in %s mode', config.port, app.get('env'));

exports.index = function (req, res) {

  var query = {};
  //query.size = 750;
  query.size = 1500;
  query.latest = true;

  var options = {
    path: 'repository/process-definitions',
    query: query
  };

  activiti.get(options, function (error, statusCode, result) {
    if (error) {
      res.send(error);
    } else {

      //logger.info('Express server listening on %d, in %s mode', config.port, app.get('env'));
//      logger.info('result='+result);
      res.status(200).send(result);
    }
  });
};

exports.getLoginBPs = function (req, res) {
  var user = JSON.parse(req.cookies.user), path;

  path = req.query.isOld && req.query.isOld === 'true' ? 'action/task/getLoginBPs' : 'subject/group/getBPs_ForReferent';

  var query = {
    'sLogin' : user.id
  };
  var options = {
    path: path,
    query: query
  };
  var cacheKey = JSON.stringify(options);
  var cachedValue = cache.get(cacheKey);
  if (cachedValue) {
    res.json(cachedValue);
  } else {
    activiti.get(options, function (error, statusCode, result) {
      if (error) {
        res.send(error);
      } else {
        cache.set(cacheKey, result, 86400);
        res.json(result);
      }
    });
  }
};

exports.getBPs_ForExport = function (req, res) {
  var user = JSON.parse(req.cookies.user), path;

  path = 'subject/group/getBPs_ForExport';

  var query = {
    'sLogin' : user.id
  };
  var options = {
    path: path,
    query: query
  };
  var cacheKey = JSON.stringify(options);
  var cachedValue = cache.get(cacheKey);
  if (cachedValue) {
    res.json(cachedValue);
  } else {
    activiti.get(options, function (error, statusCode, result) {
      if (error) {
        res.send(error);
      } else {
        cache.set(cacheKey, result, 86400);
        res.json(result);
      }
    });
  }
};

exports.getmIDTaskAndProcess = function (req, res) {
  var options = {
    path: 'action/task/getmID_TaskAndProcess',
    query: req.query
  };

  activiti.get(options, function (error, statusCode, result) {
    if (error) {
      res.send(error);
    } else {
      res.json(result);
    }
  });
};

exports.setProcessSubjectStatus = function (req, res) {
  var options = {
    path: 'subject/process/setProcessSubjectStatus',
    headers: {
      'Content-Type': 'text/html;charset=utf-8',
      'cookie': 'JSESSIONID=' + req.cookies.JSESSIONID
    },
    query: {}
  };

  if (req.cookies.referent) {
    var referent = JSON.parse(req.cookies.referent);
    options.query.sLoginReferent = referent.id;
  }

  if (req.body.taskServer) {
    options.taskServer = req.body.taskServer;
    delete req.body.taskServer;
  }

  activiti.post(options, function (error, statusCode, result) {
    error ? res.send(error) : res.status(statusCode).json(result);
  }, {queryParams: req.body});
};

exports.getAllBpForLogin = function (req, res) {
  var referent;
  var options = {
    path: 'process/getAllBpForLogin',
    query: req.query,
    headers: {
      'cookie': 'JSESSIONID=' + req.cookies.JSESSIONID,
    }
  };

  if (req.cookies.referent) {
    referent = JSON.parse(req.cookies.referent);
    options.query.sLoginReferent = referent.id;
  }

  activiti.get(options, function (error, statusCode, result) {
    if (error) {
      res.send(error);
    } else {
      res.json(result);
    }
  });
};

exports.getBPFields = function (req, res) {
  var options = {
    path: 'process/getBPFields',
    query: req.query
  };

  activiti.get(options, function (error, statusCode, result) {
    if (error) {
      res.send(error);
    } else {
      res.json(result);
    }
  });
};

