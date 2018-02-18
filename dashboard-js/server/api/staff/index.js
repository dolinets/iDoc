'use strict';

var express = require('express');
var router = express.Router();
var controller = require('./staff.controller');


router.get('/', controller.getSubjectHumanPositionCustom);
router.get('/setSubjectHumanPositionCustom', controller.setSubjectHumanPositionCustom);
router.post('/createSubjectHuman', controller.createSubjectHuman);
router.get('/editSubjectHuman', controller.editSubjectHuman);
router.get('/getSubjectGroupsTreeUp', controller.getSubjectGroupsTreeUp);
router.post('/changeStaffPassword', controller.changeStaffPassword);
router.get('/getStaffContact', controller.getStaffContact);
router.get('/getSubjectContactType', controller.getSubjectContactType);
router.post('/setSubjectContact', controller.setSubjectContact);
router.post('/deleteSubjectContact', controller.deleteSubjectContact);
router.get('/setSubjectOrgan', controller.setSubjectOrgan);
router.get('/getSubjectAccountTypes', controller.getSubjectAccountTypes);
router.get('/getReferentStaff', controller.getReferentStaff);
router.get('/setReferentStaff', controller.setReferentStaff);
router.delete('/removeReferentStaff', controller.removeReferentStaff);
router.get('/findInCompany', controller.findInCompany);
router.get('/checkIsAdmin', controller.checkIsAdmin);
router.get('/setSubjectRightBP', controller.setSubjectRightBP);
router.get('/removeSubjectRightBP', controller.removeSubjectRightBP);
router.get('/getSubjectRightBP', controller.getSubjectRightBP);
router.get('/getAllBP', controller.getAllBP);

module.exports = router;