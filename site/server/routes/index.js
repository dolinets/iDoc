var express = require('express');
var router = express.Router();
var log = require('../libs/log')(module);

/* GET home page. */
router.get('/', function(req, res, netx) {
  res.render('index' );
});

// router.post('/app', function (req, res, netx) {
//   res.json(req.body);
//   log.info(req.body);
//
// });

module.exports = router;
