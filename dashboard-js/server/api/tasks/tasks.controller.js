'use strict';

var _ = require('lodash');
var activiti = require('../../components/activiti');
var activitiUpload = require('../../components/activiti/upload');
var errors = require('../../components/errors');
var userService = require('../user/user.service');
var authService = require('../../auth/activiti/basic');
var async = require('async');
var tasksService = require('./tasks.service');
var environment = require('../../config/environment');
var request = require('request');
var pdfConversion = require('phantom-html-to-pdf')();
var Buffer = require('buffer').Buffer;

function parseJson(value) {
  try{
    return JSON.parse(value);
  } catch (e){
    return value;
  }
}

/*
 var nodeLocalStorage = require('node-localstorage').LocalStorage;
 var localStorage = new nodeLocalStorage('./scratch');
 */
function createHttpError(error, statusCode) {
  return {httpError: error, httpStatus: statusCode};
}

function step(input, lowerFunction, withoutResult) {
  return withoutResult ? function (callback) {
    lowerFunction(callback, input);
  } : function (result, callback) {
    lowerFunction(result, callback, input);
  };
}

function loadGroups(wfCallback, assigneeID) {
  userService.getGroups(assigneeID, function (error, statusCode, result) {
    if (error) {
      wfCallback(createHttpError(error, statusCode));
    } else {
      wfCallback(null, result.data);
    }
  });
}

function loadUsers(groups, wfCallback) {
  userService.getUserIDsFromGroups(groups, function (error, users) {
    wfCallback(error, users);
  });
}

function loadTasksForOtherUsers(usersIDs, wfCallback, currentUserID) {
  var tasks = [];
  usersIDs = usersIDs
    .filter(function (usersID) {
      return usersID !== currentUserID;
    });

  async.forEach(usersIDs, function (usersID, frCallback) {
    var path = 'action/task/getTasks';

    var options = {
      path: path,
      query: {sLogin: usersID, nSize: 500, sFilterStatus: 'Opened', nStart: 0},
      json: true,
      headers: {
        'cookie': 'JSESSIONID=' + req.cookies.JSESSIONID,
      }
    };

    activiti.get(options, function (error, statusCode, result) {
      if (!error && result.aoTaskDataVO) {
        tasks = tasks.concat(result.aoTaskDataVO);
      }
      frCallback(null);
    });
  }, function (error) {
    wfCallback(error, tasks);
  });
}

function loadAllTasks(tasks, wfCallback, assigneeID) {
  var path = 'action/task/getTasks';
  var options = {
    path: path,
    query: {sLogin: assigneeID, nSize: 500, sFilterStatus: 'Opened', nStart: 0},
    json: true,
    headers: {
      'cookie': 'JSESSIONID=' + req.cookies.JSESSIONID,
    }
  };


  activiti.get(options, function (error, statusCode, result) {
    if (error) {
      wfCallback(error);
    } else {
      result.data = result.aoTaskDataVO.concat(tasks);
      wfCallback(null, result);
    }
  });
}

