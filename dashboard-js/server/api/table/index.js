'use strict';

var express = require('express');
var router = express.Router();
var controller = require('./index.controller.js');


router.get('/getTable', controller.getTable);

module.exports = router;
