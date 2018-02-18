'use strict';

var activiti = require('../../components/activiti'),
    NodeCache = require("node-cache"),
    config = require('../../config/environment');


var cache = new NodeCache();
var cacheTtl = 1800; // 30min

var buildKey = function (params) {
  var key = 'BPs';
  if (params) {
    for (var k in params) {
      key += '&' + k + '=' + params[k];
    }
  }
  return key;
};

exports.getDocumentStepRights = function (req, res) {
  var user = JSON.parse(req.cookies.user);
  var query = {
    sLogin: user.id,
    nID_Process: req.query.nID_Process
  };
  activiti.get({
    path: '/common/document/getDocumentStepRights',
    query: query
  }, function (error, statusCode, result) {
    res.statusCode = statusCode;
    res.send(result)
  })
};

exports.getDocumentStepLogins = function (req, res) {
  var options = {
    path: '/common/document/getDocumentStepLogins',
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

exports.getProcessSubject = function (req, res) {
  activiti.get({
    path: '/subject/process/getProcessSubject',
    query: req.query
  }, function (error, statusCode, result) {
    res.statusCode = statusCode;
    res.send(result);
  })
};

exports.getBPs_ForReferent = function (req, res) {
  var isTestServer = config.bTest;

  var callbackTestServer = function (error, statusCode, result) {
    if (!error) {
      res.statusCode = statusCode;
      res.send(result);
    } else {
      console.error(error);
    }
  };

  var callback = function (error, statusCode, result) {
    if (!error) {
      cache.set(buildKey(req.query), result, cacheTtl);
      res.statusCode = statusCode;
      res.send(result);
    } else {
      console.error(error);
    }
  };

  if(isTestServer) {
    activiti.get({
      path: 'subject/group/getBPs_ForReferent',
      query: req.query
    }, callbackTestServer)
  } else {
    cache.get(buildKey(req.query), function (error, value) {
      if (value) {
        res.send(value);
      } else {
        activiti.get({
          path: 'subject/group/getBPs_ForReferent',
          query: req.query
        }, callback)
      }
    })
  }
};

exports.setDocument = function (req, res) {
  var user = JSON.parse(req.cookies.user);
  var referent;
  var options = {
    path: 'action/task/setDocument',
    query: {
      sID_BP: req.query.sID_BP,
      sLogin: user.id
    },
    headers: {
      'cookie': 'JSESSIONID=' + req.cookies.JSESSIONID
    }
  };
  if (req.cookies.referent) {
    referent = JSON.parse(req.cookies.referent);
    options.query.sLoginReferent = referent.id;
  }
  activiti.get(options, function (error, statusCode, result) {
    res.statusCode = statusCode;
    res.send(result);
  });
};

exports.getProcessSubjectTree = function (req, res) {
  activiti.get({
    path: '/subject/process/getProcessSubjectTree',
    query: req.query
  }, function (error, statusCode, result) {
    res.statusCode = statusCode;
    res.send(result);
  })
};

exports.delegateDocument = function (req, res) {
  var options = {
    path: 'common/document/delegateDocumentStepSubject',
    query: req.query
  };

  if (req.query.taskServer) {
    options.taskServer = req.query.taskServer;
    delete req.query.taskServer;
  }

  activiti.get(options, function (error, statusCode, result) {
    res.statusCode = statusCode;
    res.send(result);
  })
};

exports.addAcceptor = function (req, res) {
  var options = {
    path: 'common/document/addAcceptor',
    query: req.query,
    headers: {
      'cookie': 'JSESSIONID=' + req.cookies.JSESSIONID
    }
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

exports.addVisor = function (req, res) {
  var options = {
    path: 'common/document/addVisor',
    query: req.query,
    headers: {
      'cookie': 'JSESSIONID=' + req.cookies.JSESSIONID
    }
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

exports.addViewer = function (req, res) {
  var options = {
    path: 'common/document/addViewer',
    query: req.query,
    headers: {
      'cookie': 'JSESSIONID=' + req.cookies.JSESSIONID
    }
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

exports.getDocumentSubmittedUnsigned = function (req, res) {
  activiti.get({
    path: 'common/document/getDocumentSubmitedUnsigned',
    query: req.query
  }, function (error, statusCode, result) {
    if(!error) {
      res.statusCode = statusCode;
      res.send(result)
    }
  })
};


exports.removeDocumentSteps = function (req, res) {
  activiti.get({
    path: 'common/document/removeDocumentSteps',
    query: req.query
  }, function (error, statusCode, result) {
    if(!error) {
      res.statusCode = statusCode;
      res.send(result)
    }
  })
};

exports.cancelDocumentSubmit = function (req, res) {
  var options = {
    path: 'common/document/cancelDocumentSubmit',
    query: req.query
  };

  if (req.query.taskServer){
    options.taskServer = req.query.taskServer;
    delete req.query.taskServer;
  }

  activiti.get(options, function (error, statusCode, result) {
    if(!error) {
      res.statusCode = statusCode;
      res.send(result);
    }
  });
};

exports.removeDocumentStepSubject = function (req, res) {
  var options = {
    path: 'common/document/removeDocumentStepSubject',
    query: req.query,
    headers: {
      'cookie': 'JSESSIONID=' + req.cookies.JSESSIONID
    }
  };

  if (req.query.taskServer){
    options.taskServer = req.query.taskServer;
    delete req.query.taskServer;
  }

  activiti.get(options, function (error, statusCode, result) {
    if(!error) {
      res.statusCode = statusCode;
      res.send(result);
    }
  });
};

exports.setDocumentUrgent = function (req, res) {
  activiti.get({
    path: 'common/document/setDocumentUrgent',
    query: req.query
  }, function (error, statusCode, result) {
    if(!error) {
      res.statusCode = statusCode;
      res.send(result);
    }
  });
};