// Get list of tasks
exports.index = function (req, res) {
  var user = JSON.parse(req.cookies.user);
  //var user = JSON.parse(localStorage.getItem('user'));
  var query = {};
  //https://test.igov.org.ua/wf/service/runtime/tasks?size=20
  if (req.query.soaFilterField) {
    query.soaFilterField = req.query.soaFilterField;
  }
  if (req.query.bIncludeDeleted)
    query.bIncludeDeleted = req.query.bIncludeDeleted;

  query.nSize = 50;

  if (req.query.filterType === 'all') {
    async.waterfall([
      step(user.id, loadGroups, true),
      loadUsers,
      step(user.id, loadTasksForOtherUsers),
      step(user.id, loadAllTasks)
    ], function (error, result) {
      if (error) {
        res.send(error);
      } else {
        res.json(result);
      }
    });
  } else {
    var path = 'action/task/getTasks';

    query.nStart = (req.query.page || 0) * query.nSize;

    if (req.query.filterType === 'control') {
      query.sLogin = user.id;
      query.sSortBy = 'datePlan';
      query.sFilterStatus = 'Control';
      if (req.query.sOrderBy && req.query.sOrderBy === 'true'){
        query.sOrderBy = 'desc';
      } else {
        query.sOrderBy = null;
      }
    } if (req.query.filterType === 'execution') {
      query.sLogin = user.id;
      query.sSortBy = 'datePlan';
      query.sFilterStatus = 'Execution';
      if (req.query.sOrderBy && req.query.sOrderBy === 'true'){
        query.sOrderBy = 'desc';
      } else {
        query.sOrderBy = null;
      }
    } else if (req.query.filterType === 'executed') {
      query.sLogin = user.id;
      query.sSortBy = 'executionTime';
      query.sFilterStatus = 'ExecutionFinished';
      if (req.query.sOrderBy && req.query.sOrderBy === 'true'){
        query.sOrderBy = 'desc';
      } else {
        query.sOrderBy = null;
      }
    }  
    else if (req.query.filterType === 'controled') {
      query.sLogin = user.id;
      query.sSortBy = 'executionTime';
      query.sFilterStatus = 'ControlFinished';
      if (req.query.sOrderBy && req.query.sOrderBy === 'true'){
        query.sOrderBy = 'desc';
      } else {
        query.sOrderBy = null;
      }
    }  
    else if (req.query.filterType === 'selfAssigned') {
      query.sLogin = user.id;
      query.sFilterStatus = 'OpenedAssigned';
      if (req.query.sOrderBy && req.query.sOrderBy === 'true'){
        query.sOrderBy = 'desc'
      } else {
        query.sOrderBy = null;
      }
    } else if (req.query.filterType === 'unassigned') {
      query.sLogin = user.id;
      //query.sSortBy = 'datePlan';
      query.sFilterStatus = 'OpenedUnassigned';
      if (req.query.sOrderBy && req.query.sOrderBy === 'true'){
        query.sOrderBy = 'desc'
      } else {
        query.sOrderBy = null;
      }
    } else if (req.query.filterType === 'finished') {
      query.sLogin = user.id;
      query.sFilterStatus = 'Closed';
      if (req.query.sOrderBy && req.query.sOrderBy === 'true'){
        query.sOrderBy = 'desc'
      } else {
        query.sOrderBy = null;
      }
    } else if (req.query.filterType === 'documents') {
      query.sFilterStatus = 'DocumentOpenedUnassignedUnprocessed';
      query.sLogin = user.id;
      query.nSize = 30;
      if (req.query.sOrderBy && req.query.sOrderBy === 'true'){
        query.sOrderBy = 'desc'
      } else {
        query.sOrderBy = null;
      }
    } else if (['agree', 'accept', 'seen', 'execute', 'direct', 'watch'].indexOf(req.query.filterType) > -1) {
      query.sFilterStatus = tasksService.getSubfolder(req.query.filterType);
      query.sLogin = user.id;
      query.nSize = 30;
      if (req.query.sOrderBy && req.query.sOrderBy === 'true'){
        query.sOrderBy = 'desc'
      } else {
        query.sOrderBy = null;
      }
    } else if (req.query.filterType === 'ecp') {
      query.sFilterStatus = 'DocumentOpenedUnassignedWithoutECP';
      query.sLogin = user.id;
      query.bIncludeVariablesProcess = true;
      query.nSize = 15;
      if (req.query.sOrderBy && req.query.sOrderBy === 'true'){
        query.sOrderBy = 'desc'
      } else {
        query.sOrderBy = null;
      }
    } else if (req.query.filterType === 'viewed') {
      query.sFilterStatus = 'DocumentOpenedUnassignedProcessed';
      query.sLogin = user.id;
      query.nSize = 50;
      if (req.query.sOrderBy && req.query.sOrderBy === 'true' || !req.query.sOrderBy){
        query.sOrderBy = 'desc'
      }
    } else if (req.query.filterType === 'myDocuments') {
      query.sFilterStatus = 'OpenedCreatorDocument';
      query.sLogin = user.id;
      query.nSize = 50;
      if (req.query.sOrderBy && req.query.sOrderBy === 'true'){
        query.sOrderBy = 'desc'
      } else {
        query.sOrderBy = null;
      }
    } else if (req.query.filterType === 'docHistory') {
      query.sFilterStatus = 'DocumentClosed';
      query.sLogin = user.id;
      query.nSize = 50;
      if (req.query.sOrderBy && req.query.sOrderBy === 'true'){
        query.sOrderBy = 'desc'
      } else if (req.query.sOrderBy && req.query.sOrderBy === 'false'){
        query.sOrderBy = null;
      } else {
        query.sOrderBy = 'desc'
      }
    } else if (req.query.filterType === 'tickets') {
      path = 'action/flow/getFlowSlotTickets';
      query.sLogin = user.id;
      query.bEmployeeUnassigned = req.query.bEmployeeUnassigned;
      if (req.query.sDate) {
        query.sDate = req.query.sDate;
      }
    }

    query.nStart = (req.query.page || 0) * query.nSize;
    if (req.cookies.referent){
      if (JSON.parse(req.cookies.referent)){
        query.sLoginReferent = JSON.parse(req.cookies.referent).id;
      }
    }

    var options = {
      path: path,
      query: query,
      json: true,
      headers: {
        'cookie': 'JSESSIONID=' + req.cookies.JSESSIONID,
      }
    };

    activiti.get(options, function (error, statusCode, result) {
      if (error) {
        res.send(error);
      } else {
        // if (req.query.filterType === 'tickets') {
        //   result = {data: result};
        // }
        res.json(result);
      }
    });
  }
};

// Get list of task events
exports.getAllTaskEvents = function (req, res) {
  var options = {
    path: '/runtime/tasks/' + req.params.taskId + '/events'
  };

  activiti.get(options, function (error, statusCode, result) {
    if (error) {
      res.send(error);
    } else {
      res.json(result);
    }
  });

};

exports.getSubFolders = function (req, res) {
  var options = {
    path: 'action/task/getSubTabs'
  };

  activiti.get(options, function (error, response, body) {
    if (!error) {
      res.send(body);
    } else {
      res.send(error);
    }
  })
};

exports.getForm = function (req, res) {
  var options = {
    path: 'form/form-data',
    query: {
      'taskId': req.params.taskId
    }
  };

  if (req.query.taskServer) {
    options.taskServer = req.query.taskServer;
    delete req.query.taskServer;
  }

  res.setHeader('Content-Type', 'application/json;charset=utf-8');

  activiti.get(options, function (error, statusCode, result) {
    if (error) {
      res.send(error);
    } else {
      res.status(statusCode).send(result);
    }
  });
};

exports.getFormFromHistory = function (req, res) {
  var options = {
    path: 'history/historic-task-instances',
    query: {
      'taskId': req.params.taskId
    }

  };

  activiti.get(options, function (error, statusCode, result) {
    if (error) {
      res.send(error);
    } else {
      res.status(statusCode).json(result);
    }
  });
};

