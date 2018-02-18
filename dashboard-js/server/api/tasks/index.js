'use strict';

var express = require('express');
var controller = require('./tasks.controller');

var router = express.Router();

router.get('/', controller.index);
router.get('/getSubFolders', controller.getSubFolders);
router.get('/getTaskData', controller.getTaskData);
router.get('/cancelTask', controller.cancelTask);
router.get('/getProcesses', controller.getProcesses);
router.get('/getTabByOrder', controller.getTabByOrder);
router.get('/getFile/:nFile', controller.getFile);
router.get('/getPatternFile', controller.getPatternFile);
router.get('/:taskId/events', controller.getAllTaskEvents);
router.get('/:taskId/form', controller.getForm);
router.get('/:taskId/form-from-history', controller.getFormFromHistory);
router.get('/:taskId/attachments', controller.getAttachments);
router.post('/action/task/saveForm', controller.saveChangesTask);
router.get('/:nID_Process/getOrderMessages', controller.getOrderMessages);
router.get('/:taskId/attachments/:attachmentId/content/:nFile', controller.getAttachmentContent);
router.get('/download/:keyOrProcessID/attachment/:typeOrAttachID', controller.getAttachmentFile);
router.get('/download/:keyOrProcessID/attachment/:typeOrAttachID/server/:taskServer', controller.getAttachmentFile);
router.get('/download/:keyOrProcessID/document/:sKey_Step', controller.getDocumentImage);
router.get('/download/:keyOrProcessID/attachment/:typeOrAttachID/:sFileName', controller.getAttachmentFile);
router.get('/download/:keyOrProcessID/attachment/:typeOrAttachID/:sFileName/server/:taskServer', controller.getAttachmentFile);
router.get('/download/getDocumentImagesAsBase64', controller.getDocumentImagesAsBase64);
router.get('/download/getAttachmentsAsBase64', controller.getAttachmentsAsBase64);
router.get('/open/:keyOrProcessID/attachment/:typeOrAttachID', controller.showAttachmentFile);
router.get('/open/:keyOrProcessID/attachment/:typeOrAttachID/name/:sFileName', controller.showAttachmentFile);
router.get('/:taskId/attachments/:attachmentId/table', controller.getAttachmentContentTable);
router.post('/:taskId/attachments/:field/upload',controller.uploadFile);
router.post('/:taskId/form', controller.submitForm);
router.post('/:taskId/setDocumentImage', controller.setDocumentImage);
router.post('/pluralUploading', controller.pluralUploadingOfSignedContents);
router.put('/:taskId', controller.updateTask);
router.put('/:taskId/unassign', controller.unassign);
router.get('/:taskId', controller.getTask);
router.get('/search/byOrder/:orderId', controller.getTasksByOrder);
router.get('/search/byText/:text/type/:sType', controller.getTasksByText);
router.post('/:taskId/upload_content_as_attachment', controller.upload_content_as_attachment);
router.post('/setTaskQuestions', controller.setTaskQuestions);
router.get('/:taskId/attachments/:attachmentId/checkAttachmentSign', controller.checkAttachmentSign);
router.get('/sign/checkAttachmentSignNew', controller.checkAttachmentSignNew);
router.get('/:taskId/getMessageFile/:messageId/:fileName', controller.getMessageFile);
router.post('/postServiceMessages', controller.postServiceMessage);
router.post('/:taskId/setTaskAttachment', controller.setTaskAttachment);
router.post('/:taskId/setTaskAttachmentNew', controller.setTaskAttachmentNew);
router.post('/uploadFileHTML', controller.uploadFileHTML);
router.post('/searchTasks', controller.searchTasks);


module.exports = router;
