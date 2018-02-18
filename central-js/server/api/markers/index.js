var express = require('express');
var router = express.Router();

var activiti = require('../../components/activiti');
var config = require('../../config/environment');

router.post('/validate', validateMarkers);
router.get('/checkOrderValue/:orderId', checkOrderValue);

module.exports = router;

function validateMarkers(req, res) {
  var Ajv = require('ajv');
  if (!req.body.definitions || !req.body.definitions.schema) {
    var connect = {
      errors: [{messages: "ERROR Connection to marker validator"}]
    };
    res.send({valid: false, errors: connect.errors});
  } else {
    if (!req.body.definitions.options) {
      req.body.definitions.options = {
        allErrors: true,
        useDefaults: true
      }
    }
    var ajv = Ajv(req.body.definitions.options);
    var validate = ajv.compile(req.body.definitions.schema);
    var valid = validate(req.body.markers);
    res.send({valid: valid, errors: validate.errors});
  }
}

function checkOrderValue(req, res) {
  var serverId = req.query.nID_Server || req.body.nID_Server;
  var nID_Server = (!serverId || serverId < 0) && serverId !== 0 ? config.activiti.nID_Server : serverId;
  activiti.getServerRegionHost(nID_Server, function(sHost){
    activiti.sendGetRequest(req, res, '/service/action/task/getTasksByOrder', {
      'nID_Order': req.params.orderId
    }, null, sHost);
  });
}