exports.uploadFile = function (req, res) {
  var options = {
    url: activiti.getRequestURL({
      path: 'object/file/upload_file_as_attachment',
      query: {
        taskId: req.params.taskId,
        description: req.query.description,
        sID_Field: req.params.field
      }
    })
  };

  if (req.query.taskServer) {
    options.taskServer = req.query.taskServer;
    delete req.query.taskServer;
  }

  activiti.fileupload(req, res, options);
};

exports.getAttachments = function (req, res) {
  var options = {
    path: 'runtime/tasks/' + req.params.taskId + '/attachments'
  };

  activiti.get(options, function (error, statusCode, result) {
    if (error) {
      res.send(error);
    } else {
      res.status(statusCode).json(result);
    }
  });
};
exports.saveChangesTask = function (req, res) {
  var options = {
    path: '/action/task/saveForm',
    query: {
      sParams: req.body
    }
  };
  if (req.query.taskServer){
    options.taskServer = req.query.taskServer;
    delete req.query.taskServer;
  }

  activiti.post(options, function (error, statusCode, result) {
    if (error) {
      res.send(error);
    } else {
      res.status(statusCode).json(result);
    }
  }, req.body);
};

exports.getOrderMessages = function (req, res) {
  var options = {
    path: 'action/task/getOrderMessages_Local',
    query: {
      'nID_Process': req.params.nID_Process
    }
  };

  activiti.get(options, function (error, statusCode, result) {
    if (error) {
      console.log("[getOrderMessages]:error=" + error);
      res.status(200).json("[]");
      //res.send(error);
    } else {
      console.log("[getOrderMessages]:result=" + result);
      if (statusCode !== 200) {
        res.status(200).json("[]");
      } else {
        res.status(statusCode).json(result);
      }
    }
  });
};

exports.getAttachmentContent = function (req, res) {
  var options = {
    path: 'object/file/download_file_from_db',
    query: {
      'taskId': req.params.taskId,
      'nFile': req.params.nFile
    }
  };
  activiti.filedownload(req, res, options);
};

exports.getAttachmentFile = function (req, res) {
  var qs = {};

  if (['Mongo', 'Redis'].indexOf(req.params.typeOrAttachID) !== -1) {
    qs = {
      'sKey': req.params.keyOrProcessID,
      'sID_StorageType': req.params.typeOrAttachID
    };
    if (req.params.sFileName) {
      qs['sFileName'] = req.params.sFileName;
    }
  } else {
    qs = {
      'nID_Process': req.params.keyOrProcessID,
      'sID_Field': req.params.typeOrAttachID
    }
  }
  var options = {
    query: qs
  };

  if (req.query.taskServer) {
    options.taskServer = req.query.taskServer;
    delete req.query.taskServer;
  } else if (req.params.taskServer) {
    options.taskServer = req.params.taskServer;
    delete req.params.taskServer;
  }

  if(!req.query || (req.query && !req.query.bAsBase64)){
    options.path = 'object/file/getProcessAttach';
    activiti.filedownload(req, res, options);
  } else {
    options.path = 'object/file/getProcessAttachAsBase64';
    activiti.get(options, function (error, statusCode, result) {
      if (error) {
        res.send(error);
      } else {
        res.status(statusCode).json(result);
      }
    });
  }

};

exports.showAttachmentFile = function (req, res) {
  var qs = {};

  if (req.params.typeOrAttachID === 'Mongo') {
    qs = {
      'sKey': req.params.keyOrProcessID,
      'sID_StorageType': req.params.typeOrAttachID
    };
    if (req.params.sFileName) {
      qs['sFileName'] = req.params.sFileName;
    }
  } else {
    qs = {
      'nID_Process': req.params.keyOrProcessID,
      'sID_Field': req.params.typeOrAttachID
    }
  }

  if (req.query.taskServer) {
    qs.taskServer = req.query.taskServer;
    delete req.query.taskServer;
  } else if (req.params.taskServer) {
    qs.taskServer = req.params.taskServer;
    delete req.params.taskServer;
  }

  var options = {
    query: qs
  };

  if(!req.query || (req.query && !req.query.bAsBase64)){
    options.path = 'object/file/getProcessAttach';
    activiti.typedfileshow(req, res, options);
  } else {
    options.path = 'object/file/getProcessAttachAsBase64';
    activiti.get(options, function (error, statusCode, result) {
      if (error) {
        res.send(error);
      } else {
        res.status(statusCode).json(result);
      }
    });
  }
}

exports.getDocumentImage = function (req, res) {
  var user = JSON.parse(req.cookies.user);
  var qs = {
    'nID_Process': req.params.keyOrProcessID,
    'sLogin': user.id,
    'sKey_Step': req.params.sKey_Step
  };

  var options = {
    query: qs
  };

  if(!req.query || (req.query && !req.query.bAsBase64)){
    options.path = 'object/file/getDocumentImage';
    activiti.filedownload(req, res, options);
  } else {
    options.path = 'object/file/getDocumentImageAsBase64';
    activiti.get(options, function (error, statusCode, result) {
      if (error) {
        res.send(error);
      } else {
        res.status(statusCode).json(result);
      }
    });
  }
};

