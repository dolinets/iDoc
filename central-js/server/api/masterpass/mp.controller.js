'use strict';
var request = require('request'),
    masterPassAuth = require('./mp.service'),
    errorMessages = require('./bankResponses.service'),
    config = require('../../config/environment'),
    async = require('async');

function getOptions(req) {
  var config = require('../../config/environment');

  var activiti = config.activiti;

  return {
    protocol: activiti.protocol,
    hostname: activiti.hostname,
    port: activiti.port,
    path: activiti.path,
    username: activiti.username,
    password: activiti.password
  };
}

module.exports.walletOperations = function (req, res) {
  var auth = masterPassAuth.getUserAuth();
  var params = req.body.body;
  var guid, userID = null;

  if (req.body.body.guid) {
    guid = req.body.body.guid;
    delete req.body.body.guid;
  }

  if (req.body.body.user_id) {
    userID = req.body.body.user_id;
  }

  var callback = function (error, response, body) {
    if(!error) {
      body.response.guid = guid;
      body.response.user_id = userID;
      res.send(body);
      res.end();
    } else {
      res.send(error.message);
      res.end();
    }
  };

  request.post({
    "url": "https://walletmc.ipay.ua/",
    "headers": {"Content-type": "application/json; charset=utf-8"},
    "json": {
      "request": {
        "auth": auth,
        "action": req.body.action,
        "body": params
      }
    }
  }, callback);
};

module.exports.checkUser = function (req, res) {
  var auth = masterPassAuth.getUserAuth();
  var params = req.body.body;

  async.waterfall([
    walletCheck,
    showCardListOrRegister
  ], function (err, result, body) {
    if(!err) {
      res.send(body);
      res.end();
    } else {
      res.send(err.message);
      res.end();
    }
  });
  function walletCheck(callback) {
    request.post({
      "url": "https://walletmc.ipay.ua/",
      "headers": {"Content-type": "application/json; charset=utf-8"},
      "json": {
        "request": {
          "auth": auth,
          "action": req.body.action,
          "body": params
        }
      }
    }, function (err, res, body) {
      if(!err) {
        callback(null, body)
      } else {
        callback(error, null);
      }
    });

  }
  function showCardListOrRegister(result, callback) {
    var action;
    if(result.response.user_status === 'notexists') {
      action = 'RegisterByURL';
    } else if(result.response.user_status === 'invite') {
      action = 'InviteByURL';
    } else if(result.response.user_status === 'exists') {
      action = 'List';
    }

    request.post({
      "url": "https://walletmc.ipay.ua/",
      "headers": {"Content-type": "application/json; charset=utf-8"},
      "json": {
        "request": {
          "auth": auth,
          "action": action,
          "body": params
        }
      }
    }, function (err, response, body) {
      if(!err) {
        res.send({body: body, type: result.response.user_status});
        res.end();
      } else {
        res.send(err.message);
        res.end();
      }
    });
  }
};

module.exports.verify3DSCallback = function (req, res) {
  var guid, userID = req.query.user;

  if (req.query.guid) {
    guid = req.query.guid;
    delete req.query.guid;
  }

  var auth = masterPassAuth.getUserAuth(),
      params = {
        "pmt_id": req.query.id,
        "user_id": req.query.user,
        "msisdn": req.query.msisdn,
        "md": req.body.MD,
        "pares": req.body.PaRes
      },
      callbackUrl = req.query.url;

  async.waterfall([
    check3DSAnswer,
    checkoutOrReturn
  ], function (err, result, body) {
    if(!err) {
      res.send(body);
      res.end();
    } else {
      res.send(err.message);
      res.end();
    }
  });
  function check3DSAnswer(callback) {
    request.post({
      "url": "https://walletmc.ipay.ua/",
      "headers": {"Content-type": "application/json; charset=utf-8"},
      "json": {
        "request": {
          "auth": auth,
          "action": "PaymentVerify3DS",
          "body": params
        }
      }
    }, function (err, response, body) {
      if(!err) {
        callback(null, body)
      } else {
        callback(error, null);
      }
    });
  }
  function checkoutOrReturn(result, callback) {
    if(result.response.pmt_status == 1) {
      res.redirect(callbackUrl + '?status=' + result.response.pmt_status + '&pmt_id=' + result.response.pmt_id + '&guid=' + guid + '&user_id=' + userID + '&msisdn=' + result.response.msisdn + '&invoice=' + result.response.invoice);
    } else if(result.response.pmt_status == 4 && result.response.error || result.response.error){
      res.redirect(callbackUrl + '?status=' + result.response.error);
    } else if(result.response.pmt_status == 4 && !result.response.error) {
      res.redirect(callbackUrl + '?status=failed&bank_id=' + result.response.bank_response.bank_id + '&bank_response=' + result.response.bank_response.rc)
    }
  }
};

module.exports.createSaleCancelPayment = function (req, res) {
  var auth = masterPassAuth.getUserAuth();
  var params = req.body.body;
  params.guid = masterPassAuth.createGuid();

    request.post({
      "url": "https://walletmc.ipay.ua/",
      "headers": {"Content-type": "application/json; charset=utf-8"},
      "json": {
        "request": {
          "auth": auth,
          "action": req.body.action,
          "body": params
        }
      }
    }, function (err, response, body) {
      if(!err) {
        body.response.guid = params.guid;
        body.response.user_id = params.user_id;
        res.send(body);
        res.end();
      } else {
        res.send(err.message);
        res.end();
      }
    });
};

module.exports.verifyPhoneNumber = function (req, res) {
  var options = getOptions(req),
      isTestServer = config.bTest,
      url = options.protocol + '://' + options.hostname + options.path + '/subject/message/sendSms';

  var verifyData = masterPassAuth.createAndCheckOTP(req.query);

    var callback = function(error, response, body) {
      if(!error) {
        res.send({message: body});
        res.end();
      } else {
        res.send(error);
        res.end();
      }
    };

    if (!isTestServer) {
      return request.get({
        'url': url,
        'auth': {
          'username': options.username,
          'password': options.password
        },
        'qs': {
          'phone': verifyData.phone,
          'message': verifyData.code,
          'sID_Order': '1'
        }
      }, callback);
    } else {
      res.send({message: 'ok'});
      res.end();
    }
};

module.exports.confirmOtp = function (req, res) {
  var response = masterPassAuth.createAndCheckOTP(req.query);
    if(response) {
      res.send(response);
      res.end();
    } else {
      res.send(false);
      res.end();
    }
};

module.exports.getErrorMessage = function (req, res) {
  var response = errorMessages.getErrorMessage(req.query.code, req.query.error);
  if(response) {
    res.send(response);
    res.end();
  } else {
    res.send('Спробуйте пiзнiше');
    res.end();
  }
};
