'use strict';

var activiti = require('../../components/activiti'),
    proxy = require('../../components/proxy');

exports.uploadFile = function (req, res) {
  var qs = {
    sFileNameAndExt:req.query.sFileNameAndExt
  };
  
  if(req.query.sID_Field){
    qs['sID_Field'] = req.query.sID_Field;
  }

  if(req.query.nID_Process) {
    qs['nID_Process'] = req.query.nID_Process;
  } else {
    qs['sID_StorageType'] = 'Redis'
  }
  qs['url'] = '';

  var params = {
    path: 'object/file/setProcessAttach',
      query: qs
  };

  if (req.query.taskServer) {
    params.taskServer = req.query.taskServer;
    delete req.query.taskServer;
  }

  var options = {
    url: activiti.getRequestURL(params)
  };
  req.url='';

  proxy.upload(req, res, options.url, function (error) {
    res.status(500).send({ error: error });
  });
};