exports.getDocumentImagesAsBase64 = function (req, res) {
  var user = JSON.parse(req.cookies.user);
  var aDownloadedDocuments = [];

  var options = {
    path: 'object/file/getDocumentImageAsBase64',
    query: {
      'sLogin': user.id
    }
  };

  var getDocumetAsBase64 = function (sParams, callback) {
    var oPraram = parseJson(sParams);
    options.query['nID_Process'] = oPraram.processInstanceId;
    options.query['sKey_Step'] = oPraram.sKey_Step;

    activiti.get(options, function (error, statusCode, result) {
      if (error) {
        aDownloadedDocuments.push({
          'oParam': oPraram,
          'error': parseJson(error)
        });

      } else {
        aDownloadedDocuments.push({
          'oParam': oPraram,
          'oDocument': parseJson(result)
        });
      }
      callback();
    });

  };
  var aParams = [];

  if(_.isArray(req.query.aArrayOfParams)){
    aParams = req.query.aArrayOfParams;
  } else {
    aParams.push(req.query.aArrayOfParams);
  }

  async.forEach(aParams, function (sParams, callback) {
    getDocumetAsBase64(sParams, callback);
  }, function (error) {
    if (error) {
      res.status(500).send(error);
    } else {
      res.send(aDownloadedDocuments);
    }
  });
};

exports.getAttachmentsAsBase64 = function (req, res) {
  var aDownloadedAttachmens = [];

  var options = {
    path: 'object/file/getProcessAttachAsBase64'
  };

  var getAttachmentAsBase64 = function (sParams, callback) {
    var oPraram = parseJson(sParams);
    options.query = {};

    if (oPraram.sID_StorageType === 'Mongo') {
      options.query = {
        'sKey': oPraram.sKey,
        'sID_StorageType': oPraram.sID_StorageType
      };
      if (oPraram.sFileNameAndExt) {
        options.query['sFileName'] = oPraram.sFileNameAndExt;
      }
    } else {
      options.query = {
        'nID_Process': oPraram.nID_Process,
        'sID_Field': oPraram.sID_Field
      }
    }

    activiti.get(options, function (error, statusCode, result) {
      if (error) {
        aDownloadedAttachmens.push({
          'oParam': oPraram,
          'error': parseJson(result)
        });
      } else {
        aDownloadedAttachmens.push({
          'oParam': oPraram,
          'oAttachment': parseJson(result)
        });
      }
      callback();
    });

  };

  async.forEach(req.query.aArrayOfParams, function (sParams, callback) {
    getAttachmentAsBase64(sParams, callback);
  }, function (error) {
    if (error) {
      res.status(500).send(error);
    } else {
      res.send(aDownloadedAttachmens);
    }
  });
};

exports.getAttachmentContentTable = function (req, res) {
  var options = {
    path: 'object/file/download_file_from_db',
    query: {
      'taskId': req.params.taskId,
      'attachmentId': req.params.attachmentId
    }
  };
  activiti.get(options, function (error, statusCode, result) {
    if (error) {
      res.send(error);
    } else if (statusCode == 500) {
      console.log(statusCode, "isn't table attachment");
    } else {
      res.status(statusCode).json(result);
    }
  });
};

exports.submitForm = function (req, res) {
  var user = JSON.parse(req.cookies.user);
  var referent;
  var options = {
    path: 'action/task/updateProcess',
    query: {
      bSaveOnly: false,
      sLogin: user.id
    },
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
      'cookie': 'JSESSIONID=' + req.cookies.JSESSIONID
    }
  };
  if (req.cookies.referent) {
    referent = JSON.parse(req.cookies.referent);
    options.query.sLoginReferent = referent.id;
  }
  if (req.query.sName_DocumentStepSubjectSignType){
    options.query.sName_DocumentStepSubjectSignType = req.query.sName_DocumentStepSubjectSignType;
  }
  if (req.query.taskServer) {
    options.taskServer = req.query.taskServer;
    delete req.query.taskServer;
  }
  if(req.query.needAssign) {
    options.query.sLoginAssigne = req.query.needAssign;
  }
  activiti.post(options, function (error, statusCode, result) {
    res.statusCode = statusCode;
    res.send(result);
  }, req.body);
};

exports.updateTask = function (req, res) {
  var options = {
    path: 'runtime/tasks/' + req.params.taskId
  };
  if (req.query.taskServer) {
    options.taskServer = req.query.taskServer;
    delete req.query.taskServer;
  }
  activiti.put(options, function (error, statusCode, result) {
    res.statusCode = statusCode;
    res.send(result);
  }, req.body);
};

exports.getTask = function (req, res) {
  var options = {
    path: 'runtime/tasks/' + req.params.taskId
  };
  //activiti.put(options, function (error, statusCode, result) {
  activiti.get(options, function (error, statusCode, result) {
    res.statusCode = statusCode;
    if (typeof(result) === 'number') {
      result = '' + result;
    }
    res.send(result);
  }, req.body);
};

exports.getTasksByOrder = function (req, res) {
  var options = {
    path: 'action/task/getTasksByOrder',
    query: {'nID_Order': req.params.orderId}
    //query: {'nID_Process': req.params.nID_Process}
  };
  if (req.query.taskServer) {
    options.taskServer = req.query.taskServer;
    delete req.query.taskServer;
  }
  activiti.get(options, function (error, statusCode, result) {
    error ? res.send(error) : res.status(statusCode).json(result);
  });
};

