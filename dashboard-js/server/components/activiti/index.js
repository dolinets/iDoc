'use strict';

var _ = require('lodash');
var config = require('../../config/environment');
var request = require('request');
var url = require('url');

var Buffer = require('buffer').Buffer;

var authBase = 'Basic ' + new Buffer(
    config.activiti.username +
    ':' +
    config.activiti.password)
    .toString('base64');

var httpProxy = require('http-proxy');
var default_headers = {
  'Authorization': authBase
};

module.exports.getAuthHeaderValue = function () {
  return authBase;
};

/* 'Authorization': config.activiti.auth.basic */

request.debug = config.request.debug;

var createUploadProxy = function () {
  var uploadProxy = httpProxy.createProxyServer({});
  uploadProxy.on('proxyReq', function (proxyReq, req, res, options) {
    proxyReq.path = options.target.path;
    proxyReq.setHeader('Authorization', authBase);
    /*proxyReq.setHeader('Authorization', config.activiti.auth.basic);*/
  });
  uploadProxy.on('proxyRes', function (proxyRes, req, res) {

  });
  return uploadProxy;
};

var getRequestURL = function (options) {
  var requestURL,
      hostname = !options.taskServer ? config.activiti.host : options.taskServer;


  requestURL = url.format({
    protocol: config.activiti.prot,
    hostname: hostname,
    port: config.activiti.port,
    pathname: '/' + (options.root || config.activiti.rest) + '/' + options.path,
    query: options.query
  });

  if (options.taskServer) {
    delete requestURL.port;
  }

  if (options.doNotUseActivityConfigUrl) {
    requestURL = options.path;
  }

  return requestURL;
};

var getRequestOptions = function (options) {
  var headers = options.headers;
  /*if (config.activiti.auth.basic) {*/
  if (config.activiti.password) {
    headers = _.merge(options.headers, default_headers) || default_headers;
  }

  return {
    url: getRequestURL(options),
    headers: headers,
    json: options.json ? options.json : false
  };
};

var prepareRequest = function (req, options, data) {
  var r = null;
  if (req.method === 'POST') {
    r = request.post(_.merge(getRequestOptions(options),
      data ? {
        json: true,
        body: data
      } : {
        json: true,
        body: req.body
      }));
  } else {
    r = request(getRequestOptions(options));
  }
  return r;
};

var uploadProxy = createUploadProxy();

exports.getRequestURL = getRequestURL;
exports.getRequestOptions = getRequestOptions;

exports.get = function (options, onResult) {
  request.get(getRequestOptions(options), function (error, response, body) {
    if (!error) {
      onResult(null, response.statusCode, body, response.headers);
    } else {
      onResult(error, null, null);
    }
  });
};

exports.filedownload = function (req, res, options) {
  var r = prepareRequest(req, options);
  req.pipe(r).on('response', function (response) {
    response.headers['content-type'] = 'application/octet-stream';
  }).pipe(res);
};

//downloads the file with the specified content type
exports.typedfiledownload = function (req, res, options) {
  var r = prepareRequest(req, options);
  req.pipe(r).on('response', function (response) {
    response.headers['content-type'] = options.contentType;
  }).pipe(res);
};

exports.typedfileshow = function (req, res, options) {
  var r = prepareRequest(req, options);
  
  req.pipe(r).on('response', function (response) {
    var fileName = response.headers['content-disposition'].split('=')[1];
    var ext = fileName.split('.');
    switch(ext[ext.length-1]){
      case 'pdf': response.headers['Content-Type'] = 'application/pdf'; break;
      case 'png': response.headers['Content-Type'] = 'image/png'; break;
      case 'gif': response.headers['Content-Type'] = 'image/gif'; break;
      case 'jpeg': response.headers['Content-Type'] = 'image/jpeg'; break;
      case 'jpg': response.headers['Content-Type'] = 'image/jpeg'; break;
      case 'bmp': response.headers['Content-Type'] = 'image/bmp'; break;
      default: break;
    }
    response.headers['content-disposition'] = 'inline; filename="'+fileName+'"';
  }).pipe(res);
};

