'use strict';

var express = require('express');
var controller = require('./process.controller');

var router = express.Router();

router.get('/', controller.index);
router.get('/getLoginBPs', controller.getLoginBPs);
router.get('/getBPs_ForExport', controller.getBPs_ForExport);
router.get('/getmID_TaskAndProcess', controller.getmIDTaskAndProcess);
router.get('/getAllBpForLogin', controller.getAllBpForLogin);
router.get('/getBPFields', controller.getBPFields);
router.post('/setProcessSubjectStatus', controller.setProcessSubjectStatus);


module.exports = router;
