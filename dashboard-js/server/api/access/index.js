'use strict';

var express = require('express');
var router = express.Router();
var controller = require('./access.controller');

router.get('/setsLoginPrincipal', controller.setsLoginPrincipal);

module.exports = router;