exports.fileupload = function (req, res, options) {
  uploadProxy.web(req, res, {
    target: options.url,
    secure: false
  }, function (e) {
    if (e) {

    }
  });
};

exports.pipe = function (req, res, options, data) {
  var r = null;
  if (req.method === 'POST') {
    r = request.post(_.merge(getRequestOptions(options),
      data ? {
        json: true,
        body: data
      } : {
        json: true,
        body: req.body
      }));
  } else {
    r = request(getRequestOptions(options));
  }
  req.pipe(r).pipe(res);
};

exports.post = function (options, onResult, data, json) {
  if (typeof(json) == 'undefined')
    json = true;
  request.post(_.merge(getRequestOptions(options), data ? {
      json: json,
      body: data
    } : {
      json: json
    }),
    function (error, response, body) {
      if (!error) {
        onResult(null, response.statusCode, body, response.headers);
      } else {
        onResult(error, null, null);
      }
    });
};

/**
 * https://github.com/e-government-ua/i/issues/1382
 * @param options
 * @param onResult
 */
exports.uploadStream = function (options, onResult) {
  var formData = {
    file: options.stream
  };
  if(options.nID_Process){
    formData.nID_Process = options.nID_Process;
  }
  if(options.sFileNameAndExt){
    formData.sFileNameAndExt = options.sFileNameAndExt.replace(new RegExp(/[*|\\:"<>?/]/g), "");
  }
  if(options.sID_StorageType){
    formData.sID_StorageType = options.sID_StorageType;
  }
  if(options.sID_Field){
    formData.sID_Field = options.sID_Field;
  }
  if(options.sLogin){
    formData.sLogin = options.sLogin;
  }
  if(options.sKey_Step){
    formData.sKey_Step = options.sKey_Step;
  }
  if(options.isMime){
    formData.isMime = options.isMime;
  }
  var content = {
    url: getRequestURL(options),
    formData: formData,
    headers: default_headers
  };
  request.post(content, function (error, response, body) {
    if (!error) {
      onResult(null, response.statusCode, body, response.headers);
    } else {
      onResult(error, null, null);
    }
  });
};

exports.put = function (options, onResult, data) {
  request.put(_.merge(getRequestOptions(options), data ? {
      json: true,
      body: data
    } : {
      json: true
    }),
    function (error, response, body) {
      if (!error) {
        onResult(null, response.statusCode, body, response.headers);
      } else {
        onResult(error, null, null);
      }
    });
};

exports.del = function (options, onResult) {
  request.del(getRequestOptions(options), function (error, response, body) {
    if (!error) {
      onResult(null, response.statusCode, body, response.headers);
    } else {
      onResult(error, null, null);
    }
  });
};

/*
 * sendGetRequest with central url using for select fields;
 */
exports.sendGetRequest = function (req, res, apiURL, params, callback, sHost, buffer) {
  var options = {
    path : apiURL,
    query : params
  };
  var _callback = callback ? callback : this.getDefaultCallback(res);
  var url = getRequestURLToCentral(options);
  return request(url, _callback);
};

exports.getDefaultCallback = function (res) {
  return function (error, response, body) {
    if (error) {
      res.status(500).send(error);
    } else {
      res.status(response.statusCode).send(body);
    }
  }
};

var getRequestURLToCentral = function (options) {

  var requestURL = url.format({
    protocol: config.activiti_central.prot,
    hostname: config.activiti_central.host,
    port: config.activiti_central.port,
    pathname: '/' + (options.root || config.activiti_central.rest) + options.path
  });
  var qs = options.query;

  return {
    url: requestURL,
    json: true,
    qs: qs,
    auth: {
      username:config.activiti_central.username,
      password:config.activiti_central.password
    }
  };
};
