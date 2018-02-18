'use strict';

var express = require('express');
var controller = require('./index.controller');

var router = express.Router();

router.get('/getDocumentStepRights', controller.getDocumentStepRights);
router.get('/getDocumentStepLogins', controller.getDocumentStepLogins);
router.get('/getProcessSubject', controller.getProcessSubject);
router.get('/getBPs_ForReferent', controller.getBPs_ForReferent);
router.get('/setDocument', controller.setDocument);
router.get('/getProcessSubjectTree', controller.getProcessSubjectTree);
router.get('/delegateDocument', controller.delegateDocument);
router.get('/getDocumentSubmittedUnsigned', controller.getDocumentSubmittedUnsigned);
router.get('/removeDocumentSteps', controller.removeDocumentSteps);
router.get('/addAcceptor', controller.addAcceptor);
router.get('/addVisor', controller.addVisor);
router.get('/addViewer', controller.addViewer);
router.get('/cancelDocumentSubmit', controller.cancelDocumentSubmit);
router.get('/removeDocumentStepSubject', controller.removeDocumentStepSubject);
router.get('/setDocumentUrgent', controller.setDocumentUrgent);

module.exports = router;