exports.getTasksByText = function (req, res) {
  var user = JSON.parse(req.cookies.user);
  //var user = JSON.parse(localStorage.getItem('user'));
  //query.bEmployeeUnassigned = req.query.bEmployeeUnassigned;
  var options = {
    path: 'action/task/getTasksByText',
    query: {
      'sFind': req.params.text,
      'sLogin': user.id,//finished,unassigned, selfAssigned
      'bAssigned': req.params.sType === 'selfAssigned' ? true : req.params.sType === 'unassigned' ? false : null, //bAssigned
      'bSortByStartDate': true
    }
  };
  activiti.get(options, function (error, statusCode, result) {
    error ? res.send(error) : res.status(statusCode).json(result);
    //error ? res.send(error) : res.status(statusCode).json("[\"4585243\"]");
  });
};

exports.getProcesses = function (req, res) {
  var options = {
    path: 'analytic/process/getProcesses',
    query: {
      'sID_': req.query.sID
    }
  };
  activiti.get(options, function (error, statusCode, result) {
    error ? res.send(error) : res.status(statusCode).json(result);
  });
};

exports.getFile = function (req, res) {
  var user = JSON.parse(req.cookies.user);
  var options = {
    path: 'analytic/process/getFile',
    query: {
      'sLogin': user.id,
      'nID_Attribute_File': req.params.nFile
    }
  };
  activiti.filedownload(req, res, options);
};

