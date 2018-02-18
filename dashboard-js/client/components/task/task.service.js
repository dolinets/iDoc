'use strict';

angular.module('dashboardJsApp')
  .factory('tasks', function tasks($http, $q, $rootScope, uiUploader, $compile, $timeout, processes, $filter, TableService,
                                   PrintTemplateProcessor, Auth, generationService, Modal, signDialog, CurrentServer) {
    function simpleHttpPromise(req, callback) {
      var cb = callback || angular.noop;
      var deferred = $q.defer();

      $http(req).then(
        function (response) {
          deferred.resolve(response.data);
          return cb();
        },
        function (response) {
          deferred.reject(response);
          return cb(response);
        }.bind(this));
      return deferred.promise;
    }

    return {
      filterTypes: {
        control: 'control',
        selfAssigned: 'selfAssigned',
        unassigned: 'unassigned',
        documents: 'documents',
        myDocuments: 'myDocuments',
        docHistory: 'docHistory',
        viewed: 'viewed',
        ecp: 'ecp',
        finished: 'finished',
        tickets: 'tickets',
        all: 'all',
        execution: 'execution',
        executed: 'executed',
        controled: 'controled'
      },
      /**
       * Get list of tasks
       *
       * @param  {Function} callback - optional
       * @return {Promise}
       */
      list: function (filterType, params, sort) {
        var def = $q.defer();
        var bshowDeletedTasks = JSON.parse(localStorage.getItem('deleted-tasks-status'));
        simpleHttpPromise({
          method: 'GET',
          url: '/api/tasks',
          params: angular.merge({filterType: filterType, sOrderBy: sort,
            bIncludeDeleted: bshowDeletedTasks !== null ? bshowDeletedTasks.status : false}, params)
        }).then(function (resp) {
          var tab = filterType;
          if(resp.aoTaskDataVO && tab === 'ecp'){
            angular.forEach(resp.aoTaskDataVO, function (task) {
              task.bSelectedForEDS = false;
            })
          }
          def.resolve(resp);
        }, function (err) {
          def.reject(err);
        });
        return def.promise;
      },

      listByTasksIDs: function (sFilterType, aIds) {
        var self = this;
        var def = $q.defer();
        var aExistingIDs;
        if(angular.isArray(aIds) && aIds.length > 0){
          aExistingIDs = angular.copy(aIds);
        } else {
          aExistingIDs = [];
        }

        var result = [];

        var searchTaskInType = function(params, page) {

          if (!page){
            page = 0;
          }
          self.list(params.type, {page: page}).then(function(response){

            for (var i = 0; i < response.aoTaskDataVO.length; i++) {
              if(aExistingIDs.includes(response.aoTaskDataVO[i].id)){
                result.push(response.aoTaskDataVO[i]);
                aExistingIDs.splice(aExistingIDs.indexOf(response.aoTaskDataVO[i].id), 1);
              }
            }

            if (aExistingIDs.length > 0) {
              if ((response.start + response.size) < response.total){
                searchTaskInType(params, page + 1);
              } else {
                def.resolve(result);
              }
            } else {
              def.resolve(result);
            }
          })
        };

        if(aExistingIDs.length > 0){
          searchTaskInType({type:sFilterType});
        } else {
          def.resolve([]);
        }

        return def.promise;
      },

      getEventMap: function () {
        var deferred = $q.defer();
        var eventMap = {
          'AddAttachment': {},
          'AddComment': {
            'messageTemplate': '${ user.name } відповів(ла): ${ message }',
            'getMessageOptions': function (messageObject) {
              return !_.isEmpty(messageObject) ? messageObject[0] : '';
            },
            'getFullMessage': function (user, messageObject) {
              return _.template(
                eventMap.AddComment.messageTemplate, {
                  'user': {
                    'name': user.name
                  },
                  'message': eventMap.AddComment.getMessageOptions(messageObject)
                }
              );
            }
          },
          'AddGroupLink': {},
          'AddUserLink': {
            'messageTemplate': '${ user.name } призначив(ла) : ${ message }',
            'getMessageOptions': function (messageObject) {
              return !_.isEmpty(messageObject) ? messageObject[0] : '';
            },
            'getFullMessage': function (user, messageObject) {
              return _.template(
                eventMap.AddUserLink.messageTemplate, {
                  'user': {
                    'name': user.name
                  },
                  'message': eventMap.AddUserLink.getMessageOptions(messageObject)
                }
              );
            }
          },
          'DeleteAttachment': {},
          'DeleteGroupLink': {},
          'DeleteUserLink': {}
        };

        deferred.resolve(eventMap);

        return deferred.promise;
      },

      assignTask: function (taskId, userId, callback) {
        var taskServer = CurrentServer.getServer();
        return simpleHttpPromise({
          method: 'PUT',
          url: '/api/tasks/' + taskId,
          data: {
            assignee: userId
          },
          params: {taskServer: taskServer.another ? taskServer.name : null}
        }, callback);
      },

      downloadDocument: function (taskId, callback) {
        return simpleHttpPromise({
          method: 'GET',
          url: '/api/tasks/' + taskId + '/document'
        }, callback);
      },


      getOrderMessages: function (processId, callback) {
        return simpleHttpPromise({
          method: 'GET',
          url: '/api/tasks/' + processId + '/getOrderMessages'
        }, callback);
      },

      taskForm: function (taskId, callback) {
        var serverData = CurrentServer.getServer();
        return simpleHttpPromise({
          method: 'GET',
          url: '/api/tasks/' + taskId + '/form',
          params: {taskServer: serverData.another ? serverData.name : null}
        }, callback);
      },

      getDocumentStepRights: function (nID_Process) {
        return simpleHttpPromise({
          method: 'GET',
          url: '/api/documents/getDocumentStepRights',
          params: {
            nID_Process: nID_Process
          }
        })
      },

      getDocumentStepLogins: function (nID_Process, taskType) {
        var taskServer = CurrentServer.getServer();

        return simpleHttpPromise({
          method: 'GET',
          url: '/api/documents/getDocumentStepLogins',
          params: {
            nID_Process: nID_Process,
            bHistory: taskType === 'docHistory',
            taskServer: taskServer.another ? taskServer.name : null
          }
        })
      },

      getProcessSubjectTree: function (id) {
        return simpleHttpPromise({
          method: 'GET',
          url: '/api/documents/getProcessSubjectTree',
          params: {
            snID_Process_Activiti: id,
            nDeepLevel: 0
          }
        })
      },

      getDocumentImage: function (taskId, sKey_Step, bBase64) {
        return simpleHttpPromise({
          method: 'GET',
          url: '/api/tasks/download/' + taskId + '/document/' + sKey_Step,
          params: {
            bAsBase64: bBase64
          }
        })
      },

      getDocumentImagesAsBase64: function (params) {
        return simpleHttpPromise({
          method: 'GET',
          url: '/api/tasks//download/getDocumentImagesAsBase64',
          params: {
            aArrayOfParams: params
          }
        })
      },

      getAttachmentsAsBase64: function (params) {
        return simpleHttpPromise({
          method: 'GET',
          url: '/api/tasks//download/getAttachmentsAsBase64',
          params: {
            aArrayOfParams: params
          }
        })

      },

      getTableOrFileAttachment: function (taskId, attachId, isNewService, bBase64) {
        // old and new services requests
        var serverData = CurrentServer.getServer();

        if(isNewService) {
          return simpleHttpPromise({
            method: 'GET',
            url: '/api/tasks/download/' + taskId + '/attachment/' + attachId,
            params: {
              bAsBase64: bBase64,
              taskServer: serverData.another ? serverData.name : null
            }
          })
        } else {
          return simpleHttpPromise({
            method: 'GET',
            url: '/api/tasks/' + taskId + '/attachments/' + attachId + '/table',
            params: {taskServer: serverData.another ? serverData.name : null}
          })
        }
      },

      taskFormFromHistory: function (taskId) {
        return simpleHttpPromise({
          method: 'GET',
          url: '/api/tasks/' + taskId + '/form-from-history'
        });
      },

      taskAttachments: function (taskId, callback) {
        return simpleHttpPromise({
          method: 'GET',
          url: '/api/tasks/' + taskId + '/attachments'
        }, callback);
      },

      submitTaskForm: function (formProperties, taskData, issue, oParams, sSignType, needAssign, isEdit) {
        var self = this,
          tasksServer = CurrentServer.getServer(),
          deferred = $q.defer(),
          promises = [],
          tablePromises = [],
          items = 0,
          def = [],
          tablePromisesReal = [];

        var attachments = taskData.aAttachment;

        var createProperties = function (formProperties, processVariables) {
          var properties = new Array();
          angular.forEach(processVariables, function (value, key) {
            if (key === 'bAuthorEdit' && value === 'true' && isEdit !== true){
              properties.push({
                id: key,
                value: 'false'
              });
            }
          });
          for (var i = 0; i < formProperties.length; i++) {
            var formProperty = formProperties[i];
            if (formProperty && formProperty.id === 'bAuthorEdit') {
              properties.push({
                id: formProperty.id,
                value: isEdit ? isEdit.toString() : null
              });
            } else if (formProperty && formProperty.writable) {
              properties.push({
                id: formProperty.id,
                value: formProperty.value
              });
            }
          }
          return properties;
        };

        // upload tables sync
        var syncTableUpload = function (i, table, defs) {
          if (i < table.length) {
            self.uploadAttachment(table[i].table, table[i].taskID, table[i].tableId, table[i].desc, table[i].isNew).then(function(resp) {
              defs[i].resolve();
              ++i;
              syncTableUpload(i, table, defs);
            })
          }
        };

        var tableFields = $filter('filter')(formProperties, function(prop){
          return prop.type === 'table' || prop.type === 'fileHTML';
        });

        if(tableFields.length > 0) {
          for(var t=0; t<tableFields.length; t++) {
            var table = tableFields[t];
            /*
              in old service we need to check that we are saving new table or update old, so if id table from form field
              is equal attach id -> update, otherwise save as new. later it can be removed..
            */
            var isNotEqualsAttachments = function (table, num) {
              var checkForNewService = table.name.split(';');
              if (checkForNewService.length === 3 && checkForNewService[2].indexOf('bNew=true') > -1 || table.type === 'fileHTML') {
                def[num] = $q.defer();
                tablePromises[num] = {table:table, taskID:taskData.oProcess.nID, tableId:table.id, desc:null, isNew:true};
                tablePromisesReal[num] = def[num].promise;
              } else {
                var tableName = table.name.split(';')[0];
                def[num] = $q.defer();
                tablePromises[num] = {table:table, taskID:taskData.nID_Task, tableId:null, desc:tableName, isNew:false};
                tablePromisesReal[num] = def[num].promise;
              }
            };

            if(attachments.length > 0) {
              var theSameAttachments = attachments.filter(function (item) {
                var matchTableId = item.description.match(/(\[id=(\w+)\])/);
                var x = item.description.indexOf('[table]') !== -1 && matchTableId !== null;
                if(x) {
                  var name = matchTableId[2];
                  return name.toLowerCase() === table.id.toLowerCase()
                }
              });

              if(theSameAttachments.length !== 0) {
                theSameAttachments.map(function (a) {
                  var description = a.description.split('[')[0];
                  def[t] = $q.defer();
                  tablePromises[t] = {table:table, taskID:taskData.nID_Task, tableId:a.id, desc:description, isNew:false};
                  tablePromisesReal[t] = def[t].promise;
                });
              } else {
                isNotEqualsAttachments(table, t);
              }
            } else {
              isNotEqualsAttachments(table, t);
            }
          }
        }

        syncTableUpload(items, tablePromises, def);

        // upload files before form submitting
        promises.push(this.uploadTaskFiles(formProperties, taskData));
        var filesProm = $q.all(promises);
        var tableProm = $q.all(tablePromisesReal);

        $q.all([filesProm, tableProm]).then(function () {
          var submitTaskFormData = {
            'taskId': taskData.nID_Task,
            'properties': createProperties(formProperties, taskData.mProcessVariable)
          };

          if (issue)
            submitTaskFormData.aProcessSubjectTask = issue;

          if (oParams) {
            if (oParams.nID_Process)
              submitTaskFormData.nID_Process = oParams.nID_Process;
            if (oParams.sStep_Document)
              submitTaskFormData.sKey_Step = oParams.sStep_Document;
          }

          var req = {
            method: 'POST',
            url: '/api/tasks/' + taskData.nID_Task + '/form',
            params: {
              sName_DocumentStepSubjectSignType: sSignType
            },
            data: submitTaskFormData
          };

          if (tasksServer.another)
            req.params.taskServer = tasksServer.name;
          if (needAssign)
            req.params.needAssign = Auth.getCurrentUser().id;

          simpleHttpPromise(req).then(function (result) {
              deferred.resolve(result);
            },
            function(result) {
              deferred.resolve(result);
            });
        });

        return deferred.promise;
      },

      uploadAttachment: function(files, taskId, attachmentID, description, isNewService) {
        var deferred = $q.defer(),
          taskServer = CurrentServer.getServer(),
          ID = files.id,
          stringifyContent,
          data = {},
          url,
          ext;

        if(files.type === 'table') {
          stringifyContent = JSON.stringify(files);
          ext = '.json'
        } else if (files.type === 'fileHTML') {
          stringifyContent = files.valueVisible;
          ext = '.html'
        } else {
          // when added new type, chose your extension and content
        }

        if(isNewService) {
          data = {
            sFileNameAndExt: ID + ext,
            sContent: stringifyContent,
            nID_Process: taskId,
            nID_Attach: attachmentID
          };
          url = '/api/tasks/' + taskId + '/setTaskAttachmentNew';
        } else {
          data = {
            sDescription: description + '[table][id='+ ID +']',
            sFileName: ID + ext,
            sContent: stringifyContent,
            nID_Attach: attachmentID
          };
          url = '/api/tasks/' + taskId + '/setTaskAttachment';
        }

        if (taskServer.another)
          data.taskServer = taskServer.name;

        $http.post(url, data).success(function(uploadResult){
          var parsedResponse = JSON.parse(uploadResult);
          if(parsedResponse && parsedResponse.sKey && parsedResponse.sID_StorageType){
            files.value = uploadResult;
          }else {
            files.value = parsedResponse.id;
          }
          deferred.resolve(uploadResult);
        });

        return deferred.promise;
      },

      saveChangesTaskForm: function (formProperties, taskData, issue) {
        var self = this;
        var attachments = taskData.aAttachment;
        var promises = [];
        var deferred = $q.defer(),
          tablePromises = [],
          items = 0,
          def = [],
          tablePromisesReal = [];

        var createProperties = function (formProperties) {
          var properties = new Array();
          for (var i = 0; i < formProperties.length; i++) {
            var formProperty = formProperties[i];
            if (formProperty && formProperty.writable) {
              properties.push({
                id: formProperty.id,
                value: formProperty.value
              });
            }
          }
          return properties;
        };

        var syncTableUpload = function (i, table, defs) {
          if (i < table.length) {
            self.uploadAttachment(table[i].table, table[i].taskID, table[i].tableId, table[i].desc, table[i].isNew).then(function(resp) {
              defs[i].resolve();
              ++i;
              syncTableUpload(i, table, defs);
            })
          }
        };

        var tableFields = $filter('filter')(formProperties, function(prop){
          return prop.type === 'table' || prop.type === 'fileHTML';
        });

        if(tableFields.length > 0) {
          for(var t=0; t<tableFields.length; t++) {
            var table = tableFields[t];
            /*
             in old service we need to check that we are saving new table or update old, so if id table from form field
             is equal attach id -> update, otherwise save as new. later it can be removed..
             */
            var isNotEqualsAttachments = function (table, num) {
              var checkForNewService = table.name.split(';');
              if (checkForNewService.length === 3 && checkForNewService[2].indexOf('bNew=true') > -1 || table.type === 'fileHTML') {
                def[num] = $q.defer();
                tablePromises[num] = {table:table, taskID:taskData.oProcess.nID, tableId:table.id, desc:null, isNew:true};
                tablePromisesReal[num] = def[num].promise;
              } else {
                var tableName = table.name.split(';')[0];
                def[num] = $q.defer();
                tablePromises[num] = {table:table, taskID:taskData.nID_Task, tableId:null, desc:tableName, isNew:false};
                tablePromisesReal[num] = def[num].promise;
              }
            };

            if(attachments.length > 0) {
              var theSameAttachments = attachments.filter(function (item) {
                var matchTableId = item.description.match(/(\[id=(\w+)\])/);
                var x = item.description.indexOf('[table]') !== -1 && matchTableId !== null;
                if(x) {
                  var name = matchTableId[2];
                  return name.toLowerCase() === table.id.toLowerCase()
                }
              });

              if(theSameAttachments.length !== 0) {
                theSameAttachments.map(function (a) {
                  var description = a.description.split('[')[0];
                  def[t] = $q.defer();
                  tablePromises[t] = {table:table, taskID:taskData.nID_Task, tableId:a.id, desc:description, isNew:false};
                  tablePromisesReal[t] = def[t].promise;
                });
              } else {
                isNotEqualsAttachments(table, t);
              }
            } else {
              isNotEqualsAttachments(table, t);
            }
          }
        }

        syncTableUpload(items, tablePromises, def);

        // upload files before form submitting
        promises.push(this.uploadTaskFiles(formProperties, taskData));
        var filesProm = $q.all(promises);
        var tableProm = $q.all(tablePromisesReal);

        $q.all([filesProm, tableProm]).then(function () {
          var taskServer = CurrentServer.getServer();
          var submitTaskFormData = {
            'taskId': taskData.nID_Task,
            'properties': createProperties(formProperties)
          };

          if (issue)
            submitTaskFormData.aProcessSubjectTask = issue;

          var req = {
            method: 'POST',
            url: '/api/tasks/action/task/saveForm',
            data: submitTaskFormData
          };

          if (taskServer.another)
            req.params = {taskServer: taskServer.name};

          simpleHttpPromise(req).then(function (result) {
              deferred.resolve(result);
            },
            function(result) {
              deferred.resolve(result);
            });
        });

        return deferred.promise;
      },

      upload: function(files, taskId, sID_Field, newUpload, formProperties, inputId, win) {
        var deferred = $q.defer();
        var self = this;
        var scope = $rootScope.$new(true, $rootScope);
        var url;
        var taskServer = CurrentServer.getServer();

        var field_id = 'sFile';

        function assignHeaderValue(oHeader, inputId, win) {
          if (inputId && win) {
            var headerId = (+inputId.split('-')[0].split('_')[1]) + 2;
            var headerName = inputId.split('_')[0] + "_" + headerId;
            if(win.document.getElementById(headerName)) {
              oHeader.value = win.document.getElementById(headerName).value;
            }
          }
        }

        function setFieldId(rows) {
          if (typeof sID_Field !== 'number') {
            if (rows.length > 1) {
              field_id += '_' + (rows.length);
            }
          }
          return field_id;
        }

        function findPlaceToSetFieldId(index) {
          var bIsIdSet = false;
          if (formProperties && formProperties[index] && formProperties[index].id
            && formProperties[index].id.indexOf('_') > 0) {
            var prefix = formProperties[index].id.split('_')[0];
            if (prefix) {
              angular.forEach(formProperties, function (form) {
                if (form.type === 'table' && form.id.split('_')[0] === prefix) {
                  var tableId = form.id.split('_')[1];
                  var rows = form.aRow;
                  var fields = rows[rows.length - 1];
                  angular.forEach(fields, function (table) {
                    angular.forEach(table, function (field) {
                      if (!bIsIdSet) {
                        if (field.id.split('_')[0] === 'oAttach' && field.type === 'file'
                          && field.id.split('_')[1] === tableId) {
                          sID_Field = setFieldId(rows);
                          bIsIdSet = true;
                        }
                      }
                    });
                  });
                }
              });
            }
          } else {
            var bIsUnderHTMLFormExists = false;
            if (formProperties) {
              for (var i = index + 1; i < formProperties.length; i++) {
                if (formProperties[i].type === 'table') {
                  bIsUnderHTMLFormExists = true;
                  var rows = formProperties[i].aRow;
                  var fields = rows[rows.length - 1];
                  angular.forEach(fields, function (table) {
                    angular.forEach(table, function (field) {
                      if (!bIsIdSet) {
                        if (field.type === 'file') {
                          sID_Field = setFieldId(rows);
                          bIsIdSet = true;
                        }
                      }
                    });
                  });
                }
              }
            }

            if (!bIsUnderHTMLFormExists && formProperties) {
              for (var i = index - 1; i > -1; i--) {
                if (formProperties[i].type === 'table') {
                  var rows = formProperties[i].aRow;
                  var fields = rows[rows.length - 1];
                  angular.forEach(fields, function (table) {
                    angular.forEach(table, function (field) {
                      if (!bIsIdSet) {
                        if (field.type === 'file') {
                          sID_Field = setFieldId(rows);
                          bIsIdSet = true;
                        }
                      }
                    });
                  });
                }
              }
            }
          }
        }

        function findProperEditor() {
          var occurences = 0;
          var properIndex;
          if (formProperties && tinymce.activeEditor){
            for (var index = 0; index < formProperties.length; index++) {
              var tinymceNumber = +tinymce.activeEditor.id.split('-')[2];
              if (formProperties[index].type === 'fileHTML') {
                occurences++;
                if(occurences === tinymceNumber) {
                  properIndex = index;
                  break;
                }
              }
            }
          }
          return properIndex;
        }

        function watchToSetAttachments(response, index) {
          var bIsUploaded = false;
          if (formProperties && formProperties[index] && formProperties[index].id && formProperties[index].id.indexOf('_') > 0) {
            var prefix = formProperties[index].id.split('_')[0];
            if (prefix) {
              angular.forEach(formProperties, function (form) {
                if (form.type === 'table' && form.id.split('_')[0] === prefix) {
                  var tableId = form.id.split('_')[1];
                  var rows = form.aRow;
                  var fields = rows[rows.length - 1];
                  angular.forEach(fields, function (table) {
                    angular.forEach(table, function (field) {
                      if (!bIsUploaded) {
                        if (field.id.split('_')[0] === 'oAttach' && field.type === 'file'
                          && field.id.split('_')[1] === tableId) {
                          if (field.value) {
                            TableService.addRow(form.id, formProperties);
                            fields = rows[rows.length - 1];
                            for (var f in fields) {
                              var ts = fields[f];
                              for (var t in ts) {
                                if (ts[t].id.split('_')[0] === 'oAttach' && ts[t].type === 'file'
                                  && ts[t].id.split('_')[1] === tableId) {
                                  ts[t].value = response;
                                  ts[t].sKey = JSON.parse(response).sKey;
                                  ts[t].storageType = JSON.parse(response).sID_StorageType;
                                  ts[t].fileName = JSON.parse(response).sFileNameAndExt;
                                  ts[t].isFromHTML = true;

                                  bIsUploaded = true;
                                }
                                if (ts[t].id.split('_')[0] === 'sAttachHeader' && ts[t].type === 'string'
                                  && ts[t].id.split('_')[1] === tableId) {
                                  assignHeaderValue(ts[t], inputId, win);
                                }
                              }
                            }
                          } else {
                            field.value = response;
                            field.sKey = JSON.parse(response).sKey;
                            field.storageType = JSON.parse(response).sID_StorageType;
                            field.fileName = JSON.parse(response).sFileNameAndExt;
                            field.isFromHTML = true;

                            bIsUploaded = true;
                          }
                        }
                        if (field.id.split('_')[0] === 'sAttachHeader' && field.value === '' && field.type === 'string'
                          && field.id.split('_')[1] === tableId) {
                          assignHeaderValue(field, inputId, win);
                        }
                      }
                    });
                  });
                }
              });
            }
          } else {
            var bIsUnderHTMLFormExists = false;
            if (formProperties){
              for (var i = index + 1; i < formProperties.length; i++) {
                if (formProperties[i].type === 'table') {
                  bIsUnderHTMLFormExists = true;
                  var rows = formProperties[i].aRow;
                  var fields = rows[rows.length - 1];
                  angular.forEach(fields, function (table) {
                    angular.forEach(table, function (field) {
                      if (!bIsUploaded) {
                        if (field.type === 'file') {
                          if (field.value) {
                            TableService.addRow(formProperties[i].id, formProperties);
                            fields = rows[rows.length - 1];
                            for (var f in fields) {
                              var ts = fields[f];
                              for (var t in ts) {
                                if (ts[t].type === 'file') {
                                  ts[t].value = response;
                                  ts[t].sKey = JSON.parse(response).sKey;
                                  ts[t].storageType = JSON.parse(response).sID_StorageType;
                                  ts[t].fileName = JSON.parse(response).sFileNameAndExt;
                                  ts[t].isFromHTML = true;

                                  bIsUploaded = true;
                                }
                                if (ts[t].type === 'string') {
                                  assignHeaderValue(ts[t], inputId, win);
                                }
                              }
                            }
                          } else {
                            field.value = response;
                            field.sKey = JSON.parse(response).sKey;
                            field.storageType = JSON.parse(response).sID_StorageType;
                            field.fileName = JSON.parse(response).sFileNameAndExt;
                            field.isFromHTML = true;

                            bIsUploaded = true;
                          }
                        }
                        if (field.type === 'string' && field.value === '') {
                          assignHeaderValue(field, inputId, win);
                        }
                      }
                    });
                  });
                }
              }
            }

            if (!bIsUnderHTMLFormExists && formProperties) {
              for (var i = index - 1; i > -1; i--) {
                var rows = formProperties[i].aRow;
                var fields = rows[rows.length - 1];
                angular.forEach(fields, function (table) {
                  angular.forEach(table, function (field) {
                    if (!bIsUploaded) {
                      if (field.type === 'file') {
                        if (field.value) {
                          TableService.addRow(formProperties[i].id, formProperties);
                          fields = rows[rows.length - 1];
                          for (var f in fields) {
                            var ts = fields[f];
                            for (var t in ts) {
                              if (ts[t].type === 'file') {
                                ts[t].value = response;
                                ts[t].sKey = JSON.parse(response).sKey;
                                ts[t].storageType = JSON.parse(response).sID_StorageType;
                                ts[t].fileName = JSON.parse(response).sFileNameAndExt;
                                ts[t].isFromHTML = true;

                                bIsUploaded = true;
                              }
                              if (ts[t].type === 'string') {
                                assignHeaderValue(ts[t], inputId, win);
                              }
                            }
                          }
                        } else {
                          field.value = response;
                          field.sKey = JSON.parse(response).sKey;
                          field.storageType = JSON.parse(response).sID_StorageType;
                          field.fileName = JSON.parse(response).sFileNameAndExt;
                          field.isFromHTML = true;

                          bIsUploaded = true;
                        }
                      }
                      if (field.type === 'string' && field.value === '') {
                        assignHeaderValue(field, inputId, win);
                      }
                    }
                  });
                });
              }
            }
          }
        }

        var properHTMLIndex = findProperEditor();

        if(sID_Field.indexOf('sFile_') !== 0) {
          findPlaceToSetFieldId(properHTMLIndex);
        }

        if(newUpload && taskId) {
          url = '/api/uploadfile?nID_Process=' + taskId + '&sID_Field=' + sID_Field + '&sFileNameAndExt=' + files[0].name.replace(new RegExp(/[№`~!@#$%^&*()|+=?;:'",<>\{\}\[\]\\\/]+/g), '');
        } else if(newUpload && !taskId) {
          url = '/api/uploadfile?sID_Field=' + sID_Field + '&sFileNameAndExt=' + files[0].name.replace(new RegExp(/[№`~!@#$%^&*()|+=?;:'",<>\{\}\[\]\\\/]+/g), '');
        } else if(sID_Field === 'sTextForm') {
          url = '/api/uploadfile?nID_Process=' + taskId + '&sFileNameAndExt=' + files[0].name.replace(new RegExp(/[№`~!@#$%^&*()|+=?;:'",<>\{\}\[\]\\\/]+/g), '');
        } else {
          url = '/api/tasks/' + taskId + '/attachments/' + sID_Field + '/upload';
        }

        if (taskServer.another) {
          url = url + '&taskServer=' + taskServer.name;
        }

        uiUploader.removeAll();
        uiUploader.addFiles(files);
        uiUploader.startUpload({
          url: url,
          concurrency: 1,
          onProgress: function (file) {
            if (tinymce && tinymce.activeEditor)
              tinymce.activeEditor.setMode('readonly');
            scope.$apply(function () {

            });
          },
          onCompleted: function (file, response) {

            watchToSetAttachments(response, properHTMLIndex);

            if (tinymce && tinymce.activeEditor)
              tinymce.activeEditor.setMode('design');
            scope.$apply(function () {
              /*
              try {
                deferred.resolve({
                  file: file,
                  response: JSON.parse(response)
                });
              } catch (e) {
                deferred.reject({
                  err: response
                });
              }
              */

              /*если это дашборд айгова, проверяем эцп при аплоаде, если айдок не проверяем.*/
              if ($rootScope.ProjectRegion_MainPage_bTasksOnly === 'TRUE') {
                checkFileSignFn();
              } else {
                deferred.resolve({
                  file: file,
                  response: JSON.parse(response),
                  signInfo: null
                });
              }

              function checkFileSignFn() {
                var oCheckSignReq = {};
                try{
                  oCheckSignReq = angular.fromJson(response);
                } catch (errParse){
                  if(self.value){
                    self.value.signInfo = null;
                  } else {
                    self.value = {
                      signInfo : null
                    }
                  }
                }
                if(oCheckSignReq.taskId && oCheckSignReq.id ||
                  oCheckSignReq.sKey && oCheckSignReq.sID_StorageType ||
                  oCheckSignReq.sID_Field && oCheckSignReq.sID_Process){

                  self.value = {id : oCheckSignReq.id ? oCheckSignReq.id : null, signInfo: null, fromDocuments: false};
                  var params = {url:null, query:null};

                  if(oCheckSignReq.sKey && oCheckSignReq.sID_StorageType){
                    params.url = '/api/tasks/sign/checkAttachmentSignNew';
                    params.query = {
                      sID_StorageType: oCheckSignReq.sID_StorageType,
                      sKey: oCheckSignReq.sKey,
                      sID_Process: oCheckSignReq.sID_Process,
                      sID_Field: oCheckSignReq.sID_Field,
                      sFileNameAndExt: oCheckSignReq.sFileNameAndExt
                    }
                  } else {
                    params.url = '/api/tasks/' + oCheckSignReq.taskId + '/attachments/' + oCheckSignReq.id + '/checkAttachmentSign';
                  }

                  if (taskServer.another)
                    params.query.taskServer = taskServer.name;

                  simpleHttpPromise({
                      method: 'GET',
                      url: params.url,
                      params: params.query
                    }
                  ).then(function (signInfo) {
                    //self.value.signInfo = Object.keys(signInfo).length === 0 ? null : signInfo;
                    try {
                      deferred.resolve({
                        file: file,
                        response: JSON.parse(response),
                        signInfo: Object.keys(signInfo).length === 0 ? null : signInfo
                      });
                    } catch (e) {
                      deferred.reject({
                        err: response
                      });
                    }
                  }, function (err) {
                    if(self.value){
                      self.value.signInfo = null;
                    } else {
                      self.value = {
                        signInfo : null
                      }
                    }
                  })
                }
              }
            });
          }
        });
        return deferred.promise;
      },

      signTasks: function(aTasksForSigning) {
        var self = this;
        var mainDeferred = $q.defer();

        if(!angular.isArray(aTasksForSigning)){
          mainDeferred.reject("Вхідний об'єкт не є масивом");
        }

        function getContentsForSigning(tasks) {
          var deferred = $q.defer();
          getDocumentImages(tasks).then(function (documents) {
            getTaskAttachments(tasks).then(function (attachments) {
              if(angular.isArray(documents) && documents.length > 0){
                angular.forEach(attachments, function (attach) {
                  documents.push(attach);
                });
                deferred.resolve(documents);
              } else {
                deferred.resolve(attachments);
              }
            }, function (error) {
              deferred.reject(error);
            });
          }, function (error) {
            deferred.reject(error);
          });
          return deferred.promise;
        }

        function getDocumentImages(aTasks) {
          var deferred = $q.defer();
          var aTasksWithDocumentImage = aTasks.filter(function (task) {
            return task.hasOwnProperty('globalVariables') && task['globalVariables'].hasOwnProperty('sKey_Step_Document');
          });

          var aParamsForGettingDocumentImage = [];
          angular.forEach(aTasksWithDocumentImage, function (task) {
            aParamsForGettingDocumentImage.push({
              processInstanceId: task.processInstanceId,
              sKey_Step: task.globalVariables.sKey_Step_Document
            })
          });
          var documentsForSignin = [];
          self.getDocumentImagesAsBase64(aParamsForGettingDocumentImage).then(function (result) {
            documentsForSignin = result.filter(function (oDoc) {
              if (oDoc.hasOwnProperty('oDocument')) {
                try {
                  oDoc['oDocument'] = angular.fromJson(oDoc['oDocument']);
                } catch (e) {

                }
                return !(oDoc['oDocument'].hasOwnProperty('code') && oDoc['oDocument'].hasOwnProperty('message'));
              }
              return false;
            });

            deferred.resolve(documentsForSignin);
          }, function (error) {
            deferred.reject(error);
          });
          return deferred.promise;
        }

        function getTaskAttachments(aTasks) {
          var deferred = $q.defer();
          var attachments = [];
          angular.forEach(aTasks, function (task){
            if(task.hasOwnProperty('globalVariables')){
              angular.forEach(task['globalVariables'], function (field, key) {
                if(angular.isObject(field) &&
                  field.hasOwnProperty('bSigned') && field.hasOwnProperty('sID_StorageType') &&
                  field.hasOwnProperty('aAttribute') && field.hasOwnProperty('sContentType') &&
                  field.hasOwnProperty('sFileNameAndExt') && field.hasOwnProperty('sKey')){

                  attachments.push({
                    bSigned: field.bSigned,
                    sID_StorageType: field.sID_StorageType,
                    aAttribute: field.aAttribute,
                    sContentType: field.sContentType,
                    sFileNameAndExt: field.sFileNameAndExt,
                    sID_Field: key,
                    sKey: field.sKey,
                    nID_Process: task.processInstanceId
                  });

                }
              });
            }
          });

          self.getAttachmentsAsBase64(attachments).then(function (result) {
            deferred.resolve(result);
          }, function (error) {
            deferred.reject(error);
          });

          return deferred.promise;

        }

        getContentsForSigning(aTasksForSigning).then(function (result) {

          if(!angular.isArray(result) || result.length < 1){
            mainDeferred.reject('Відсутній контент для накладання ЕЦП');
          }

          angular.forEach(result, function (el) {
            if(el.hasOwnProperty('oDocument') && el.oDocument.hasOwnProperty('base64')){
              el.content = el.oDocument.base64;
            }
            if(el.hasOwnProperty('oAttachment') && el.oAttachment.hasOwnProperty('base64')){
              el.content = el.oAttachment.base64;
            }
          });

          result.base64encoded = true;

          signDialog.signContentsArray(result,
            function (signedContents) {

              self.uploadSignedContents(signedContents).then(function (responce) {
                var message = (signedContents.length && signedContents.length < 2 ? 'Документ' : 'Документи') + ' успішно підписано ЕЦП';
                mainDeferred.resolve(message);
              }, function (error) {
                mainDeferred.reject(error);
              });

            }, function () {
              console.log('Sign Dismissed');
              mainDeferred.resolve();
            }, function (error) {
              mainDeferred.reject(error);
            }, 'ng-on-top-of-modal-dialog modal-info');

        }, function (error) {
          mainDeferred.reject(error);
        });

        return mainDeferred.promise;
      },

      uploadSignedContents: function (contents) {
        return simpleHttpPromise({
            method: 'POST',
            url: '/api/tasks/pluralUploading',
            data: contents
          }
        );
      },

      uploadAttachToTaskForm: function (content, taskForm, processId, taskId, inputId, win) {
        var deferred = $q.defer();

        var isNewAttachmentService = false;

        var taskID = taskId;
        if(content.fieldId === 'sFileFromHTML') {
          isNewAttachmentService = true;
          taskID = processId;
        }
        for (var i = 0; i < taskForm.length; i++) {
          var item = taskForm[i];
          var splitNameForOptions = item.name.split(';');
          if (item.type !== 'table' && item.id === content.fieldId && splitNameForOptions.length === 3) {
            if (splitNameForOptions[2].indexOf('bNew=true') !== -1) {
              isNewAttachmentService = true;
              taskID = processId;
              break
            }
          } else if (item.type === 'table') {
            if (item.aRow.length !== 0) {
              for (var t = 0; t < item.aRow.length; t++) {
                var row = item.aRow[t];
                for (var f = 0; f < row.aField.length; f++) {
                  var field = row.aField[f];
                  var fieldOptions = field.name.split(';');
                  if (field.id === content.fieldId && fieldOptions.length === 3) {
                    if (fieldOptions[2].indexOf('bNew=true') !== -1) {
                      isNewAttachmentService = true;
                      taskID = processId;
                      break
                    }
                  }
                }
              }
            }
          }
        }
        this.upload(content.files, taskID, content.fieldId, isNewAttachmentService, taskForm, inputId, win)
          .then(function (result) {
          var filterResult = taskForm.filter(function (property) {
            return property.id === content.fieldId;
          });

          // if filterResult === 0 => check file in table
          if (filterResult.length === 0) {
            for (var j = 0; j < taskForm.length; j++) {
              if (taskForm[j].type === 'table') {
                for (var c = 0; c < taskForm[j].aRow.length; c++) {
                  var row = taskForm[j].aRow[c];
                  for (var i = 0; i < row.aField.length; i++) {
                    if (row.aField[i].id === content.fieldId) {
                      filterResult.push(row.aField[i]);
                      break
                    }
                  }
                }
              }
            }
          }

          if (filterResult && filterResult.length === 1) {
            if (result.response.sKey) {
              filterResult[0].value = JSON.stringify(result.response);
              filterResult[0].fileName = result.response.sFileNameAndExt;
              filterResult[0].signInfo = result.signInfo;
            } else {
              filterResult[0].value = result.response.id;
              filterResult[0].fileName = result.response.name;
              filterResult[0].signInfo = result.signInfo;
            }
          }

          deferred.resolve(result);
        }, function (err) {

          deferred.reject(err);
        });

        return deferred.promise;
      },

      uploadAttachmentsToTaskForm: function (aContents, taskForm, processId, taskId) {
        var deferred = $q.defer();
        var self = this;

        var uploadPromises = [],
          contents = [],
          documentPromises = [],
          docDefer = [],
          counter = 0;

        angular.forEach(aContents, function (oContent, key) {
          docDefer[key] = $q.defer();
          contents[key] = oContent;
          documentPromises[key] = docDefer[key].promise;
        });

        var uplaadingResult = [];

        var asyncUpload = function (i, docs, defs) {
          if (i < docs.length) {

            return self.uploadAttachToTaskForm(docs[i], taskForm, processId, taskId).then(function (resp) {
              uplaadingResult.push(resp);
              defs[i].resolve(resp);
              return asyncUpload(i + 1, docs, defs);
            }, function (err) {
              uplaadingResult.push({error : err});
              defs[i].reject(err);
              return asyncUpload(i + 1, docs, defs);
            });

          }
        };

        var first = $q.all(uploadPromises).then(function () {
          return asyncUpload(counter, contents, docDefer);
        });

        $q.all([first, documentPromises]).then(function () {
          deferred.resolve(uplaadingResult);
        });

        return deferred.promise;
      },

      setDocumentImages: function (properties) {
        var deferred = $q.defer();

        var uploadPromises = [],
          contents = [],
          documentPromises = [],
          docDefer = [],
          counter = 0;

        angular.forEach(properties.signedContents, function (oContent, key) {
          docDefer[key] = $q.defer();
          contents[key] = {
            data : oContent
          };
          documentPromises[key] = docDefer[key].promise;
        });

        var uplaadingResult = [];

        var asyncDocumentUpload = function (i, docs, defs) {
          if (i < docs.length) {

            return $http.post('/api/tasks/' + properties.taskId + '/setDocumentImage', {
              bSigned: true,
              sFileNameAndExt: docs[i].data.id + '.pdf',
              sID_Field: docs[i].data.id,
              sContentType: 'application/pdf',
              sKey_Step: properties.sKey_Step,
              sContent: docs[i].data.sign
            }).then(function (resp) {
              uplaadingResult.push(resp);
              defs[i].resolve(resp);
              return asyncDocumentUpload(i + 1, docs, defs);
            }, function (err) {
              uplaadingResult.push({error : err});
              defs[i].reject(err);
              return asyncDocumentUpload(i + 1, docs, defs);
            });

          }
        };

        var first = $q.all(uploadPromises).then(function () {
          return asyncDocumentUpload(counter, contents, docDefer);
        });

        $q.all([first, documentPromises]).then(function () {
          deferred.resolve(uplaadingResult);
        });

        return deferred.promise;

      },

      generatePDFFromPrintForms: function (formProperties, taskData) {
        var filesFields = [];

        // нужно найти все поля с тимом "file" и id, начинающимся с "PrintForm_"
        var filesFieldsTemp = $filter('filter')(formProperties, function (prop) {
          return prop.type === 'file' && (prop.options.hasOwnProperty('sID_Field_Printform_ForECP') || /^PrintForm_/.test(prop.id));
        });

        // отфильтровываем только те поля файлов, ПринтФормы связанные принтформы которых имеют опцию bThisOnlyECP=true
        var printFormsTemplates = $filter('filter')(formProperties, function (prop) {
          return prop.printFormLinkedToFileField && (prop.options && prop.options.bThisOnlyECP && prop.options.bThisOnlyECP === true);
        });
        if(printFormsTemplates && printFormsTemplates.length > 0){
          angular.forEach(printFormsTemplates, function (pfField) {
            angular.forEach(filesFieldsTemp, function (fField) {
              if(pfField.printFormLinkedToFileField === fField.id){
                filesFields.push(fField);
              }
            })
          })
        } else {
          filesFields = filesFieldsTemp;
        }

        var self = this;
        var deferred = $q.defer();
        var filesDefers = [];
        // загрузить все шаблоны
        angular.forEach(filesFields, function (fileField) {
          var defer = $q.defer();
          filesDefers.push(defer.promise);
          var patternFileName = fileField.options.sPatternFileUrl ? fileField.options.sPatternFileUrl : fileField.name.split(';')[2];
          if(fileField.value){
            var fv = angular.fromJson(fileField.value);
            var keyOrProcessID;
            var typeOrAttachID;
            if(fv.sID_StorageType && fv.sID_StorageType === 'Mongo'){
              keyOrProcessID = fv.sKey;
              typeOrAttachID = fv.sID_StorageType;
            } else {
              keyOrProcessID = taskData.oProcess.nID;
              typeOrAttachID = fileField.id;
            }
            self.getTableOrFileAttachment(keyOrProcessID, typeOrAttachID, true, true).then(function (response) {
              var result = angular.fromJson(response);
              defer.resolve({
                fileField: fileField,
                fileBase64: result.base64,
                template: ''
              });
            })
          } else if (patternFileName) {
            patternFileName = patternFileName.replace(/^pattern\//, '');
            self.getPatternFile(patternFileName).then(function (result) {
              defer.resolve({
                fileField: fileField,
                fileBase64: null,
                template: result
              });
            });
          } else
            defer.resolve({
              fileField: fileField,
              fileBase64: null,
              template: ''
            });
        });
        var isDocPrintFormPresent = !formProperties.sendDefaultPrintForm;
        angular.forEach(formProperties, function (field) {
          if(field.id.match(/^PrintForm_/) && field.options.sPrintFormFileAsPDF){
            var printFormName = field.options.sPrintFormFileAsPDF.split('/');
            var ind = printFormName.length - 1 < 0 ? 0 : printFormName.length - 1;
            if(printFormName[ind].match(/^_doc_/)){
              isDocPrintFormPresent = true;
            }
          }
        });
        if(formProperties.sendDefaultPrintForm && !isDocPrintFormPresent && filesDefers.length === 0){
          filesDefers.push($q.resolve({
            fileField: null,
            template: '<html><head><meta charset="utf-8"><link rel="stylesheet" type="text/css" href="style.css" /></head><body">' + $(".ng-modal-dialog-content")[0].innerHTML + '</html>'
          }));
        }
        // компиляция и отправка html
        $q.all(filesDefers).then(function (results) {
          var uploadPromises = [],
            printforms = [],
            printPromises = [],
            printDefer = [],
            counter = 0;
          var sKey_Step_field = formProperties.filter(function (item) {
            return item.id === "sKey_Step_Document";
          })[0];
          if(sKey_Step_field){
            var sKey_Step = sKey_Step_field.value
          }

          angular.forEach(results, function (templateResult, key) {


            if(!templateResult.fileBase64){
              var scope = $rootScope.$new();
              //scope.selectedTask = task;
              scope.taskData = taskData;
              scope.taskForm = formProperties;
              //scope.getPrintTemplate = function(){return PrintTemplateProcessor.getPrintTemplate(task, formProperties, templateResult.template, scope.lunaService);},
              scope.getPrintTemplate = function () {
                return PrintTemplateProcessor.getPrintTemplate(formProperties, templateResult.template);
              };
              scope.containsPrintTemplate = function () {
                return templateResult.template !== '';
              };
              scope.getProcessName = processes.getProcessName;
              scope.sDateShort = function (sDateLong) {
                if (sDateLong !== null) {
                  var o = new Date(sDateLong);
                  return o.getFullYear() + '-' + ((o.getMonth() + 1) > 9 ? '' : '0') + (o.getMonth() + 1) + '-' + (o.getDate() > 9 ? '' : '0') + o.getDate() + ' ' + (o.getHours() > 9 ? '' : '0') + o.getHours() + ':' + (o.getMinutes() > 9 ? '' : '0') + o.getMinutes();
                }
              };
              scope.sFieldLabel = function (sField) {
                var s = '';
                if (sField !== null) {
                  var a = sField.split(';');
                  s = a[0].trim();
                }
                return s;
              };
              scope.sEnumValue = function (aItem, sID) {
                var s = sID;
                _.forEach(aItem, function (oItem) {
                  if (oItem.id == sID) {
                    s = oItem.name;
                  }
                });
                return s;
              };
              var compiled = $compile('<print-dialog></print-dialog>')(scope);

              /**
               * https://github.com/e-government-ua/i/issues/1382
               * parse name string property to get file names sPrintFormFileAsPDF and sPrintFormFileAsIs
               */
              var fileName = null;
              var fileNameTemp = null;
              var sFileFieldID = null;
              var sOutputFileType = null;
              var html = null;


              if(templateResult.fileField) {
                if (typeof templateResult.fileField.name === 'string') {
                  fileNameTemp = templateResult.fileField.name.split(/;/).reduce(function (prev, current) {
                    var reduceResult = prev += current.match(/sPrintFormFileAsPDF/i) || current.match(/sPrintFormFileAsIs/i) || [];
                    if (reduceResult !== '') {
                      var parts = current.split(',');
                      angular.forEach(parts, function (el) {
                        if (el.match(/^sFileName=/)) {
                          fileName = el.split('=')[1];
                        }
                      })
                    }
                    return reduceResult;
                  }, '');

                  fileName = fileName || fileNameTemp;

                  if (fileNameTemp === 'sPrintFormFileAsPDF') {
                    fileName = fileName + '.pdf';
                    sOutputFileType = 'pdf';
                    if(templateResult.fileField.options.sPrintFormFileAsPDF){
                      var printFormName = templateResult.fileField.options.sPrintFormFileAsPDF.split('/');
                      var ind = printFormName.length - 1 < 0 ? 0 : printFormName.length - 1;
                      var sProcessDefinitionId = taskData.oProcess.sBP;
                      if(printFormName[ind].match(/^_doc_/)&& sProcessDefinitionId.match(/^_doc_/)){
                        formProperties.isSendAsDocument = true;
                        formProperties.skipSendingPrintForm = true;
                      } else {
                        formProperties.isSendAsDocument = false;
                      }
                    }
                  }

                  if (fileNameTemp === 'sPrintFormFileAsIs') {
                    fileName = fileName + '.html';
                    sOutputFileType = 'html';
                    formProperties.isSendAsDocument = false;
                  }

                  sFileFieldID = templateResult.fileField.id;
                }
                var description = templateResult.fileField.name.split(";")[0];
              } else {
                sOutputFileType = 'pdf';
                fileName = 'form.pdf';
                html = templateResult.template;
              }

              uploadPromises.push($timeout(function(){
                if(!html){
                  html = '<html><head><meta charset="utf-8"></head><body>' + compiled.find('.print-modal-content').html() + '</body></html>';
                }
                var data = {
                  sDescription: description,
                  sFileNameAndExt: fileName || 'User form.html',
                  sID_Field: sFileFieldID,
                  sContent: html,
                  sOutputFileType: sOutputFileType,
                  sKey_Step: sKey_Step,
                  isSendAsDocument: formProperties.sendDefaultPrintForm || formProperties.isSendAsDocument,
                  skipSendingPrintForm: formProperties.skipSendingPrintForm
                };

                printDefer[key] = $q.defer();
                printforms[key] = {html:html, data:data};
                printPromises[key] = printDefer[key].promise;
              }));
            } else {
              uploadPromises.push($timeout(function(){
                var data = {
                  sDescription: description,
                  sFileNameAndExt: fileName || 'User form.html',
                  sID_Field: templateResult.fileField.id,
                  sContent: templateResult.fileBase64,
                  sOutputFileType: sOutputFileType,
                  sKey_Step: sKey_Step,
                  isSendAsDocument: formProperties.sendDefaultPrintForm || formProperties.isSendAsDocument,
                  skipSendingPrintForm: formProperties.skipSendingPrintForm
                };

                printDefer[key] = $q.defer();
                printforms[key] = {html:null, data:data};
                printPromises[key] = printDefer[key].promise;
              }))
            }

          });
//
          var resultsPdf = [];

          var asyncPdfGenerate = function (i, print, defs) {
            if (i < print.length) {
              if(!print[i].data.sID_Field && print[i].data.skipSendingPrintForm){
                defs[i].resolve();
                return asyncPdfGenerate(i+1, print, defs);
              } else {
                var printContents = print[i].html;
                if(printContents){
                  return generationService.generatePDFFromHTML(printContents).then(function (pdfContent) {
                    resultsPdf.push({
                      id: print[i].data.sID_Field,
                      content: pdfContent.base64
                    });
                    defs[i].resolve();
                    return asyncPdfGenerate(i + 1, print, defs);
                  })
                } else {
                  resultsPdf.push({
                    id: print[i].data.sID_Field,
                    content: print[i].data.sContent
                  });
                  defs[i].resolve();
                  return asyncPdfGenerate(i + 1, print, defs);
                }

              }
            }
          };

          var first = $q.all(uploadPromises).then(function () {
            return asyncPdfGenerate(counter, printforms, printDefer);
          });

          $q.all([first, printPromises]).then(function (uploadResults) {
            deferred.resolve(resultsPdf);
          });

        });

        return deferred.promise;
      },

      /**
       * Ф-ция загрузки файлов из принт-диалога в виде аттачей к форме
       * @param formProperties
       * @param taskData
       * @returns {deferred.promise|{then, always}}
       */
      uploadTaskFiles: function (formProperties, taskData) {
        // нужно найти все поля с тимом "file" и id, начинающимся с "PrintForm_"
        var filesFields = $filter('filter')(formProperties, function (prop) {
          return prop.type == 'file' && /^PrintForm_/.test(prop.id);
        });

        var self = this;
        var deferred = $q.defer();
        var filesDefers = [];
        // загрузить все шаблоны
        angular.forEach(filesFields, function (fileField) {
          var defer = $q.defer();
          filesDefers.push(defer.promise);
          var patternFileName = fileField.name.split(';')[2];
          if (patternFileName) {
            patternFileName = patternFileName.replace(/^pattern\//, '');
            self.getPatternFile(patternFileName).then(function (result) {
              defer.resolve({
                fileField: fileField,
                template: result
              });
            });
          } else
            defer.resolve({
              fileField: fileField,
              template: ''
            });
        });
        var isDocPrintFormPresent = !formProperties.sendDefaultPrintForm;
        angular.forEach(formProperties, function (field) {
          if(field.id.match(/^PrintForm_/) && field.options.sPrintFormFileAsPDF){
            var printFormName = field.options.sPrintFormFileAsPDF.split('/');
            var ind = printFormName.length - 1 < 0 ? 0 : printFormName.length - 1;
            if(printFormName[ind].match(/^_doc_/)){
              isDocPrintFormPresent = true;
            }
          }
        });
        if(formProperties.sendDefaultPrintForm && !isDocPrintFormPresent){
          filesDefers.push($q.resolve({
            fileField: null,
            template: '<html><head><meta charset="utf-8"><link rel="stylesheet" type="text/css" href="style.css" /></head><body">' + $(".ng-modal-dialog-content")[0].innerHTML + '</html>'
          }));
        }
        // компиляция и отправка html
        $q.all(filesDefers).then(function (results) {
          var uploadPromises = [],
            printforms = [],
            printPromises = [],
            printDefer = [],
            counter = 0;

          angular.forEach(results, function (templateResult, key) {
            var scope = $rootScope.$new();
            //scope.selectedTask = task;
            scope.taskData = taskData;
            scope.taskForm = formProperties;
            //scope.getPrintTemplate = function(){return PrintTemplateProcessor.getPrintTemplate(task, formProperties, templateResult.template, scope.lunaService);},
            scope.getPrintTemplate = function () {
              return PrintTemplateProcessor.getPrintTemplate(formProperties, templateResult.template);
            };
            scope.containsPrintTemplate = function () {
              return templateResult.template != '';
            };
            scope.getProcessName = processes.getProcessName;
            scope.sDateShort = function (sDateLong) {
              if (sDateLong !== null) {
                var o = new Date(sDateLong);
                return o.getFullYear() + '-' + ((o.getMonth() + 1) > 9 ? '' : '0') + (o.getMonth() + 1) + '-' + (o.getDate() > 9 ? '' : '0') + o.getDate() + ' ' + (o.getHours() > 9 ? '' : '0') + o.getHours() + ':' + (o.getMinutes() > 9 ? '' : '0') + o.getMinutes();
              }
            };
            scope.sFieldLabel = function (sField) {
              var s = '';
              if (sField !== null) {
                var a = sField.split(';');
                s = a[0].trim();
              }
              return s;
            };
            scope.sEnumValue = function (aItem, sID) {
              var s = sID;
              _.forEach(aItem, function (oItem) {
                if (oItem.id == sID) {
                  s = oItem.name;
                }
              });
              return s;
            };
            var compiled = $compile('<print-dialog></print-dialog>')(scope);

            /**
             * https://github.com/e-government-ua/i/issues/1382
             * parse name string property to get file names sPrintFormFileAsPDF and sPrintFormFileAsIs
             */
            var fileName = null;
            var fileNameTemp = null;
            var sFileFieldID = null;
            var sOutputFileType = null;
            var html = null;
            var taskServer = CurrentServer.getServer();
            var sKey_Step_field = formProperties.filter(function (item) {
              return item.id === "sKey_Step_Document";
            })[0];
            if(sKey_Step_field){
              var sKey_Step = sKey_Step_field.value
            }

            if(templateResult.fileField) {
              if (typeof templateResult.fileField.name === 'string') {
                fileNameTemp = templateResult.fileField.name.split(/;/).reduce(function (prev, current) {
                  var reduceResult = prev += current.match(/sPrintFormFileAsPDF/i) || current.match(/sPrintFormFileAsIs/i) || [];
                  if (reduceResult !== '') {
                    var parts = current.split(',');
                    angular.forEach(parts, function (el) {
                      if (el.match(/^sFileName=/)) {
                        fileName = el.split('=')[1];
                      }
                    })
                  }
                  return reduceResult;
                }, '');

                fileName = fileName || fileNameTemp;

                if (fileNameTemp === 'sPrintFormFileAsPDF') {
                  fileName = fileName + '.pdf';
                  sOutputFileType = 'pdf';
                  if(templateResult.fileField.options.sPrintFormFileAsPDF){
                    var printFormName = templateResult.fileField.options.sPrintFormFileAsPDF.split('/');
                    var ind = printFormName.length - 1 < 0 ? 0 : printFormName.length - 1;
                    if(printFormName[ind].match(/^_doc_/)&& taskData.oProcess.sBP.match(/^_doc_/)){
                      formProperties.isSendAsDocument = true;
                      formProperties.skipSendingPrintForm = true;
                    } else {
                      formProperties.isSendAsDocument = false;
                    }
                  }
                }

                if (fileNameTemp === 'sPrintFormFileAsIs') {
                  fileName = fileName + '.html';
                  sOutputFileType = 'html';
                  formProperties.isSendAsDocument = false;
                }

                sFileFieldID = templateResult.fileField.id;
              }
              var description = templateResult.fileField.name.split(";")[0];
            } else {
              sOutputFileType = 'pdf';
              fileName = 'form.pdf';
              html = templateResult.template;
            }

            uploadPromises.push($timeout(function(){
              if(!html){
                html = '<html><head><meta charset="utf-8"></head><body>' + compiled.find('.print-modal-content').html() + '</body></html>';
              }
              var data = {
                sDescription: description,
                sFileNameAndExt: fileName || 'User form.html',
                sID_Field: sFileFieldID,
                sContent: html,
                sOutputFileType: sOutputFileType,
                sKey_Step: sKey_Step,
                isSendAsDocument: formProperties.sendDefaultPrintForm || formProperties.isSendAsDocument,
                skipSendingPrintForm: formProperties.skipSendingPrintForm
              };

              if (taskServer.another)
                data.taskServer = taskServer.name;

              printDefer[key] = $q.defer();
              printforms[key] = {html:html, data:data};
              printPromises[key] = printDefer[key].promise;
            }));

          });

          var asyncPrintUpload = function (i, print, defs) {
            if (i < print.length) {
              if(!print[i].data.sID_Field && print[i].data.skipSendingPrintForm){
                defs[i].resolve();
                return asyncPrintUpload(i+1, print, defs);
              } else {
                return $http.post('/api/tasks/' + taskData.oProcess.nID + '/upload_content_as_attachment', print[i].data)
                  .then(function (uploadResult) {
                    if(results[i].fileField && results[i].fileField.value){
                      results[i].fileField.value = uploadResult.data;
                    } else {
                      results[i]['uploadDefaultPrintForm'] = uploadResult.data;
                    }
                    defs[i].resolve();
                    return asyncPrintUpload(i+1, print, defs);
                  });
              }
            }
          };

          var first = $q.all(uploadPromises).then(function () {
            return asyncPrintUpload(counter, printforms, printDefer);
          });

          $q.all([first, printPromises]).then(function (uploadResults) {
            deferred.resolve();
          });

        });

        return deferred.promise;
      },

      getActivitiTaskObject: function (stateId) {
        var self = this;
        tasksStateModel.sID_Order = stateId;
        if ($stateParams.type == 'finished' || $stateParams.type == 'docHistory'){
          var defer = $q.defer();
          self.taskFormFromHistory($stateParams.id).then(function(response){
            defer.resolve(JSON.parse(response).data[0]);
          }, defer.reject);
          return defer.promise;
        }
        else {
          return self.getTask(stateId);
        }
      },

      getTask: function (taskId) {
        var deferred = $q.defer();

        var req = {
          method: 'GET',
          url: '/api/tasks/' + taskId,
          data: {}
        };

        $http(req).success(function (data) {
          deferred.resolve(data);
        }).error(function (err) {
          deferred.reject(err);
        }.bind(this));

        return deferred.promise;
      },
      getTasksByOrder: function (nID_Order, bNoShowErrorWindow) {
        var taskServer = CurrentServer.getServer();
        return simpleHttpPromise({
            method: 'GET',
            url: '/api/tasks/search/byOrder/' + nID_Order,
            params: {
              bNoShowErrorWindow: bNoShowErrorWindow,
              taskServer: taskServer.another ? taskServer.name : null
            }
          }
        );
      },
      getTasksByText: function (sFind, sType) {
        return simpleHttpPromise({
            method: 'GET',
            url: '/api/tasks/search/byText/' + sFind + "/type/" + sType
          }
        );
      },
      getProcesses: function (sID) {
        return simpleHttpPromise({
          method: 'GET',
          url: '/api/tasks/getProcesses',
          params: {
            sID: sID
          }
        });
      },
      getPatternFile: function (sPathFile) {
        return simpleHttpPromise({
            method: 'GET',
            url: '/api/tasks/getPatternFile?sPathFile=' + sPathFile
          }
        );
      },
      setTaskQuestions: function (params) {
        return simpleHttpPromise({
            method: 'POST',
            url: '/api/tasks/setTaskQuestions',
            data: params
          }
        );
      },
      postServiceMessages: function (params) {
        return simpleHttpPromise({
          method: 'POST',
          url: 'api/tasks/postServiceMessages',
          data: params
        })
      },
      checkAttachmentSign: function (nID_Task, nID_Attach, isNewService) {
        var taskServer = CurrentServer.getServer();

        if(isNewService) {
          return simpleHttpPromise({
            method: 'GET',
            url: '/api/tasks/sign/checkAttachmentSignNew',
            params: {
              sKey:nID_Task,
              sID_StorageType:nID_Attach,
              taskServer: taskServer.another ? taskServer.name : null
            }
          })
        } else {
          // old ecp check service, remove it later. Now checkAttachmentSignNew is new service.
          return simpleHttpPromise({
              method: 'GET',
              url: '/api/tasks/' + nID_Task + '/attachments/' + nID_Attach + '/checkAttachmentSign',
              params: {taskServer: taskServer.another ? taskServer.name : null}
            }
          );
        }
      },
      unassign: function (nID_Task) {
        return simpleHttpPromise({
            method: 'PUT',
            url: '/api/tasks/' + nID_Task + '/unassign'
          }
        );
      },
      getTaskData: function (params, allData, server) {
        var requestParams = angular.copy(params);
        var serverData = CurrentServer.getServer();
        if (serverData && serverData.another) {
          requestParams.taskServer = serverData.name;
        }
        if (allData === true)
          angular.merge(requestParams, {
            bIncludeGroups: true,
            bIncludeStartForm: true,
            bIncludeAttachments: true,
            bIncludeProcessVariables: true,
            bIncludeMessages: true
          });
        return simpleHttpPromise({
            method: 'GET',
            url: '/api/tasks/getTaskData',
            params: requestParams
          }
        ).then(function (data) {
          // Костыль. Удалить когда будет приходить массив вместо строки
          if (angular.isString(data.aMessage))
            data.aMessage = JSON.parse(data.aMessage);
          angular.forEach(data.aMessage, function (message) {
            if (angular.isString(message.sData) && message.sData.length > 1) {
              try {
                message.osData = JSON.parse(message.sData);
              } catch (e) {
                message.osData = {};
              }
            }
          });
          return data;
        });
      },
      /**
       * Реализовать открытие по урл-у "расширенного профиля задачи" и ссылку для админа из "обычного профиля" #1015
       * @param taskData
       * @returns {boolean}
       */
      isFullProfileAvailableForCurrentUser: function (taskData) {
        var currentUser = Auth.getCurrentUser();
        // 4.1) отображать тем, кто входит в группу: admin,super-admin
        if (currentUser.roles.indexOf('admin') || currentUser.roles.indexOf('super_admin'))
          return true;
        // 4.2) а также тем на кого эта таска ассйнута
        if (taskData.sLoginAssigned == currentUser.id)
          return true;
        // 4.3) а так-же тем, кто входит в группу, в которую входит эта таска и одновременно - когда она не ассайнута
        // или когда он входит в группу manager и она ассайнута на другого т.е.
        // (входит в группу, в которую входит эта таска) && (она не ассайнута || (он входит в группу manager && она ассайнута на другого))
        var groups = $.grep(taskData.aGroup || taskData.aGroup, function (group) {
          return currentUser.roles.indexOf(group) > -1;
        });
        if (groups.length > 0 && (!taskData.sLoginAssigned || currentUser.roles.indexOf('manager') > -1)) {
          return true;
        }
        return false;
      },
      isUserHasDocuments: function (login) {
        return simpleHttpPromise({
          method: 'GET',
          url: '/api/documents/getBPs_ForReferent',
          params: {
            sLogin: login
          }
        })
      },
      createNewDocument: function (bpID) {
        return simpleHttpPromise({
          method: 'GET',
          url: 'api/documents/setDocument',
          params: {
            sID_BP: bpID
          }
        })
      },
      /*вытягиваем шаблон бизнес-процесса для последующего заполнения*/
      createNewTask: function (bpID) {
        return simpleHttpPromise({
          method: 'GET',
          url: 'api/create-task/createTask',
          params: {
            sID_BP: bpID
          }
        })
      },
      /*сохраняем ранее заполненный шаблон как таску, сохранив перед этим таблицы в редис*/
      submitNewCreatedTask: function (task, bpID, issue) {
        var self = this, def = [], tables = [], tablePromises = [], items = 0, deferred = $q.defer();

        var createProperties = function (formProperties) {
          var properties = [];
          for (var i = 0; i < formProperties.length; i++) {
            var formProperty = formProperties[i];
            if (formProperty && formProperty.writable) {
              if(formProperty.hasOwnProperty('aRow')) {

              }
              properties.push({
                id: formProperty.id,
                value: formProperty.value
              });
            }
          }
          return properties;
        };

        var tableFields = $filter('filter')(task.aFormProperty, function(prop){
          return prop.type == 'table';
        });

        var syncTableUpload = function (i, table, defs) {
          if (i < table.length) {
            self.uploadAttachment(table[i].table, table[i].taskID, table[i].tableId, table[i].desc, table[i].isNew)
              .then(function(resp) {
                defs[i].resolve();
                ++i;
                syncTableUpload(i, table, defs);
              })
          }
        };

        var isNotEqualsAttachments = function (table, num) {
          def[num] = $q.defer();
          tables[num] = {table:table, taskID:null, tableId:table.id, desc:null, isNew:true};
          tablePromises[num] = def[num].promise;
        };

        if(tableFields.length > 0) {
          for (var i=0; i<tableFields.length; i++) {
            isNotEqualsAttachments(tableFields[i], i);
          }
        }

        syncTableUpload(items, tables, def);

        $q.all(tablePromises).then(function () {
          var qs = {
            aFormProperty : createProperties(task.aFormProperty)
          };

          if (issue)
            qs.aProcessSubjectTask = issue;

          simpleHttpPromise({
            method: 'POST',
            params: {sID_BP: bpID, nID_Subject: 1, nID_Service: 1, nID_ServiceData: 1, sID_UA: 1}, // хардкод будем тянуть эти параметры позже с централа
            url: '/api/create-task/saveCreatedTask',
            data: qs
          }).then(function (result) {
              deferred.resolve(result);
            },
            function(result) {
              deferred.resolve(result);
            });
        });
        return deferred.promise;
      },
      getFilterFieldsList: function (login) {
        return simpleHttpPromise({
          method: 'GET',
          url: '/api/fields-list',
          params: {
            sLogin: login
          }
        })
      },
      getOrganizationData : function (code) {
        if(code)
          return $http.get('./api/organization-info', {
            params : {
              code : code
            }
          })
      },
      delegateDocToUser : function (params) {
        if (params)
          return simpleHttpPromise({
            method: 'GET',
            url: '/api/documents/delegateDocument',
            params: params
          })
      },
      addAcceptorToDoc  : function (params) {
        if (params)
          return simpleHttpPromise({
            method: 'GET',
            url: '/api/documents/addAcceptor',
            params: params
          });
      },
      addVisorToDoc  : function (params) {
        if (params)
          return simpleHttpPromise({
            method: 'GET',
            url: '/api/documents/addVisor',
            params: params
          });
      },
      addViewerToDoc  : function (params) {
        if (params)
          return simpleHttpPromise({
            method: 'GET',
            url: '/api/documents/addViewer',
            params: params
          });
      },
      getUnsignedDocsList: function () {
        var currentUser = Auth.getCurrentUser();
        return simpleHttpPromise({
          method: 'GET',
          url: '/api/documents/getDocumentSubmittedUnsigned',
          params: {
            sLogin: currentUser.id
          }
        })
      },
      removeDocumentSteps: function (nID_Process) {
        return simpleHttpPromise({
          method: 'GET',
          url: '/api/documents/removeDocumentSteps',
          params: {
            snID_Process_Activiti: nID_Process
          }
        })
      },
      uploadFileHtml: function(name, content) {
        var taskServer = CurrentServer.getServer();
        var deferred = $q.defer();
        var data = {
          sFileNameAndExt: name + '.html',
          sContent: content
        } ;
        var url = '/api/tasks/uploadFileHTML';

        if (taskServer.another)
          data.taskServer = taskServer.name;

        $http.post(url, data).then(function(uploadResult){
          deferred.resolve(uploadResult.data);
        });
        return deferred.promise;
      },
      findAndAssignTask: function (processID) {
        var deferred = $q.defer(),
          self = this,
          login = Auth.getCurrentUser().id;

        self.getTasksByOrder(processID).then(function (taskID) {
          var task = JSON.parse(taskID)[0];
          self.assignTask(task, login).then(function (res) {
            deferred.resolve(task);
          })
        });
        return deferred.promise;
      }
    }
  });
