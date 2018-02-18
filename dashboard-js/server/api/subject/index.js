'use strict';

var express = require('express');
var router = express.Router();
var controller = require('./subject.controller');

router.get('/organs/join-tax', controller.getSubjectOrganJoinTaxList);
router.get('/organs/:nID_SubjectOrgan', controller.getSubjectOrganJoin);
router.post('/organs/attributes/:nID_SubjectOrgan/:nID', controller.getOrganAttributes);

router.get('/getSubjectStatus', controller.getSubjectStatus);
router.get('/sendPasswordOfUser', controller.sendPasswordOfUser);
router.get('/sendPasswordOfUserCustom', controller.sendPasswordOfUserCustom)
router.get('/sendPasswordsOfUsersOnServer', controller.sendPasswordsOfUsersOnServer);


module.exports = router;
