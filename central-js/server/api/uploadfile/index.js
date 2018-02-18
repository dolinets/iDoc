var express = require('express')
  , router = express.Router()
  , uploadfileController = require('./uploadfile.controller');

router.post('/', uploadfileController.uploadProxy);
router.post('/uploadFileHTML', uploadfileController.uploadFileHTML);
router.post('/setAttachment', uploadfileController.setAttachment);

module.exports = router;
