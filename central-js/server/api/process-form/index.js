var express = require('express')
  , router = express.Router()
  , form = require('./form.controller')
  , auth = require('../../auth/auth.service.js')
  , region = require('../../components/region')
  , endpoint = require('../../components/endpoint')
  , scansMock = require('../user-mock/scans-mock.controller.js');

router.get('/', endpoint.assertQueryParams('nID_Server', 'sID_BP_Versioned'), form.index);
router.post('/', form.submit);
router.get('/sign', form.signForm);
router.use('/sign/callback', form.signFormCallback);
router.get('/signMultiple', form.signFormMultiple);
router.use('/signMultiple/callback', form.signFormMultipleCallback);
router.get('/sign/check', form.signCheck);
router.post('/save', form.saveForm);
router.get('/load', form.loadForm);
router.get('/loadPatternFile', form.loadPatternFile);

//Mock scans process
router.post('/scansUpload', scansMock.scanUpload);

router.post('/scansUpload', form.scanUpload);

router.get('/get-nais-service', form.getMinJustForm);

module.exports = router;
