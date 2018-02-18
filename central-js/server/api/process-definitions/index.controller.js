var activiti = require('../../components/activiti');

module.exports.index = function (req, res) {
  var sHost = req.region.sHost;
  var params = {
    latest: true,
    size: 1500
  };
  if(req.query.key){
    params.key = req.query.key;
  }
  activiti.get('/service/repository/process-definitions', params, function (error, response, body) {
    res.send(body);
  }, sHost);
};