exports.getPatternFile = function (req, res) {
  var options = {
    path: 'object/file/getPatternFile',
    query: {
      'sPathFile': req.query.sPathFile.split(',')[0]
    }
  };

  options.query.sPathFile = options.query.sPathFile.replace(/^sPrintFormFileAsPDF=pattern\/|^sPrintFormFileAsIs=pattern\//, '');
  activiti.filedownload(req, res, options);
};

exports.signAndUpload = function (req, res) {
  var user = JSON.parse(req.cookies.user);
  var contentAndSignContainer = req.body.sign;
  var params = {
    qs: {
      nID_Process: req.body.taskId,
      sFileNameAndExt: req.body.sFileNameAndExt,
      sID_Field: req.body.sID_Field,
      sKey_Step: req.body.sKey_Step,
      sLogin: user.id
    },
    headers: {
      'Content-Type': 'application/pdf;charset=utf-8'
    }
  };

  if (req.query.taskServer) {
    params.taskServer = req.query.taskServer;
    delete req.query.taskServer;
  }

  var content = {buffer: new Buffer(new Buffer(contentAndSignContainer, 'base64').toString('binary'), 'binary')};

  activitiUpload.uploadContent('object/file/setDocumentImage', params, content, function (error, response, body) {
    error ? res.send(error) : res.status(response.statusCode).json(result);
  });
};

exports.pluralUploadingOfSignedContents = function (req, res) {
  var user = JSON.parse(req.cookies.user);
  var serverResponces = [];

  function upload(oContent, callback) {
    var sPath = '';
    var params = {};
    if(oContent.sType === 'TasksAttachment'){
      sPath = 'object/file/setProcessAttach';
      params = {
        qs: {
          nID_Process: oContent.oParam.nID_Process,
          bSigned: true,
          sID_StorageType: oContent.oParam.sID_StorageType,
          aAttribute: oContent.oParam.aAttribute,
          sContentType: getContentTypeByFileName(oContent.oParam.sFileNameAndExt),
          sFileNameAndExt: oContent.oParam.sFileNameAndExt,
          sID_Field: oContent.oParam.sID_Field
        },
        headers: {
          'Content-Type': getContentTypeByFileName(oContent.oParam.sFileNameAndExt) + ';charset=utf-8'
        }
      };
    } else if (oContent.sType === 'DocumentImage'){
      sPath = 'object/file/setDocumentImage';
      params = {
        qs: {
          nID_Process: oContent.oParam.nID_Process,
          sKey_Step: oContent.oParam.sKey_Step,
          sLogin: user.id
        },
        headers: {
          'Content-Type': 'application/pdf;charset=utf-8'
        }
      };
    } else {
      callback('Undefined type of content');
    }

    var content = {buffer: new Buffer(new Buffer(oContent.sign, 'base64').toString('binary'), 'binary')};

    activitiUpload.uploadContent(sPath, params, content, function (error, response, body) {

      if (error) {
        serverResponces.push({
          'oContent': oContent,
          'error': parseJson(result)
        });
      } else {
        serverResponces.push({
          'oContent': oContent,
          'status': response.statusCode,
          'result': parseJson(result)
        });
      }
      callback();
    });
  }

  async.forEach(req.body, function (oContent, callback) {
    upload(oContent, callback);
  }, function (error) {
    if (error) {
      res.status(500).send(error);
    } else {
      res.send(serverResponces);
    }
  });
};

function getContentTypeByFileName(sFileNameAndExt, defaultType) {
  var ext = sFileNameAndExt.split('.').pop().toLowerCase();
  switch (ext){
    case "pdf": return 'application/pdf';
    case "html": return 'text/html';
    case "bmp": return 'image/bmp';
    case "gif": return 'image/gif';
    case "jpeg": return 'image/jpeg';
    case "jpg": return 'image/jpeg';
    case "png": return 'image/png';
    case "tif": return 'image/tiff';
    case "doc": return 'application/msword';
    case "docx": return 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
    case "odt": return 'application/vnd.oasis.opendocument.text';
    case "rtf": return 'application/rtf';
    case "xls": return 'application/excel';
    case "xlsx": return 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
    case "xlsm": return 'application/vnd.ms-excel.sheet.macroEnabled.12';
    case "xml": return 'text/xml';
    case "ods": return 'application/vnd.oasis.opendocument.spreadsheet';
    case "sxc": return 'application/vnd.sun.xml.calc';
    case "wks": return 'application/vnd.ms-works';
    case "csv": return 'text/csv';
    case "zip": return 'application/zip';
    case "rar": return 'application/x-rar-compressed';
    case "7z": return 'application/x-7z-compressed';
    case "p7s": return 'application/x-pkcs7-signature';
    default: return defaultType ? defaultType : 'application/octet-stream';
  }
}

/**
 * https://github.com/e-government-ua/i/issues/1382
 * added pdf conversion if file name is sPrintFormFileAsPDF
 */
exports.upload_content_as_attachment = function (req, res) {
  if (req.body.sOutputFileType === 'pdf') {
    async.waterfall([
      function (callback) {
        var options = {
          html: req.body.sContent,
          allowLocalFilesAccess: true,
          paperSize: {
            format: 'A4', orientation: 'portrait'
          },
          fitToPage: true,
          customHeaders: [],
          settings: {
            javascriptEnabled: true
          },
          format: {
            quality: 100
          }
        };
        if (req.body.isSendAsDocument) {
          req.body.url = "setDocumentImage";
        } else {
          req.body.url = 'setProcessAttach';
        }
        pdfConversion(options, function (err, pdf) {
          callback(err, {content: pdf.stream, contentType: 'application/pdf'});
        });
      },
      function (data, callback) {
        if (req.body.url === 'setProcessAttach') {
          var options = {
            path: 'object/file/' + req.body.url,
            nID_Process: req.params.taskId,
            stream: data.content,
            sFileNameAndExt: req.body.sFileNameAndExt,
            sID_Field: req.body.sID_Field,
            sID_StorageType: req.body.sID_StorageType,
            headers: {
              'Content-Type': getContentTypeByFileName(req.body.sFileNameAndExt, data.contentType) + ';charset=utf-8'
            }
          };

          if (req.body.taskServer) {
            options = req.body.taskServer;
            delete req.body.taskServer;
          }

          activiti.uploadStream(options, function (error, statusCode, result) {
            pdfConversion.kill();
            error ? res.send(error) : res.status(statusCode).json(result);
          });
        } else if (req.body.url === "setDocumentImage") {
          var user = JSON.parse(req.cookies.user);
          var options = {
            path: 'object/file/' + req.body.url,
            nID_Process: req.params.taskId,
            stream: data.content,
            sFileNameAndExt: req.body.sFileNameAndExt,
            sID_Field: req.body.sID_Field,
            sKey_Step: req.body.sKey_Step,
            sLogin: user.id,
            headers: {
              'Content-Type': getContentTypeByFileName(req.body.sFileNameAndExt, data.contentType) + ';charset=utf-8'
            }
          };

          if (req.body.taskServer) {
            options.taskServer = req.body.taskServer;
            delete req.body.taskServer;
          }

          activiti.uploadStream(options, function (error, statusCode, result) {
            pdfConversion.kill();
            error ? res.send(error) : res.status(statusCode).json(result);
          });
        }
      }
    ]);
  } else {
    activiti.post({
      path: 'object/file/setProcessAttachText',
      query: {
        nID_Process: req.params.taskId,
        sFileNameAndExt: req.body.sFileNameAndExt,
        sID_Field: req.body.sID_Field
      },
      headers: {
        'Content-Type': getContentTypeByFileName(req.body.sFileNameAndExt, 'text/html') +';charset=utf-8'
      }
    }, function (error, statusCode, result) {
      error ? res.send(error) : res.status(statusCode).json(result);
    }, req.body.sContent, false);
  }

};

exports.setDocumentImage = function (req, res) {

  var user = JSON.parse(req.cookies.user);
  var contentAndSignContainer = req.body.sContent;
  var params = {
    qs: {
      nID_Process: req.params.taskId,
      bSigned: req.body.bSigned,
      sFileNameAndExt: req.body.sFileNameAndExt.replace(new RegExp(/[*|\\:"<>?/]/g), ""),
      sID_Field: req.body.sID_Field,
      sKey_Step: req.body.sKey_Step,
      sLogin: user.id,
      sContentType: req.body.sContentType
    },
    headers: {
      'Content-Type': getContentTypeByFileName(req.body.sFileNameAndExt, 'application/pdf') + ';charset=utf-8'
    }
  };

  if (req.body.taskServer) {
    params.taskServer = req.body.taskServer;
    delete req.body.taskServer;
  }

  var content = {buffer: new Buffer(new Buffer(contentAndSignContainer, 'base64').toString('binary'), 'binary')};

  activitiUpload.uploadContent('object/file/setDocumentImage', params, content, function (error, response, body) {
    error ? res.send(error) : res.status(response.statusCode).json(body);
  });

};

exports.setTaskQuestions = function (req, res) {
  var query = {
    nID_Process: req.body.nID_Process,
    sMail: req.body.sMail,
    sHead: req.body.sHead,
    sSubjectInfo: req.body.sSubjectInfo,
    nID_Subject: req.body.nID_Subject
  };

  var data = {
    saField: req.body.saField,
    soParams: req.body.soParams,
    sBody: req.body.sBody
  };

  activiti.post({
    path: 'action/task/setTaskQuestions',
    query: query,
    headers: {
      'Content-Type': 'text/html;charset=utf-8'
    }
  }, function (error, statusCode, result) {
    error ? res.send(error) : res.status(statusCode).json(result);
  }, data);
};

// отправка комментария от чиновника, сервис работает на централе, поэтому с env конфигов берем урл.
exports.postServiceMessage = function (req, res) {
  var oData = req.body;
  var oDateNew = {
    'sID_Order': environment.activiti.nID_Server + '-' + oData.nID_Process,
    'sBody': oData.sBody,
    'nID_SubjectMessageType': 9,
    'sMail': oData.sMail,
    'soParams': oData.soParams
  };
  var central = environment.activiti_central;
  var sURL = central.prot + '://' + central.host + ':' + central.port + '/' + central.rest + '/subject/message/setServiceMessage';
  var callback = function (error, response, body) {
    res.send(body);
    res.end();
  };
  return request.post({
    'url': sURL,
    'auth': {
      'username': central.username,
      'password': central.password
    },
    'qs': oDateNew
  }, callback);
};

exports.checkAttachmentSign = function (req, res) {
  var nID_Task = req.params.taskId;
  var nID_Attach = req.params.attachmentId;

  if (!nID_Task) {
    res.status(400).send(errors.createError(errors.codes.INPUT_PARAMETER_ERROR, 'nID_Task should be specified'));
    return;
  }

  if (!nID_Attach) {
    res.status(400).send(errors.createError(errors.codes.INPUT_PARAMETER_ERROR, 'nID_Attach should be specified'));
    return;
  }

  var options = {
    path: 'object/file/check_attachment_sign',
    query: {
      nID_Task: nID_Task,
      nID_Attach: nID_Attach
    },
    json: true
  };

  if (req.query.taskServer) {
    options.taskServer = req.query.taskServer;
    delete req.query.taskServer;
  }

  activiti.get(options, function (error, statusCode, body) {
    if (error) {
      error = errors.createError(errors.codes.EXTERNAL_SERVICE_ERROR, 'Error while checking file\'s sign', error);
      res.status(500).send(error);
      return;
    }

    res.status(200).send(body);
  });
};

exports.unassign = function (req, res) {
  var nID_Task = req.params.taskId;
  if (!nID_Task) {
    res.status(400).send(errors.createError(errors.codes.INPUT_PARAMETER_ERROR, 'nID_Task should be specified'));
    return;
  }

  var options = {
    path: 'action/task/resetUserTaskAssign',
    query: {
      nID_UserTask: nID_Task
    },
    json: true
  };

  activiti.post(options, function (error, statusCode, result) {
    error ? res.send(errors.createError(errors.codes.EXTERNAL_SERVICE_ERROR, 'Can\'t unassign', error))
      : res.status(statusCode).json(result);
  });
};

/*
 exports.getTaskData = function(req, res) {
 var options = {
 path: 'action/task/getTaskData',
 query: req.query,
 json: true
 };

 activiti.get(options, function (error, statusCode, body) {
 if (error) {
 error = errors.createError(errors.codes.EXTERNAL_SERVICE_ERROR, 'Error while loading task data', error);
 res.status(500).send(error);
 return;
 }
 var currentUser = JSON.parse(req.cookies.user);
 var UserFromStorage = JSON.parse(localStorage.getItem('user'));
 currentUser.roles = UserFromStorage.roles;
 // После запуска существует вероятность, что объекта req.session еще не ссуществует и чтобы не вывалилась ошибка
 // пропускаем проверку. todo: При следующем релизе нужно удалить условие !req.session
 //var cashedGr = authService.getCashedUserGroups(currentUser);

 if (!req.session || tasksService.isTaskDataAllowedForUser(body, req.session.roles ? req.session : currentUser))
 res.status(200).send(body);
 else {
 error = errors.createError(errors.codes.FORBIDDEN_ERROR, 'Немає доступу до цієї задачі.');
 res.status(403).send(error);
 }
 });
 };
 */
exports.getTaskData = function (req, res) {
  var referent;
  var options = {
    path: 'action/task/getTaskData',
    query: req.query,
    json: true,
    headers: {
      'cookie': 'JSESSIONID=' + req.cookies.JSESSIONID,
    }
  };

  if (req.cookies.referent) {
    referent = JSON.parse(req.cookies.referent);
    options.query.sLoginReferent = referent.id;
  }

  if (options.query.taskServer) {
    options.taskServer = options.query.taskServer;
    delete options.query.taskServer;
  }

  var currentUser = JSON.parse(req.cookies.user);
  options.query.sLogin = currentUser.id;

  var userRoles = authService.getCashedUserGroups(currentUser);
  if (userRoles) {
    currentUser.roles = userRoles;
    activiti.get(options, function (error, statusCode, body) {
      if (error) {
        error = errors.createError(errors.codes.EXTERNAL_SERVICE_ERROR, 'Error while loading task data', error);
        res.status(500).send(error);
        return;
      }

      if (!req.session || tasksService.isTaskDataAllowedForUser(body, currentUser))
        res.status(200).send(body);
      else {
        error = errors.createError(errors.codes.FORBIDDEN_ERROR, 'Немає доступу до цієї задачі.');
        res.status(403).send(error);
      }
    });
  } else {
    async.waterfall([
      function (callback) {
        activiti.get({
          path: 'action/identity/getGroups',
          query: {
            sLogin: currentUser.id
          },
          json: true
        }, function (error, statusCode, result) {
          if (error) {
            callback(error, null);
          } else {
            var resultGroups;
            if ((typeof result == "object") && (result instanceof Array)) {
              currentUser['roles'] = result.map(function (group) {
                return group.id;
              });
            } else {
              currentUser['roles'] = [];
            }
            callback(null, {
              currentUser: currentUser
            });
          }
        });
      },
      function (user, callback) {
        activiti.get(options, function (error, statusCode, body) {
          callback(error, body);
        });
      }
    ], function (error, body) {
      if (error) {
        error = errors.createError(errors.codes.EXTERNAL_SERVICE_ERROR, 'Error while loading task data', error);
        res.status(500).send(error);
        return;
      }
      authService.setCashedUserGroups(currentUser, currentUser.roles);
      if (!req.session || tasksService.isTaskDataAllowedForUser(body, currentUser))
        res.status(200).send(body);
      else {
        error = errors.createError(errors.codes.FORBIDDEN_ERROR, 'Немає доступу до цієї задачі.');
        res.status(403).send(error);
      }
    })
  }
};

exports.getMessageFile = function (req, res) {
  var options = {
    path: 'action/task/getMessageFile_Local',
    query: {
      //nID_Process: req.params.taskId,
      nID_Message: req.params.messageId
    },
    json: true
  };
  activiti.filedownload(req, res, options);
};

exports.setTaskAttachment = function (req, res) {
  async.waterfall([
    function (callback) {
      callback(null, {content: req.body.sContent, contentType: 'text/html', url: 'setTaskAttachment'});
    },
    function (data, callback) {
      if (data.url === 'setTaskAttachment') {
        var options = {
          path: 'object/file/' + data.url,
          query: {
            nTaskId: req.params.taskId,
            sContentType: getContentTypeByFileName(req.body.sFileName, data.contentType),
            sDescription: req.body.sDescription,
            sFileName: req.body.sFileName,
            nID_Attach: req.body.nID_Attach
          },
          headers: {
            'Content-Type': data.contentType + ';charset=utf-8'
          }
        };

        if (req.body.taskServer)
          options.taskServer = req.body.taskServer;

        activiti.post(options, function (error, statusCode, result) {
          error ? res.send(error) : res.status(statusCode).json(result);
        }, data.content, false);
      }
    }
  ]);
};

exports.setTaskAttachmentNew = function (req, res) {
  var query = {
    sFileNameAndExt: req.body.sFileNameAndExt,
    sID_Field: req.body.nID_Attach
  };

  if (req.body.nID_Process) {
    query['nID_Process'] = req.body.nID_Process;
  }

  var options = {
    path: 'object/file/setProcessAttachText',
    query: query,
    headers: {
      'Content-Type': getContentTypeByFileName(req.body.sFileNameAndExt, 'text/html') + ';charset=utf-8'
    }
  };

  if (req.body.taskServer) {
    options.taskServer = req.body.taskServer;
    delete req.body.taskServer;
  }

  activiti.post(options, function (error, statusCode, result) {
    error ? res.send(error) : res.status(statusCode).json(result);
  }, req.body.sContent, false);

};


exports.checkAttachmentSignNew = function (req, res) {
  var properties = {
    sKey: req.query.sKey,
    sID_StorageType: req.query.sID_StorageType || null,
    sID_Process: req.query.sID_Process || null,
    sID_Field: req.query.sID_Field || null,
    sFileNameAndExt: req.query.sFileNameAndExt || null
  };

  for (var key in properties) {
    if (!properties[key]) {
      delete properties[key];
    }
  }

  var options = {
    path: 'object/file/checkProcessAttach',
    query: properties,
    json: true
  };

  if (req.query.taskServer) {
    options.taskServer = req.query.taskServer;
    delete req.query.taskServer;
  }

  activiti.get(options, function (error, statusCode, body) {
    if (error) {
      error = errors.createError(errors.codes.EXTERNAL_SERVICE_ERROR, 'Error while checking file\'s sign', error);
      res.status(500).send(error);
      return;
    }

    res.status(200).send(body);
  });
};

module.exports.uploadFileHTML = function (req, res) {
  var options = {
    path: 'object/file/setProcessAttachText',
    query: {
      sFileNameAndExt: req.body.sFileNameAndExt
    },
    headers: {
      'Content-Type': 'text/html;charset=utf-8'
    }
  };

  if (req.query.taskServer) {
    options.taskServer = req.query.taskServer;
    delete req.query.taskServer;
  } else if (req.body.taskServer) {
    options.taskServer = req.body.taskServer;
    delete req.body.taskServer;
  }

  activiti.post(options, function (error, statusCode, result) {
    error ? res.send(error) : res.status(statusCode).json(result);
  }, req.body.sContent, false);

};

module.exports.getTabByOrder = function (req, res) {
  var currentUser = JSON.parse(req.cookies.user);
  var options = {
    path: 'action/task/getTaskTab',
    query: {
      sID_Order: req.query.order,
      sID_Group_Activiti: currentUser.id
    }
  };

  if (req.query.taskServer) {
    options.taskServer = req.query.taskServer;
    delete req.query.taskServer;
  }

  activiti.get(options, function (error, status, result) {
    if (!error) {
      res.status(status).send(result);
    } else {
      res.send(error);
    }
  });
};

exports.searchTasks = function (req, res) {
  var options = {
    path: 'action/task/searchTasks',
    query: {
      nSize: 500,
      nStart: 0,
      sOrderBy: 'asc'
    },
    json: true,
  };

  activiti.post(options, function (error, statusCode, result) {
    res.send(result);
  }, req.body);
};

exports.cancelTask = function (req, res) {
  var options = {
    path: 'action/task/cancelTask',
    query: req.query,
    headers: {
      'cookie': 'JSESSIONID=' + req.cookies.JSESSIONID
    }
  };

  activiti.get(options, function (error, status, result) {
    if (!error) {
      res.status(status).send(result);
    }
  });
};
