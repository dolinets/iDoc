'use strict';

var express = require('express');
var router = express.Router();
var controller = require('./chat.controller');

router.post('/setProcessChatMessage', controller.setProcessChatMessage);
router.get('/getProcessChat', controller.getProcessChat);
router.put('/updateProcessChatMessage', controller.updateProcessChatMessage);
router.delete('/deleteProcessChatMessage', controller.deleteProcessChatMessage);

module.exports = router;
