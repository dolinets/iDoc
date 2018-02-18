'use strict';

var express = require('express');
var router = express.Router();
var controller = require('./index.controller');

router.get('/', controller.getCurrentServer);
router.get('/updateServerInfo', controller.getNewServerName);

module.exports = router;
