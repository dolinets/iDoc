'use strict';

var _ = require('lodash');
var activiti = require('../../components/activiti');
var errors = require('../../components/errors');
var environment = require('../../config/environment');
var async = require('async');
var request = require('request');


module.exports.checkIsAdmin = function (req, res) {
  var callback = function (error, response, body) {
    res.send(body);
    res.end();
  };

  var updatedQuery = req.query;
  var options = {
    path: 'subject/isAdmin',
    query: updatedQuery,
    json: true
  };
  activiti.get(options, callback)
};

module.exports.getSubjectHumanPositionCustom = function (req, res) {
    var callback = function (error, response, body) {
        res.send(body);
        res.end();
    };

    var updatedQuery = req.query;
    var options = {
        path: 'subject/getSubjectHumanPositionCustom',
        query: updatedQuery,
        json: true,
        headers: {
          'cookie': 'JSESSIONID=' + req.cookies.JSESSIONID
        }
    };
    activiti.get(options, callback)
};
module.exports.setSubjectHumanPositionCustom = function (req, res) {
    var callback = function (error, response, body) {
        res.send(body);
        res.end();
    };

    var updatedQuery = req.query;
    var options = {
        path: 'subject/setSubjectHumanPositionCustom',
        query: updatedQuery,
        json: true,
        headers: {
          'cookie': 'JSESSIONID=' + req.cookies.JSESSIONID
        }
    };
    activiti.get(options, callback)
};
module.exports.editSubjectHuman = function (req, res) {
    var callback = function (error, response, body) {
        res.send(body);
        res.end();
    };

    var updatedQuery = req.query;
    var options = {
        path: 'subject/setSubjectHuman',
        query: updatedQuery,
        json: true
    };
    activiti.get(options, callback)
};


module.exports.getStaffContact = function (req, res) {

    var callback = function (error, response, body) {
        res.send(body);
        res.end();
    };

    var updatedQuery = req.query;
    var options = {
        path: 'subject/getSubjectContact',
        query: updatedQuery,
        json: true
    };
    activiti.get(options, callback)
};

module.exports.findInCompany = function (req, res) {

  var callback = function (error, response, body) {
    res.send(body);
    res.end();
  };

  var updatedQuery = req.query;
  var options = {
    path: 'subject/group/findInCompany',
    query: updatedQuery,
    json: true
  };
  activiti.get(options, callback)
};

module.exports.getSubjectContactType = function (req, res) {

    var callback = function (error, response, body) {
        res.send(body);
        res.end();
    };

    var updatedQuery = req.query;
    var options = {
        path: 'subject/getSubjectContactType',
        query: updatedQuery,
        json: true
    };
    activiti.get(options, callback)
};

module.exports.setSubjectOrgan = function (req, res) {

    var callback = function (error, response, body) {
        res.send(body);
        res.end();
    };

    var updatedQuery = req.query;
    var options = {
        path: 'subject/setSubjectOrgan',
        query: updatedQuery,
        json: true
    };
    activiti.get(options, callback)
};

module.exports.getSubjectAccountTypes = function (req, res) {

    var callback = function (error, response, body) {
        res.send(body);
        res.end();
    };

    var updatedQuery = req.query;
    var options = {
        path: 'subject/getSubjectAccountTypes',
        query: updatedQuery,
        json: true
    };
    activiti.get(options, callback)
};

module.exports.getReferentStaff = function (req, res) {

  var callback = function (error, response, body) {
    res.send(body);
    res.end();
  };

  var updatedQuery = req.query;
  var options = {
    path: 'subject/getSubjectHuman',
    query: updatedQuery,
    json: true
  };
  activiti.get(options, callback)
};

module.exports.setReferentStaff = function (req, res) {

  var callback = function (error, response, body) {
    res.send(body);
    res.end();
  };

  var updatedQuery = req.query;
  var options = {
    path: 'action/identity/setUserGroup',
    query: updatedQuery,
    json: true
  };
  activiti.get(options, callback)
};

module.exports.removeReferentStaff = function (req, res) {

  var callback = function (error, response, body) {
    res.send(body);
    res.end();
  };

  var updatedQuery = req.query;
  var options = {
    path: 'action/identity/removeUserGroup',
    query: updatedQuery,
    json: true
  };
  activiti.del(options, callback)
};

module.exports.getSubjectGroupsTreeUp = function (req, res) {
    var callback = function (error, response, body) {
        res.send(body);
        res.end();
    };

    var updatedQuery = req.query;

    if(updatedQuery.sID_SubjectRole === 'ExecutorDepart') {
        updatedQuery.sSubjectType = 'Organ';
        delete updatedQuery.sID_SubjectRole;
    } else if(updatedQuery.sID_SubjectRole === 'Executor') {
        updatedQuery.sSubjectType = 'Human';
        delete updatedQuery.sID_SubjectRole;
    }

    var options = {
        path: 'subject/group/getSubjectGroupsTreeUp',
        query: updatedQuery
    };
    activiti.get(options, callback)
};

exports.setSubjectContact = function (req, res) {
  activiti.post({
    path: 'subject/setSubjectContact',
    query: {
      sID_Group_Activiti: req.body.sID_Group_Activiti
    },
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'

    },

  }, function (error, statusCode, result) {
    console.log(error);
    console.log(statusCode);
    console.log(result);
    error ? res.send(error) : res.status(statusCode).json(result);
  },  false,
    req.body.contacts
   );
};

exports.deleteSubjectContact = function (req, res) {
  activiti.post({
      path: 'subject/deleteSubjectContact',
      query: {
        sID_Group_Activiti: req.body.sID_Group_Activiti
      },
      headers: {
        'Content-Type': 'application/json;charset=UTF-8'

      },

    }, function (error, statusCode, result) {
      console.log(error);
      console.log(statusCode);
      console.log(result);
      error ? res.send(error) : res.status(statusCode).json(result);
    },  false,
    req.body.contacts
  );
};

exports.changeStaffPassword = function (req, res) {
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

module.exports.createSubjectHuman = function (req, res) {
  activiti.post({
    path: 'subject/setSubjectHuman',
    query: req.body.employee.params,
    headers: {
      'Content-Type': 'text/html;charset=utf-8'
    }
  }, function (error, statusCode, result) {
    console.log(error);
    console.log(statusCode);
    console.log(result);
    error ? res.send(error) : res.status(statusCode).json(result);
  }, false);
};

module.exports.getSubjectRightBP = function (req, res) {
  var callback = function (error, response, body) {
    res.send(body);
    res.end();
  };

  var updatedQuery = req.query;
  var options = {
    path: 'subject/group/getBPs_ForReferent',
    query: updatedQuery,
    json: true
  };
  activiti.get(options, callback)
};

module.exports.getAllBP = function (req, res) {
  var callback = function (error, response, body) {
    res.send(body);
    res.end();
  };

  var updatedQuery = req.query;
  var options = {
    path: 'subject/right/getAllBPs',
    query: updatedQuery,
    json: true
  };
  activiti.get(options, callback)
};

module.exports.setSubjectRightBP = function (req, res) {
  var callback = function (error, response, body) {
    res.send(body);
    res.end();
  };

  var updatedQuery = req.query;
  var options = {
    path: 'subject/right/setBP',
    query: updatedQuery,
    json: true
  };
  activiti.get(options, callback)
};
module.exports.removeSubjectRightBP = function (req, res) {
 var callback = function (error, response, body) {
    res.send(body);
    res.end();
  };

  var updatedQuery = req.query;
  var options = {
    path: 'subject/right/removeBP',
    query: updatedQuery,
    json: true
  };
  activiti.get(options, callback)
};