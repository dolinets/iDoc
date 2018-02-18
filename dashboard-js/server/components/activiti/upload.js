var _ = require('lodash')
  , request = require('request')
  , activitiBase = require('./index');


var options;
var Buffer = require('buffer').Buffer;
var config = require('../../config/environment');

var authBase = 'Basic ' + new Buffer(
    config.activiti.username +
    ':' +
    config.activiti.password)
    .toString('base64');

function getConfigOptions(anotherServerRequest) {
  if (options)
    return options;

  var activiti = config.activiti;

  options = {
    protocol: activiti.prot,
    hostname: anotherServerRequest ? anotherServerRequest : activiti.host,
    port: activiti.port,
    path: activiti.rest,
    username: activiti.username,
    password: activiti.password
  };

  if (anotherServerRequest)
    delete options.port;

  return options;
}

function getRequestUrl(apiURL, sHost, anotherServerRequest) {
  var options = getConfigOptions(anotherServerRequest);
  return (sHost !== null && sHost !== undefined ? sHost : options.protocol + '://' + options.hostname + ':' + options.port + '/' + options.path + '/') + apiURL;
}

function buildGET(apiURL, params, sHost, isCustomAuth, buffer, anotherServerRequest) {
  var sURL = getRequestUrl(apiURL, sHost, anotherServerRequest);
  var qs = params;

  var reqObj = {
    url: sURL,
    json: true,
    qs: qs
  };

  if (buffer) {
    reqObj.encoding = null;
  }

  if (!isCustomAuth) {
    _.extend(reqObj, {auth: activitiBase.getAuthHeaderValue()})
  }

  return reqObj;
}

/*/**
 * General method to upload different content
 *
 * @param apiURL url where to upload content
 * @param params parameters for URL
 * @param content array of content that should be uploaded. object {name, request|text|file, [options : {filename, contentType}]}
 * @param callback here will be result, pass function there
 * @param sHost optional, for adding it in the beginning of the apiURL
 */
module.exports.uploadContent = function (apiURL, params, content, callback, sHost) {
  if (!content || content.length < 0) {
    throw Error('There is nothing to upload to ' + apiURL + '. Content shouldn\'t be empty array');
  }

  var uploadRequest;

  if(!params.headers){
    params.headers = {};
  }

  if(!params.headers.Authorization){
    _.merge(params.headers, {
      'Authorization': authBase
    });
  }

  if (params.qs || params.headers) {
    //params is object with query string and/or headers
    var hasCustomAuth = params.headers && params.headers.Authorization ? true : false;
    var qs = params.qs ? params.qs : {};
    uploadRequest = buildGET(apiURL, qs, sHost, hasCustomAuth, params.taskServer);
    if (params.headers) {
      if (!uploadRequest.headers) {
        uploadRequest.headers = {};
      }
      _.extend(uploadRequest.headers, params.headers);
    }
  } else {
    //params are query string
    uploadRequest = buildGET(apiURL, params, sHost);
  }

  if (!uploadRequest.headers) {
    uploadRequest.headers = {};
  }
  _.merge(uploadRequest.headers, {'Accept': 'application/json'});

  function formAppend(formData, content, name) {
    if(_.isArray(content)){
      content.forEach(function (formContent) {
        addContent(formData, formContent, name);
      });
    } else {
      addContent(formData, content, name);
    }

  }

  function addContent(formData, formContent, name) {
    var contentOptions;
    if (formContent.options) {
      contentOptions = formContent.options;
    }

    if(name){
      formContent.name = name;
    }

    if (formContent.request) {
      formData.append(formContent.name, formContent.request, contentOptions);
    } else if (formContent.file) {
      formData.append(formContent.name, formContent.file, contentOptions);
    } else if (formContent.text) {
      formData.append(formContent.name, formContent.text, contentOptions);
    } else if (formContent.buffer) {
      formData.append(formContent.name, formContent.buffer, contentOptions);
    }

  }


  var r = request.post(uploadRequest, callback);
  formAppend(r.form(), content, params.qs && params.qs.sID_Field ? params.qs.sID_Field : undefined);
};

module.exports.get = function (apiURL, params, callback, sHost, buffer) {
  if(!params.headers){
    params.headers = {};
  }

  if(!params.headers.Authorization){
    _.merge(params.headers, {
      'Authorization': authBase
    });
  }
  var prepared = buildGET(apiURL, params, sHost, true, buffer);
  return request(prepared, callback);
};

module.exports.downloadBuffer = function (apiURL, params, callback, sHost) {
  this.get(apiURL, params, callback, null, true);
};
