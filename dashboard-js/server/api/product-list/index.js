'use strict';

var express = require('express');
var router = express.Router();
var controller = require('./index.controller');

router.get('/', controller.getProducts);

module.exports = router;
