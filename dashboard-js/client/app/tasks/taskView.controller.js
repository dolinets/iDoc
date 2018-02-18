function traverseTable(formProperties, key, rowTriggered) {
  for (var ent in formProperties) {
    if (formProperties.hasOwnProperty(ent) && formProperties[ent].type === 'table') {
      var rows = formProperties[ent].aRow;
      for (var row in rows) {
        if (rows.hasOwnProperty(row) && row.length !== 0) {
          if(rowTriggered === undefined || rowTriggered === null) {
            var fields = rows[rows.length - 1];
          } else {
            fields = rows[rowTriggered];
          }
          for (var field in fields) {
            if (fields.hasOwnProperty(field) && field.length !== 0) {
              var tables = fields[field];
              for (var table in tables) {
                if (tables[table].id === key) {
                  return tables[table];
                }
              }
            }
          }
        }
      }
    }
  }
}

(function() {
  'use strict';

  angular
    .module('dashboardJsApp')
    .controller('TaskViewCtrl', [
      '$scope', '$stateParams', 'taskData', 'PrintTemplateService', 'iGovMarkers', 'tasks', 'user',
      'taskForm', 'iGovNavbarHelper', 'Modal', 'Auth', 'defaultSearchHandlerService',
      '$state', 'stateModel', 'ValidationService', 'FieldMotionService', 'FieldAttributesService', '$rootScope',
      'lunaService', 'TableService', 'autocompletesDataFactory', 'documentRights', 'documentLogins', '$filter',
      '$sce', 'eaTreeViewFactory', '$location', 'DocumentsService', 'snapRemote', 'tasksSearchService',
      'fieldsService', 'Issue', 'signDialog', 'generationService', '$http', 'taskFilterService', '$q', 'ExecAndCtrlService',
      'chatMessages', 'Tab', 'CurrentServer', 'SubFolder', '$cookies', 'searchService', 'Staff',
      function($scope, $stateParams, taskData, PrintTemplateService, iGovMarkers, tasks, user,
               taskForm, iGovNavbarHelper, Modal, Auth, defaultSearchHandlerService,
               $state, stateModel, ValidationService, FieldMotionService, FieldAttributesService, $rootScope,
               lunaService, TableService, autocompletesDataFactory, documentRights, documentLogins, $filter,
               $sce, eaTreeViewFactory, $location, DocumentsService, snapRemote, tasksSearchService,
               fieldsService, Issue, signDialog, generationService, $http, taskFilterService, $q, ExecAndCtrlService,
               chatMessages, Tab, CurrentServer, SubFolder, $cookies, searchService, Staff) {
        var defaultErrorHandler = function(response, msgMapping) {
          defaultSearchHandlerService.handleError(response, msgMapping);
          if ($scope.taskForm) {
            $scope.taskForm.isSuccessfullySubmitted = false;
            $scope.taskForm.isInProcess = false;
          }
        };

        CurrentServer.setServer($stateParams.sID_Order);

        function getRegexContains(str, splitBy, part) {
          var as = str.split(splitBy);
          for (var i = 0; i < as.length; i++) {
            if (as[i].indexOf(part) >= 0) {
              return as[i];
            }
          }
          return null;
        }

        FieldMotionService.reset();
        iGovMarkers.reset();
        iGovMarkers.init();

        var sLoginAsignee = "sLoginAsignee";
        $scope.sSelectedTask = $location.hash() ? $location.hash() : $stateParams.type;
        $scope.navigateInfo = {};

        function getObjFromTaskFormById(id) {
          if (id == null) return null;
          for (var i = 0; i < taskForm.length; i++) {
            //             if (taskForm[i].id && taskForm[i].id.includes && taskForm[i].id.includes(id)) {
            //               return taskForm[i];
            //             }
            if (taskForm[i].id && taskForm[i].id.indexOf(id) >= 0) {
              return taskForm[i];
            }
          }
          return null;
        }

        function convertUsersToEnum(aoUser) {
          var aoNewUser = new Array(aoUser.length);
          for (var i = 0; i < aoUser.length; i++) {
            var item = aoUser[i];
            var newItem = {};
            newItem.id = item.sLogin;
            newItem.name = item.sLastName.trim() + ' ' + item.sFirstName.trim();
            aoNewUser[i] = newItem;
          }
          return aoNewUser;
        }

        /*
         * Достаем колонки таблицы
         * @param formProperties - $scope.taskForm
         * @param key - искомый объект
         */

        function getIdFromActivityProperty(param) {
          if (param == null) return null;
          var item = getObjFromTaskFormById(sLoginAsignee);
          if (item !== null) {
            var as = getRegexContains(item.name, ';', param);
            as = getRegexContains(as, ',', param);
            var sID = as.split('=')[1];
            return sID;
          }
          return null;
        }

        $scope.updateAssigneeName = function(item) {
          if (item.id.includes(sLoginAsignee)) {
            for (var i = 0; i < item.enumValues.length; i++) {
              if (item.value == item.enumValues[i].id) {
                var sAssigneeName = getObjFromTaskFormById(getIdFromActivityProperty("sDestinationFieldID_sName"));
                if (sAssigneeName != null) {
                  sAssigneeName.value = item.enumValues[i].name;
                  break;
                }
              }
            }
          }
        };

        fillingUsers();

        function fillingUsers() {
          if (taskData.sLoginAssigned != null) {
            var itemWith_sID = getObjFromTaskFormById(getIdFromActivityProperty("sSourceFieldID_sID_Group"));

            if (itemWith_sID !== null) {
              var group = itemWith_sID.value;
              if (group !== null) {
                var item = getObjFromTaskFormById(sLoginAsignee);
                item.type = "enum";
                user.getUsers(group).then(function(users) {
                  if (users) {
                    sortUsersByAlphabet(users);
                    item.enumValues = convertUsersToEnum(users);
                    if (item.value == null) {
                      item.value = item.enumValues[0].id;
                      $scope.updateAssigneeName(item);
                    }
                    // hidden sAssignName
                    hiddenObjById(getIdFromActivityProperty("sDestinationFieldID_sName"));
                  }
                });
              }
            }
          }
        }

        function sortUsersByAlphabet(items) {
          items.sort(function(a, b) {
            if (a.sLastName > b.sLastName) {
              return 1;
            }
            if (a.sLastName < b.sLastName) {
              return -1;
            }
            if (a.sFirstName > b.sFirstName) {
              return 1;
            }
            if (a.sFirstName < b.sFirstName) {
              return -1;
            }
            return 0;
          });
        }

        function IsSavedIssueEdit() {
          if ($scope.taskData.mProcessVariable && $scope.taskData.mProcessVariable.aProcessSubjectTask) {
            if ($scope.taskData.aProcessSubjectTask && $scope.issue) {
              var isSavedIssueEdit = Issue.checkIsSavedIssueEdit(
                $scope.taskData.aProcessSubjectTask,
                $scope.taskData.mProcessVariable.aProcessSubjectTask,
                $scope.issue
              );
              if (isSavedIssueEdit)
                $scope.isIssueEdit = true;
            }
          }
        }

        if (documentRights) {
          $scope.documentRights = documentRights;
          if (documentLogins) $scope.documentLogins = documentLogins;
        }

        if (chatMessages) {
          $scope.chatMessages = chatMessages;
        }

        if ($scope.sSelectedTask === 'docHistory') {
          $scope.documentLogins = documentLogins;
        }

        activate();

        function activate() {
          angular.forEach(taskForm, function(item) {
            var checkbox = getCheckbox((item.name || '').split(';')[2]);

            if (checkbox) {
              bindEnumToCheckbox({
                id: item.id,
                enumValues: item.enumValues,
                sID_CheckboxTrue: checkbox.sID_CheckboxTrue,
                self: item
              });
            }

            if (checkbox && item.type === 'enum') {
              item.type = 'checkbox';
            }
          });


          function getCheckbox(param) {
            if (!param || !typeof param === 'string') return null;

            var input = param.trim(),
              finalArray,
              result = {};

            var checkboxExp = input.split(',').filter(function(item) {
              return (item && typeof item === 'string' ? item.trim() : '')
                .split('=')[0]
                .trim() === 'sID_CheckboxTrue';
            })[0];

            if (!checkboxExp) return null;

            finalArray = checkboxExp.split('=');

            if (!finalArray || !finalArray[1]) return null;

            var indexes = finalArray[1].trim().match(/\d+/ig),
              index;

            if (Array.isArray(indexes)) {
              index = isNaN(+indexes[0]) || +indexes[0];
            }

            result[finalArray[0].trim()] = index !== undefined &&
            index !== null ||
            index === 0 ? index : finalArray[1].trim();

            return result;
          }

          function bindEnumToCheckbox(param) {
            if (!param || !param.id || !param.enumValues ||
              param.sID_CheckboxTrue === null ||
              param.sID_CheckboxTrue === undefined) return;

            var checkbox = {},
              trueValues,
              falseValues;

            if (isNaN(+param.sID_CheckboxTrue)) {
              trueValues = param.enumValues.filter(function(o) { return o.id === param.sID_CheckboxTrue });
              falseValues = param.enumValues.filter(function(o) { return o.id !== param.sID_CheckboxTrue });
              checkbox[param.id] = {
                trueValue: trueValues[0] ? trueValues[0].id : null,
                falseValue: falseValues[0] ? falseValues[0].id : null
              };
            } else {
              falseValues = param.enumValues.filter(function(o, i) { return i !== param.sID_CheckboxTrue });
              checkbox[param.id] = {
                trueValue: param.enumValues[param.sID_CheckboxTrue] ?
                  param.enumValues[param.sID_CheckboxTrue].id : null,
                falseValue: falseValues[0] ? falseValues[0].id : null
              };
            }

            angular.extend(param.self, {
              checkbox: checkbox
            });
          }
        }

        function searchSelectSubject() {
          angular.forEach(taskForm, function(item) {
            var isExecutorSelect = item.name ? item.name.split(';')[2] : null;
            if (item.type === 'select' || item.type === 'string' || (isExecutorSelect && isExecutorSelect.indexOf('sID_SubjectRole=Executor') > -1) || (isExecutorSelect && isExecutorSelect.indexOf('sID_Relation') > -1)) {
              var match;
              if (((match = item.id ? item.id.match(/^s(Currency|ObjectCustoms|SubjectOrganJoinTax|ObjectEarthTarget|Country|ID_SubjectActionKVED|ID_ObjectPlace_UA)(_(\d+))?/) : false)) ||
                (item.type == 'select' && (match = item.id ? item.id.match(/^s(Country)(_(\d+))?/) : false)) || isExecutorSelect) {
                if (match && autocompletesDataFactory[match[1]] && !isExecutorSelect) {
                  item.type = 'select';
                  item.selectType = 'autocomplete';
                  item.autocompleteName = match[1];
                  if (match[2])
                    item.autocompleteName += match[2];
                  item.autocompleteData = autocompletesDataFactory[match[1]];
                } else if (!match && isExecutorSelect.indexOf('SubjectRole') > -1) {
                  var props = isExecutorSelect.split(','),
                    role;
                  item.type = 'select';
                  item.selectType = 'autocomplete';
                  for (var i = 0; i < props.length; i++) {
                    if (props[i].indexOf('sID_SubjectRole') > -1) {
                      role = props[i];
                      break;
                    }
                  }
                  var roleValue = role ? role.split('=')[1] : null;
                  if (roleValue && roleValue === 'Executor') item.autocompleteName = 'SubjectRole';
                  if (roleValue && roleValue === 'ExecutorDepart') item.autocompleteName = 'SubjectRoleDept';
                  item.autocompleteData = autocompletesDataFactory[item.autocompleteName];
                } else if (!match && isExecutorSelect.indexOf('Relation') > -1) {
                  var prodProps = isExecutorSelect.split(','),
                    prodValue;
                  item.type = 'select';
                  item.selectType = 'autocomplete';
                  for (var j = 0; j < prodProps.length; j++) {
                    if (prodProps[j].indexOf('sID_Relation') > -1) {
                      prodValue = prodProps[j];
                      break;
                    }
                  }
                  if ((prodValue ? prodValue.split('=')[1] : null) === 'sID_Relation') {
                    item.autocompleteName = 'ProductList';
                  }
                  item.autocompleteData = autocompletesDataFactory[item.autocompleteName];
                }
              }
            }
          })
        }
        searchSelectSubject();

        $scope.isShowExtendedLink = function() {
          return tasks.isFullProfileAvailableForCurrentUser(taskData);
        };

        $scope.taskData = taskData;
        $scope.printTemplateList = [];
        $scope.model = stateModel;
        $scope.model.printTemplate = null;
        $scope.tableContentShow = false;
        $scope.date = {
          options: {
            timePicker: false
          }
        };

        var sMinTaskDate = new Date();
        if ($scope.taskData && $scope.taskData.aProcessSubjectTask.length > 0)
          sMinTaskDate = $scope.taskData.aProcessSubjectTask[0].aProcessSubject[0].sDatePlan;
        $scope.disablePastDays = { minDate: new Date(sMinTaskDate) };
        $scope.disablePastDays.minDate.setHours(0,0,0,0);

        $scope.taskForm = null;
        $scope.error = null;
        $scope.clarify = false;
        $scope.clarifyFields = {};
        //$scope.selectedTask = oTask;
        //$scope.taskId = oTask.id;
        $scope.taskId = $scope.taskData.nID_Task;
        $scope.tabHistoryAppeal = 'appeal';
        //$scope.nID_Process = oTask.processInstanceId;
        $scope.nID_Process = $scope.taskData.oProcess.nID;
        $scope.markers = iGovMarkers.getMarkers();
        $scope.bHasEmail = false;
        $scope.isClarifySending = false;
        $scope.tableIsInvalid = false;
        $scope.taskData.aTable = [];
        $scope.usersHierarchyOpened = false;
        $scope.taskData.aNewAttachment = [];
        $rootScope.delegateSelectMenu = false;
        $rootScope.acceptSelectMenu = false;
        $rootScope.visorSelectMenu = false;
        $rootScope.viewerSelectMenu = false;
        $rootScope.spinner = false;
        $scope.isIssueEdit = false;
        $scope.showConversation = false;
        $scope.taskCounter = 0;
        $scope.orderID = $stateParams.sID_Order ? $stateParams.sID_Order : null;
        var aID_FieldPhoneUA = $scope.markers.validate.PhoneUA.aField_ID;
        $scope.wasAssignedOnCurrentStep = false;
        var referent;

        if ($cookies.get('referent')) {
          referent = JSON.parse($cookies.get('referent'));
        }

        $scope.showAcceptIssue = function() {
          var aSubjects = $scope.taskData.aProcessSubjectTask[0].aProcessSubject;
          var nMinOrder = Number.MAX_SAFE_INTEGER,
              nSubjectPos = 0;
          for (var i = 0; i < aSubjects.length; i++) {
              if (aSubjects[i].sLoginRole === 'Executor' && aSubjects[i].nOrder < nMinOrder)
                nSubjectPos = i;
          }

          return "executed notExecuted unactual".indexOf(aSubjects[nSubjectPos].oProcessSubjectStatus.sID) > -1 ? true : false;
        };

        $scope.isAnyIssues = function() {
          var issues = Issue.getIssues();
          return issues.length > 0;
        };

        $scope.isDocument = function() {
          var documentTabs = ['documents', 'myDocuments', 'ecp', 'viewed', 'docHistory'],
              isSubFolders = SubFolder.hasSubFolder(documentTabs);
          if (isSubFolders) {
            var sub = iGovNavbarHelper.getSubFolders(true);
            documentTabs = Array.isArray(sub) && sub.length > 0 ? documentTabs.concat(sub) : documentTabs;
          }

          return documentTabs.indexOf($scope.sSelectedTask) > -1;
        };

        var hasSubFolder = SubFolder.hasSubFolder(iGovNavbarHelper.tabName[$scope.taskData.oTab.sDocumentStatus]),
          step = $scope.taskData.mProcessVariable && $scope.taskData.mProcessVariable['sKey_Step_Document'] ? $scope.taskData.mProcessVariable['sKey_Step_Document'] : null,
          subFolder = hasSubFolder ? Tab.checkSubFolder(
            $scope.taskData.aDocumentStepLogin,
            step,
            iGovNavbarHelper.tabName[$scope.taskData.oTab.sDocumentStatus]) : null;

        if (subFolder && subFolder.isVisible) {
          var subObj = iGovNavbarHelper.subfolders[iGovNavbarHelper.tabName[$scope.taskData.oTab.sDocumentStatus]];
          subObj.show = true;
        }

        $scope.getInitials = function(name) {
          if (name) {
            var nameAndMiddleName = name.split(' ');
            if (nameAndMiddleName.length === 2) {
              return nameAndMiddleName[0][0] + '.' + nameAndMiddleName[1][0] + '.';
            }
          } else {
            return name;
          }
        };

        $scope.validateForm = function(form) {
          var bValid = true && bCanSubmitForm;
          var oValidationFormData = {};
          angular.forEach($scope.taskForm, function(field) {
            oValidationFormData[field.id] = angular.copy(field);
            if (field.type === 'file') {
              //debugger;
            }
          });
          ValidationService.validateByMarkers(form, $scope.markers, true, oValidationFormData);
          return form.$valid && bValid;
        };

        var addIndexForFileItems = function(val) {
          var idx = 0;
          return (val || []).map(function(item) {
            if (item.type === 'file') {
              item.nFileIdx = idx;
              idx++;
            }
            return item;
          });
        };

        var isItemFormPropertyDisabled = function(oItemFormProperty) {
          if (oItemFormProperty.id === 'reportFile' && oItemFormProperty.name.indexOf('reportFile') === 0) {
            return false;
          }

          if (!$scope.taskData || (!$scope.taskData.sLoginAssigned && !$scope.isDocument()) || !oItemFormProperty ||
            !$scope.sSelectedTask || $scope.sSelectedTask === 'finished' || $scope.sSelectedTask === 'docHistory')
            return true;

          var sID_Field = oItemFormProperty.id;
          if (sID_Field === null) {
            return true;
          }
          if (!oItemFormProperty.writable) {
            return true;
          }
          //var bNotBankID =
          var bEditable = sID_Field.indexOf("bankId") !== 0;
          var sFieldName = oItemFormProperty.name;
          if (sFieldName === null) {
            return true;
          }
          var as = sFieldName.split(";");
          if (as.length > 2) {
            bEditable = as[2] === "writable=true" ? true : as[2] === "writable=false" ? false : bEditable;
          }

          return !bEditable;
        };

        $scope.taskForm = addIndexForFileItems(taskForm);

        $scope.printTemplateList = {};

        $scope.taskForm.taskData = taskData;

        if ($stateParams.type === 'docHistory')
          $rootScope.historyTaskData = $scope.taskForm.taskData; //for accesing taskData from motion servise

        if (!$scope.taskData.sDateEnd || $stateParams.type === 'docHistory') {
          $scope.taskForm.forEach(function(field) {
            var type, value, name;
            if ($stateParams.type === 'docHistory') { //name convention huck
              value = field.oValue;
              type = field.sType;
              name = field.sName;
            } else {
              value = field.value;
              type = field.type;
              name = field.name;
            }

            if (type === 'markers' && $.trim(value)) {
              var sourceObj = null;
              try {
                sourceObj = JSON.parse(value);
              } catch (ex) {
                console.log('markers attribute ' + name + ' contain bad formatted json\n' + ex.name + ', ' + ex.message + '\nfield.value: ' + value);
              }
              if (sourceObj !== null) {
                _.merge($scope.markers, sourceObj, function(destVal, sourceVal) {
                  if (_.isArray(sourceVal)) {
                    return sourceVal;
                  }
                });
              }
            }
          });
        }

        function downloadFileHTMLContent() {
          angular.forEach($scope.taskForm, function(i, k, o) {
            if (i.type === 'fileHTML' && i.value && i.value.indexOf('sKey') > -1) {
              tasks.getTableOrFileAttachment($scope.taskData.oProcess.nID, i.id, true).then(function(res) {
                o[k].valueVisible = res;
              })
            } else if (i.sType === 'fileHTML' && i.oValue && i.oValue.indexOf('sKey') > -1) {
              var key = JSON.parse(i.oValue);
              tasks.getTableOrFileAttachment(key.sKey, key.sID_StorageType, true).then(function(res) {
                o[k].valueVisible = res;
              })
            }
          });

          if ($scope.taskData.aProcessSubjectTask && $scope.taskData.aProcessSubjectTask.length > 0) {
            angular.forEach($scope.taskData.aProcessSubjectTask, function(issue, key, object) {
              if (issue.sBody && issue.sBody.indexOf('sKey') > -1) {
                var parse = JSON.parse(issue.sBody);
                tasks.getTableOrFileAttachment(parse.sKey, parse.sID_StorageType, true).then(function(res) {
                  object[key].visibleBody = res;
                })
              }
            })
          }
        }
        downloadFileHTMLContent();

        extractFieldOption($scope.taskForm);

        function extractFieldOption(aProperties) {
          angular.forEach(aProperties, function(property) {
            var i, source, equalsIndex, key, val;
            if (!property.options) property.options = {};

            if (property.name && property.name.indexOf(';;') >= 0) {
              var as = property.name.split(';;');
              property.name = as[0];
              for (i = 1; i < as.length; i++) {
                source = as[i];
                equalsIndex = source.indexOf('=');
                key = source.substr(0, equalsIndex).trim();
                try {
                  val = angular.fromJson(source.substr(equalsIndex + 1).trim())
                } catch (e) {
                  val = source.substr(equalsIndex + 1).trim();
                }
                property.options[key] = val;
              }
            }

            if (property.name && property.name.indexOf(';') >= 0) {
              var sOldOptions = property.name.split(';')[2];
              if (sOldOptions) {
                var aOptions = sOldOptions.split(',');
                for (i = 0; i < aOptions.length; i++) {
                  source = aOptions[i];
                  equalsIndex = source.indexOf('=');
                  key = source.substr(0, equalsIndex).trim();
                  try {
                    val = angular.fromJson(source.substr(equalsIndex + 1).trim())
                  } catch (e) {
                    val = source.substr(equalsIndex + 1).trim();
                  }
                  property.options[key] = val;
                }
              }
            }
          })
        }

        function fillArrayWithNewAttaches() {
          angular.forEach($scope.taskForm, function(item) {
            var type = item.type ? item.type : item.sType;
            var sItemId = item.id ? item.id : item.sId;
            var name = item.name ? item.name : item.sName;
            if (['file', 'table'].indexOf(type) > -1 && sItemId.indexOf('oProcessSubject_Executor') !== 0 && name.indexOf('bVisible=false') === -1) {
              try {
                var parsedValue = item.oValue ? JSON.parse(item.oValue) : JSON.parse(item.value);
                if (parsedValue && parsedValue.sKey) {
                  var sFieldName = name || '';
                  var aNameParts = sFieldName.split(';');
                  var sFieldNotes = aNameParts[0].trim();
                  item.sFieldLabel = sFieldNotes;
                  sFieldNotes = null;
                  if (aNameParts.length > 1) {
                    sFieldNotes = aNameParts[1].trim();
                    if (sFieldNotes === '') {
                      sFieldNotes = null;
                    }
                  }
                  item.sFieldNotes = sFieldNotes;
                  $scope.taskData.aNewAttachment.push(item);
                }
              } catch (e) {}
            }
          })
        }
        fillArrayWithNewAttaches();

        function getAdaptedFormData(taskForm) {
          var oAdaptFormData = {};
          angular.forEach(taskForm, function(item) {
            oAdaptFormData[item.id] = {
              required: item.required,
              value: item.value,
              writable: item.writable
            }
          });
          return oAdaptFormData;
        }

        $scope.isRequired = function(item) {
          if (!item || !item.id) return false;
          var bRequired = FieldMotionService.FieldMentioned.inRequired(item.id) ?
            FieldMotionService.isFieldRequired(item.id, getAdaptedFormData($scope.taskForm)) : item.required;
          var b = !$scope.isFormPropertyDisabled(item) && (bRequired || $scope.isCommentAfterReject(item));
          return b;
        };

        function isFloat(n) {
          return n === +n && n !== (n|0);
        }

        function isInteger(n) {
          return n === +n && n === (n|0);
        }

        function findProperHtmlEditor(formProperties) {
          var occurences = 0;
          var properIndex;
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
          return properIndex;
        }


        $scope.$watch('taskForm', watchToSetDefaultValues, true);

        function isJson(str) {
          try {
            JSON.parse(str);
          } catch (e) {
            return false;
          }
          return true;
        }

        function setHasChatMessages() {
          var aChatAuthors = [];

          if ($scope.chatMessages)
            angular.forEach($scope.chatMessages.aProcessChat, function(item) {
              angular.forEach(item.aProcessChatMessage, function (message) {
                  aChatAuthors.push(message.sKeyGroup_Author);
              });
            });

          angular.forEach($scope.taskForm, function(item) {
            if (item && item.type === 'table')
              angular.forEach(item.aRow, function(row) {
                if (row)
                  angular.forEach(row.aField, function(field) {
                    if (field.name === 'Login' && aChatAuthors.indexOf(field.value) > -1)
                      row.hideDeleteRowBtn = true;
                  });
              });
          });

        }

        function calculateTableFields() {
          $scope.bIsResultCalcNameExists = false;

          angular.forEach($scope.taskForm, function (form) {
            if (form.type === 'table') {
              var rows = form.aRow;
              angular.forEach(rows, function (row) {
                angular.forEach(row.aField, function (column) {
                  if (_.has(column, 'sFormula')) {
                    try {
                      var formula = column.sFormula.replace(/[\[\]]/g, "").split(/([()\+\-\*\/])/).map(function (a) {
                        return parseFloat(a) || a;
                      }).join('');
                      var aFormulaParts = formula.split(/([()\+\-\*\/])/);
                      var result = '';
                      var bIsReplaced = false;

                      for (var i = 0; i < aFormulaParts.length; i++) {
                        angular.forEach(row.aField, function (searchColumn) {
                          if (searchColumn.id === aFormulaParts[i]) {
                            var re = new RegExp(searchColumn.id, "g");
                            if (!bIsReplaced) {
                              result = formula.replace(re, searchColumn.value);
                              bIsReplaced = true;
                            } else {
                              result = result.replace(re, searchColumn.value);
                            }
                          }
                        });
                      }
                      if (result) {
                        column.value = eval(result);
                      }
                    } catch (e) {
                      column.value = '';
                    }
                  }
                  var nSum = 0;
                  if(_.has(column, 'sFooterCalc')) {
                    form.sFooterCalcResult = 0;
                    $scope.bIsResultCalcNameExists = true;
                    if(column.sFooterCalc === 'sum') {
                      var sColumnId = column.id;
                      if(rows.length === 1) {
                        nSum += column.value;
                        var sFooterResId = column.sFooterCalcResult_sID_Field;
                        if(sFooterResId) {
                          angular.forEach($scope.taskForm, function (form) {
                            if(form.id === sFooterResId) {
                              form.value = nSum.toString();
                            }
                          });
                        }
                      } else {
                        angular.forEach(rows, function (r) {
                          angular.forEach(r.aField, function (col) {
                            if(col.id === sColumnId) {
                              nSum += col.value;
                              var sFooterResId = column.sFooterCalcResult_sID_Field;
                              if(sFooterResId) {
                                angular.forEach($scope.taskForm, function (form) {
                                  if(form.id === sFooterResId) {
                                    form.value = nSum.toString();
                                  }
                                });
                              }
                            }
                          });
                        });
                      }
                    }
                    form.sFooterCalcResult = nSum;
                  }
                });
              });
            }
          });
        }

        function watchToSetDefaultValues() {
          var calcFields = FieldMotionService.getTargetFieldsIds('Values');
          var pars = getAdaptedFormData($scope.taskForm);

          var bIsEditorExists = false;
          for(var walkIndex = 0; walkIndex < $scope.taskForm.length; walkIndex++) {
            if($scope.taskForm[walkIndex].type === 'fileHTML') {
              bIsEditorExists = true;
              break;
            }
          }

          if(bIsEditorExists && tinymce.activeEditor) {
            var aParagraphs = tinymce.activeEditor.dom.select('p');
            angular.forEach(aParagraphs, function (p) {
              angular.forEach(p.childNodes, function (a) {
                if(a.href && (a.pathname.indexOf('documents') === 1 ||
                              a.pathname.indexOf('tasks') === 1)) {
                  var newHref = a.href.replace(/(https:\/\/.*?\/)/, '/');
                  a.setAttribute('href', newHref);
                }
              });
            });
          }

          // for (var oTable in pars) {
          //   if (oTable.indexOf('sTable') > -1){
          //     if (isJson(pars[oTable].value)){
          //       tablePars[oTable] = getAdaptedFormData(JSON.parse(pars[oTable].value).aField);
          //     }
          //   }
          // }

          setHasChatMessages();

          calcFields.forEach(function(key) {
            if (_.has(pars, key)) {
              var data = FieldMotionService.calcFieldValue(key, pars, $scope.taskForm);
              if (data.value !== null && !(data.value !== data.value) && data.differentTriggered) {
                var oField = $scope.taskForm.filter(function(field) {
                  return field.id === key;
                })[0];
                if (oField.type === 'string' && angular.isNumber(data.value)) {
                  oField.value = '' + data.value;
                } else {
                  oField.value = data.value;
                }
              }
            }

            calculateTableFields();

            // for(var par in tablePars) {
            //   if (_.has(tablePars[par], key)) {
            //     var tableData = FieldMotionService.calcFieldValue(key, pars, $scope.taskForm);
            //     if (tableData.value !== null && !isNaN(tableData.value)) {
            //       var oTableField = traverseTable($scope.taskForm, key);
            //       oTableField = isFloat(tableData.value) ? oTableField.value = '' + tableData.value.toFixed(2) : oTableField.value = Math.round(tableData.value);
            //
            //     }
            //   }
            // }
          });
        }

        var bCanSubmitForm = true;

        $scope.isTaskSubmitted = function(item) {
          return $scope.taskForm.isSubmitted;
        };

        $scope.isTaskSuccessfullySubmitted = function() {
          if ($scope.taskData && $scope.taskForm) {
            if ($scope.taskForm.isSuccessfullySubmitted !== undefined && $scope.taskForm.isSuccessfullySubmitted)
              return true;
          }
          return false;
        };

        $scope.isTaskInProcess = function() {
          if ($scope.taskData && $scope.taskForm) {
            if ($scope.taskForm.isInProcess != undefined && $scope.taskForm.isInProcess)
              return true;
          }
          return false;
        };

        $scope.correctSignName = function(name) {
          var splitName = name.split(';');
          return splitName.length !== 1 ? splitName[0] : name;
        };

        $scope.takeTheKeyFromJSON = function(item) {
          return item.oValue ? JSON.parse(item.oValue).sKey : JSON.parse(item.value).sKey;
        };

        $scope.takeTheFileNameFromJSON = function(item) {
          return item.oValue ? JSON.parse(item.oValue).sFileNameAndExt : JSON.parse(item.value).sFileNameAndExt;
        };

        $scope.takeInformFromJSON = function(item, type) {
          var parsed = item.oValue ? JSON.parse(item.oValue) : (item.value ? JSON.parse(item.value) : null);
          if (parsed)
            switch (type) {
                case ('key'):
                    return parsed.sKey;
                  case ('storage'):
                    return parsed.sID_StorageType;
                  case ('name'):
                    return parsed.sFileNameAndExt;
            }
        };

        $scope.clarify = false;

        $scope.clarifyToggle = function() {
          $scope.clarify = !$scope.clarify;
        };

        $scope.clarifyModel = {
          sBody: ''
        };

        $scope.clarifySend = function() {
          $scope.isClarifySending = true;

          var oData = {
            nID_Process: $scope.nID_Process,
            saField: '',
            soParams: '',
            sMail: '',
            sBody: $scope.clarifyModel.sBody
          };

          var soParams = { sEmployerFIO: $scope.getCurrentUserName };
          var aFields = [];
          var sClientFIO = null;
          var sClientName = null;
          var sClientSurname = null;

          angular.forEach($scope.taskForm, function(item) {
            if (angular.isDefined($scope.clarifyFields[item.id]) && $scope.clarifyFields[item.id].clarify)
              aFields.push({
                sID: item.id !== null ? item.id : "",
                sName: $scope.sFieldLabel(item.name) !== null ? $scope.sFieldLabel(item.name) : "",
                sType: item.type !== null ? item.type : "",
                sValue: item.value !== null ? item.value : "",
                sValueNew: item.value !== null ? item.value : "",
                sNotify: $scope.clarifyFields[item.id].text !== null ? $scope.clarifyFields[item.id].text : ""
              });

            if (item.id === 'email') {
              oData.sMail = item.value;
            }
            //<activiti:formProperty id="bankIdfirstName" name="Ім'я" type="string" ></activiti:formProperty>
            //<activiti:formProperty id="bankIdmiddleName" name="По Батькові" type="string" ></activiti:formProperty>
            if (item.id === 'bankIdfirstName') {
              sClientName = item.value;
            }
            if (item.id === 'bankIdmiddleName') {
              sClientSurname = item.value;
            }
          });

          if ($scope.clarifyModel.sBody.trim().length === 0 && aFields.length === 0) {
            Modal.inform.warning()('Треба ввести коментар або обрати поле/ля');
            return;
          }


          if (sClientName !== null) {
            sClientFIO = sClientName;
            if (sClientSurname !== null) {
              sClientFIO += " " + sClientSurname;
            }
          }
          if (sClientFIO !== null) {
            soParams["sClientFIO"] = sClientFIO;
          }

          oData.saField = JSON.stringify(aFields);
          oData.soParams = JSON.stringify(soParams);
          if (oData.saField === "[]") {
            oData.nID_Process = oData.nID_Process + lunaService.getLunaValue(oData.nID_Process);
            tasks.postServiceMessages(oData).then(function() {
              $scope.clarify = false;
              $scope.isClarifySending = false;
              Modal.inform.success(function() {})('Коментар відправлено успішно');
            });
          } else {
            tasks.setTaskQuestions(oData).then(function() {
              $scope.clarify = false;
              $scope.isClarifySending = false;
              Modal.inform.success(function() {})('Зауваження відправлено успішно');
            });
          }
        };

        (function isTaskHasEmail() {
          try {
            for (var i = 0; i < $scope.taskData.aField.length; i++) {
              if ($scope.taskData.aField[i].sID === "email") {
                $scope.bHasEmail = true;
              }
            }
          } catch (err) {
            if ($scope.taskData.code && $scope.taskData.message) {
              console.warn($scope.taskData.message);
            } else {
              console.error(err);
            }
          }
        })();

        $scope.checkSignState = { inProcess: false, show: false, signInfo: null, attachmentName: null };

        /*
         * проверка наличия эцп. поддерживается старый и новый сервис, разделение по 4му параметру
         * @param nID_Task - ид таски (если новый серивс - ид процесса);
         * @param nID_Attach - ид аттача;
         * @param attachmentName - для старого сервиса передаеться sDescription, для нового - name;
         * @param {boolean} isNewAttachment - если true используеться новый сервис checkProcessAttach, иначе check_attachment_sign
         */
        $scope.checkAttachmentSign = function(nID_Task, nID_Attach, attachmentName, isNewAttach) {
          $scope.checkSignState.inProcess = true;
          tasks.checkAttachmentSign(nID_Task, nID_Attach, isNewAttach).then(function(signInfo) {
            if (signInfo.customer) {
              $scope.checkSignState.show = !$scope.checkSignState.show;
              $scope.checkSignState.signInfo = signInfo;
              $scope.checkSignState.attachmentName = attachmentName;
            } else if (signInfo.code) {
              $scope.checkSignState.show = false;
              $scope.checkSignState.signInfo = null;
              $scope.checkSignState.attachmentName = null;
              Modal.inform.warning()('Перевірка ЕЦП тимчасово недоступна')
              //Modal.inform.warning()(signInfo.message);
            } else {
              $scope.checkSignState.show = false;
              $scope.checkSignState.signInfo = null;
              $scope.checkSignState.attachmentName = null;
              Modal.inform.warning()('Немає підпису');
            }
          }).catch(function(error) {
            $scope.checkSignState.show = false;
            $scope.checkSignState.signInfo = null;
            $scope.checkSignState.attachmentName = null;
            //Modal.inform.error()(error.message);
          }).finally(function() {
            $scope.checkSignState.inProcess = false;
          });
        };

        $scope.isFormPropertyDisabled = isItemFormPropertyDisabled;

        function getIdByName(item, asName) {
          var asId = new Array();
          for (var i = 0; i < asName.length; i++) {
            asId.push(item[asName[i]]);
          }
          return asId;
        }

        function getValueById(id) {
          for (var i = 0; i < taskForm.length; i++) {
            var item = taskForm[i];
            if (item.id.indexOf(id) >= 0) {
              return item.value;
            }
          }
          return null;
        }

        function getAllNamesFields(item) {
          if (item == null) return null;

          var variables = "";
          for (var name in item) {
            variables += name + ",";
          }
          var as = variables.split(",");
          var result = new Array();

          for (var i = 0; i < as.length; i++) {
            if (as[i] != "") {
              result.push(as[i]);
            }
          }

          return result;
        }

        function getVariablesValue(asId) {
          if (asId == null) return null;
          var asVariablesValue = new Array(asId.length);
          for (var i = 0; i < asId.length; i++) {
            var item = getObjFromTaskFormById(asId[i]),
              value, message;
            if (!item) {
              message = 'Зверніться у технічну підтримку. Обєкт з id ' + asId[i] + ' відсутній. Формула не запрацює.';
              Modal.inform.error()(message);
              throw message;
            }

            if (!(value = item.value)) {
              return undefined;
              // message = 'Пусте поле ' + item.name + '. Прінт Формула не запрацює.';
              // Modal.inform.error()(message);
              // throw message;
            } else if (!isNaN(value)) {
              asVariablesValue[i] = parseInt(value);
            } else {
              asVariablesValue[i] = value;
            }
          }
          return asVariablesValue;
        }

        function pushResultFormula(id, value) {
          var item = getObjFromTaskFormById(id);
          if (item != null) item.value = value;
        }

        function executeFormula(item) {
          var sFormula = item['sFormula'];
          var sResultName = item['sID_Field_Target'];
          var asVariablesName = getAllNamesFields(item['asID_Field_Alias']);
          var asVariablesId = getIdByName(item['asID_Field_Alias'], asVariablesName);
          var asVariablesValue = getVariablesValue(asVariablesId);

          function getVal(index) {
            return asVariablesValue[index];
          }

          if (asVariablesValue === undefined) {
            pushResultFormula(sResultName, null);
            return;
          }
          String.prototype.replaceAll = function(search, replacement) {
            var target = this;
            return target.replace(new RegExp(search, 'g'), replacement);
          };

          for (var i = 0; i < asVariablesName.length; i++) {
            sFormula = sFormula.replaceAll(asVariablesName[i], "getVal(" + i + ")");
          }
          pushResultFormula(sResultName, eval(sFormula));
        }



        function runCalculation() {
          var item = getObjFromTaskFormById("marker");
          if (item !== null) {
            var oMotion = JSON.parse(item.value)['motion']; // Generate obj from json(item.value)
            var asNameField = getAllNamesFields(oMotion); //Generate array fields name

            /*todo иногда oMotion возвращает undefined, что в итоге делает asNameField - null,
             *в итоге ломаеться принтформа
             */
            if (asNameField) {
              for (var i = 0; i < asNameField.length; i++) {
                if (asNameField[i].indexOf("PrintFormFormula") >= 0) {
                  executeFormula(oMotion[asNameField[i]]);
                }
              }
            }
          }
        }

        $scope.hasUnPopulatedFields = function() {
          if ($scope.taskData && $scope.taskForm) {
            var unpopulated = $scope.taskForm.filter(function(item) {
              return (item.value === undefined || item.value === null || item.value.trim() === "") && (item.required || $scope.isCommentAfterReject(item)); //&& item.type !== 'file'
            });
            return unpopulated.length > 0;
          } else {
            return true;
          }
        };

        $scope.unpopulatedFields = function() {
          if ($scope.taskData && $scope.taskForm) {
            var unpopulated = $scope.taskForm.filter(function(item) {
              return (item.value === null && !item.valueVisible) && (item.value === undefined || item.value === null || item.value.trim() === "") && (item.required || $scope.isCommentAfterReject(item)); //&& item.type !== 'file'
            });
            return unpopulated;
          } else {
            return [];
          }
        };

        $scope.isFormInvalid = false;

        function relinkPrintFormsIntoFileFields() {
          var aFileFields = $scope.taskForm.filter(function(field) {
            return field.type === 'file' || field.options.hasOwnProperty('sID_Field_Printform_ForECP');
          });

          angular.forEach(aFileFields, function(oFileField) {
            var oLinkedPrintForm = $scope.taskForm.filter(function(field) {
              return field.id === oFileField.options['sID_Field_Printform_ForECP'];
            })[0];
            if (oLinkedPrintForm) {
              oFileField.options['sPatternFileUrl'] = oLinkedPrintForm.name.substr(0, oLinkedPrintForm.name.indexOf(';')).replace('[', '').replace(']', '').replace(/^pattern\//, '');
            }
          });
        }

        $scope.signTask = function(form, bNotShowSuccessModal) {
          var bEditableFieldsPresentedOnTaskForm = false;
          for (var k = 0; k < $scope.taskForm.length; k++) {
            if (form.hasOwnProperty($scope.taskForm[k].id) && $scope.taskForm[k].writable === true) {
              bEditableFieldsPresentedOnTaskForm = true;
              break;
            }
          }

          if (bEditableFieldsPresentedOnTaskForm) {
            $scope.submitTask(form, bNotShowSuccessModal, true);
          } else {
            var taskForSignin = $scope.filteredTasks.filter(function(task) {
              return task.id === '' + $scope.taskData.nID_Task;
            });
            tasks.signTasks(taskForSignin).then(function(result) {
              $scope.submitTask(form, bNotShowSuccessModal, false);
              Modal.inform.success()(result);
            }, function(error) {
              Modal.inform.error()('Під час підписання документів сталася помилка: ' + angular.toJson(error));
            })
          }
        };

        $scope.ChangeDatetimepicker = function(id) {
          var input = document.getElementById(id);
          input.value = input.value.replace(/\//g, '.');
        };
        //Script Oleg for issue #1949
        //var testDayPlan = function () {
        //    var day = $("input[name='taskDay0']").val();
        //    var regex = day.split(/[A-zА-я]/g);
        //    var result = Math.floor(regex);
        //    result !== '' ? ($('#dayPlan').html(result).css('opacity', '0'), $('#taskDay0').val(regex)) : document.getElementById('taskDay0').style.border = '1px solid #a94442';
        //}
        $scope.submitTask = function(form, bNotShowSuccessModal, isNeedEDS, nextTaskID, updateIssue, sSignType, needAssign) {
          //testDayPlan(); todo пофиксить
          if (!bCanSubmitForm) {
            $rootScope.spinner = false;
            return
          }

          var isAnyIssuesExist = Issue.getIssues();
          var nextTask;
          var taskServer = CurrentServer.getServer();
          var sKeyStepValue = null;

          $scope.taskForm.isSubmitted = true;
          $scope.issueValid = Issue.validate();

          $scope.validateForm(form);

          var interval = setInterval(waitForValidity, 1); // huck for async validator
          function waitForValidity() {
            if (form.$invalid !== undefined) {
              clearInterval(interval);

              completeSubmition();
            }
          }

          function completeSubmition() {
            if (form.$invalid) {
              $scope.$apply(function() {
                $scope.isFormInvalid = true;
              });
              $rootScope.spinner = false;
              return;
            } else {
              $scope.$apply(function() {
                $scope.isFormInvalid = false;
              });

              if (isAnyIssuesExist && !$scope.issueValid) {
                $rootScope.spinner = false;
                return;
              }
            }

            $scope.$apply();

            if ($scope.taskData && $scope.taskForm) {

              var unpopulatedFields = $scope.unpopulatedFields();
              if (documentRights) {
                angular.forEach($scope.taskForm, function(item, key, obj) {
                  if (item.type === 'date') {
                    obj[key].value = $filter('checkDate')(item.value);
                  }
                });
              }
              if (unpopulatedFields.length > 0) {
                $scope.isFormInvalid = true;
                return;
              }

              $scope.taskForm.isInProcess = true;

              rollbackReadonlyEnumFields();
              if ($scope.model.printTemplate) {
                $scope.taskForm.sendDefaultPrintForm = false;
              }

              if ($scope.taskData.oProcess && $scope.taskData.oProcess.sBP && $scope.taskData.oProcess.sBP.match(/^_doc_/)) {
                var sKey_Step_field = $scope.taskForm.filter(function(item) {
                  return item.id === "sKey_Step_Document";
                })[0];
                if (sKey_Step_field) {
                  sKeyStepValue = sKey_Step_field.value;
                  $scope.taskForm.sendDefaultPrintForm = !!sKey_Step_field.value;
                }
              }

              $rootScope.spinner = true;
              if (!nextTaskID) {
                var navNext = $scope.navigateInfo && $scope.navigateInfo.task ? $scope.navigateInfo.task.next : null;
                middlewareSubmitFunction(navNext);
              } else {
                middlewareSubmitFunction(nextTaskID);
              }
            }
          }

          function submitCallback(result) {
            var redirectParameters;

            if (result.status === 501 || result.status === 405) {
              var message = result.data.message;
              var errMsg = (message.indexOf("errMsg") >= 0) ? message.split(":")[1].split("=")[1] : message;
              $scope.taskForm.isInProcess = false;

              if (result.data.errorCode === "documentChanged") {
                Modal.inform.warning()(errMsg);
                var redirectTab = iGovNavbarHelper.tabName[result.data.sDocumentStatus];
                  $state.go('tasks.typeof.view', {
                    'tab': Tab.getCurrentTab($location.url(), $stateParams, $location.hash()),
                    'type': redirectTab,
                    'sID_Order': result.data.sID_Order,
                    '#': redirectTab
                  })
              } else {
                Modal.inform.error(function(result) {})(errMsg + " " + (result && result.length > 0 ? (': ' + result) : ''));
              }
              iGovNavbarHelper.loadTaskCounters($rootScope.tabMenu, true);
            } else {

              if ($rootScope.isEdit) {
                $state.go('tasks.typeof.view', {
                  'tab': 'myDocuments',
                  'type': 'myDocuments',
                  'sID_Order': $stateParams.sID_Order,
                  '#': 'myDocuments'
                });
              }

              Issue.clearIssues();
              var sMessage = "Форму відправлено.";
              if ($scope.sSelectedTask === 'control' && $scope.navigateInfo && $scope.navigateInfo.task){
                redirectParameters = {
                  state: 'tasks.typeof.view',
                  params: {
                    'tab': $scope.navigateInfo.tab,
                    'type': $scope.navigateInfo.type,
                    'sID_Order': $scope.orderID,
                    '#': $scope.navigateInfo['#']
                  }
                };
              } else if (nextTask && nextTask !== 'stay' && $scope.navigateInfo && $scope.navigateInfo.task)
                redirectParameters = {
                  state: 'tasks.typeof.view',
                  params: {
                    'tab': $scope.navigateInfo.tab,
                    'type': $scope.navigateInfo.type,
                    'sID_Order': $scope.navigateInfo.task.next,
                    '#': $scope.navigateInfo['#']
                  }
                };
              else
                redirectParameters = {
                  state: 'tasks.typeof',
                  params: {tab: Tab.getCurrentTab($scope.sSelectedTask), type: $scope.sSelectedTask}
                };
              angular.forEach($scope.taskForm, function(oField) {
                if (oField.id === "sNotifyEvent_AfterSubmit") {
                  sMessage = oField.value;
                }
              });

              if (!bNotShowSuccessModal && iGovNavbarHelper.currentTab && iGovNavbarHelper.currentTab.indexOf("documents") >= 0) {
                bNotShowSuccessModal = true;
              }

              if (bNotShowSuccessModal) {
                $scope.lightweightRefreshAfterSubmit();
              } else {
                Modal.inform.success(function(result) {
                  $scope.lightweightRefreshAfterSubmit();
                })(sMessage + " " + (result && result.length > 0 ? (': ' + result) : ''));
              }

              $scope.$emit('task-submitted', $scope.taskData);

              if (!$rootScope.isEdit && nextTask !== 'stay') {
                $state.go(redirectParameters.state, redirectParameters.params, {reload:true});
                iGovNavbarHelper.loadTaskCounters($rootScope.tabMenu, true);
              } else if (nextTask === 'stay') {
                $state.reload();
              }
              iGovNavbarHelper.load();
            }
            $rootScope.isEdit = false;
          }

          function middlewareSubmitFunction(id) {
            nextTask = id ? id : nextTaskID;
            if ($scope.issue && isAnyIssuesExist.length !== 0 && !updateIssue) {

              Issue.buildIssueObject($scope.issue, $scope.taskData).then(function(res) {
                if ($scope.taskData.aProcessSubjectTask && $scope.taskData.aProcessSubjectTask.length !== 0 && $scope.isIssueAdd) {
                  Issue.fillIssueForEdit($scope.taskData.aProcessSubjectTask, true);

                  Issue.buildIssueObject($scope.issue, $scope.taskData).then(function(localIssues){
                    Array.prototype.push.apply(res, localIssues);
                    signAndSubmitForm(isNeedEDS, res);
                  });
                } else
                  signAndSubmitForm(isNeedEDS, res);
              });
            } else if (updateIssue) {
              signAndSubmitForm(false, updateIssue);
            } else if ($scope.issue && isAnyIssuesExist.length === 0 && $scope.isIssueEdit) {
              signAndSubmitForm(isNeedEDS, []);
            } else {
              signAndSubmitForm(isNeedEDS);
            }
          }

          function signAndSubmitForm(isNeedSign, oIssue) {
            $rootScope.spinner = true;
            var oParams = {
              nID_Process: $scope.taskData.oProcess && $scope.taskData.oProcess.nID ? $scope.taskData.oProcess.nID : null
            };
            if ($scope.taskData.oProcess.sBP.indexOf('_doc_') === 0 || $scope.taskData.oProcess.sBP.indexOf('_task') === 0) {
              for (var variable in $scope.taskData.mProcessVariable) {
                if ($scope.taskData.mProcessVariable.hasOwnProperty(variable) && variable === 'sKey_Step_Document') {
                  oParams.sStep_Document = $scope.taskData.mProcessVariable[variable];
                  break;
                }
              }
            }

            if (isNeedSign) {
              relinkPrintFormsIntoFileFields();
              tasks.generatePDFFromPrintForms($scope.taskForm, $scope.taskData).then(function(result) {
                result.base64encoded = true;

                signDialog.signContentsArray(result,
                  function(signedContents) {
                    $rootScope.switchProcessUploadingState();

                    var aSignedContents = signedContents;

                    var contentsForUploadAsAttach = [];
                    angular.forEach(aSignedContents, function(content) {
                      var aFiles = [];
                      aFiles.push(generationService.getSignedFile(content.sign, content.id));
                      content.aFiles = aFiles;
                      contentsForUploadAsAttach.push({
                        fieldId: content.id,
                        files: aFiles
                      })
                    });

                    tasks.uploadAttachmentsToTaskForm(contentsForUploadAsAttach, $scope.taskForm, $scope.taskData.oProcess.nID, $scope.taskData.nID_Task)
                      .then(function() {
                        $rootScope.switchProcessUploadingState();

                        tasks.setDocumentImages({
                          signedContents: aSignedContents,
                          sKey_Step: sKeyStepValue,
                          taskId: $scope.taskData.oProcess.nID
                        }).then(function(resp) {
                          submitTaskForm($scope.taskForm, $scope.taskData, oIssue, oParams);
                        }, function(error) {
                          Modal.inform.error()(angular.toJson(error));
                        })

                      }, function(error) {
                        Modal.inform.error()(angular.toJson(error));
                      });

                  },
                  function() {
                    console.log('Sign Dismissed');
                    $rootScope.spinner = $scope.taskForm.isInProcess = false;
                    $scope.convertDisabledEnumFiedsToReadonlySimpleText();
                  },
                  function(error) {
                    //todo react on error during sign
                    Modal.inform.error()(angular.toJson(error));
                  }, 'ng-on-top-of-modal-dialog modal-info');

              }, function(err) {
                Modal.inform.error()(angular.toJson(error));
              }).catch(defaultErrorHandler);
            } else {
              submitTaskForm($scope.taskForm, $scope.taskData, oIssue, oParams);
            }
          }

          function submitTaskForm(taskForm, taskData, issues, oParameters) {
            return tasks.submitTaskForm(taskForm, taskData, issues, oParameters, sSignType, needAssign, $rootScope.isEdit)
              .then(submitCallback)
              .catch(function() {
                $rootScope.spinner = false;
                defaultErrorHandler;
              })
              .finally(function() {
                $scope.convertDisabledEnumFiedsToReadonlySimpleText();
                iGovNavbarHelper.loadTaskCounters($rootScope.tabMenu, true);
              });
          }
        };

        $scope.$on('refresh-task-view-after-delegate', function() {
          for (var taskIndex = 0; taskIndex < $scope.filteredTasks.length; taskIndex++) {
            if ($scope.filteredTasks[taskIndex].Id === this.taskId) {
              $scope.filteredTasks.splice(taskIndex, 1);
              break;
            }
          }
          $scope.lightweightRefreshAfterSubmit();
        });

        $scope.submitTaskQuestion = function(form) {
          Modal.inform.submitTaskQuestion(function() { return $scope.submitTask(form); });
        };

        $scope.println = function(form) {
          console.log("println");
          console.log(form);
          return true;
        };
        $scope.saveChangesTask = function(form) {
          var days = $("input[name='taskDay0']").val();
          if (days !== undefined) {
                  //testDayPlan();
          }
          $rootScope.spinner = true;
          var isAnyIssuesExist = Issue.getIssues();

          if ($scope.taskData && $scope.taskForm) {
            $scope.validateForm(form);
            if (form.$invalid) {
              $scope.isFormInvalid = true;
              if (isAnyIssuesExist && isAnyIssuesExist.length > 0) {
                $scope.issueValid = false;
              }
              $rootScope.spinner = false;
              return;
            } else {
              $scope.isFormInvalid = false;
              if (isAnyIssuesExist)
                $scope.issueValid = true;
            }
            $scope.taskForm.isSubmitted = true;

            $scope.taskForm.isInProcess = true;

            rollbackReadonlyEnumFields();

            angular.forEach($scope.taskForm, function(item, key, obj) {
              if (item.type === 'date' && item.value) {
                obj[key].value = item.value.replace(/\./g, '/');
              }
            });

            if ($scope.issue && isAnyIssuesExist.length !== 0) {
              saveTaskForm(Issue.getIssues());
            } else {
              saveTaskForm();
            }
          }

          function saveTaskForm(issue) {
            tasks.saveChangesTaskForm($scope.taskForm, $scope.taskData, issue)
              .then(function(result) {
                $scope.taskForm.isInProcess = false;
                if (result.status == 500 || result.status == 403) {
                  var message = result.data.message;
                  var errMsg = (message.indexOf("errMsg") >= 0) ? message.split(":")[1].split("=")[1] : message;
                  Modal.inform.error(function(result) {})(errMsg + " " + (result && result.length > 0 ? (': ' + result) : ''));
                } else {
                  var sMessage = "Форму збережено.";
                  Modal.inform.success(function(result) {})(sMessage + " " + (result && result.length > 0 ? (': ' + result) : ''));
                }
              })
              .catch(defaultErrorHandler)
              .finally(function() {
                $rootScope.spinner = false;
                $scope.convertDisabledEnumFiedsToReadonlySimpleText();
                iGovNavbarHelper.loadTaskCounters($rootScope.tabMenu, true);
              });
          }
        };

        $scope.assignTask = function() {
          rollbackReadonlyEnumFields();
          $scope.taskForm.isInProcess = true;
          tasks.assignTask($scope.taskData.nID_Task, Auth.getCurrentUser().id)
            .then(function(result) {
              $scope.wasAssignedOnCurrentStep = true;
              Modal.assignTask(function(event) {
                $state.go('tasks.typeof.view', {
                  tab: 'tasks',
                  type: 'selfAssigned',
                  '#': 'selfAssigned',
                  sID_Order: $stateParams.sID_Order,
                  });
              }, 'Задача у вас в роботі', $scope.lightweightRefreshAfterSubmit);

            })
            .catch(defaultErrorHandler);
        };

        $scope.unassign = function() {
          rollbackReadonlyEnumFields();
          tasks.unassign($scope.taskData.nID_Task)
            .then(function() {
              $scope.selectTask($scope.taskData.nID_Task);
            })
            .then(function() {
              return tasks.getTask($scope.taskData.nID_Task);
            })
            .then(function(updatedTaskResult) {
              angular.copy(updatedTaskResult, task.getActivitiTaskObject($stateParams.sID_Order));
            })
            .catch(defaultErrorHandler);
        };

        $scope.upload = function(files, propertyID, inputId, win) {
          var content = {
            fieldId: propertyID,
            files: files
          };

          $rootScope.switchProcessUploadingState();
          $rootScope.spinner = true;
          $scope.headersTiny = null;
          tasks.uploadAttachToTaskForm(content, $scope.taskForm, $scope.taskData.oProcess.nID, $scope.taskId, inputId, win)
            .then(function(result) {
            if(content.fieldId === "sFileFromHTML"){
                $scope.headersTiny = result.response;
            }
              $rootScope.switchProcessUploadingState();
            })
            .finally(function () {
              $rootScope.spinner = false;
            });
        };

        $scope.lightweightRefreshAfterSubmit = function() {
          //lightweight refresh only deletes the submitted task from the array of current type of tasks
          //so we don't need to refresh the whole page
          iGovNavbarHelper.loadTaskCounters();
          $scope.taskForm.isInProcess = false;
          $scope.taskForm.isSuccessfullySubmitted = true;
        };

        $scope.sFieldLabel = function(sField) {
          var s = '';
          if (sField) {
            var a = sField.split(';');
            s = a[0].trim();
          }
          return s;
        };

        $scope.sFieldHint = function(sField) {
          if (sField)
            return sField.split(';')[1];
        };

        $scope.nID_FlowSlotTicket_FieldQueueData = function(sValue) {
          var oValue;
          try {
            oValue = angular.fromJson(sValue);
          } catch (err) {
            oValue = sValue;
          }
          var nID_FlowSlotTicket = 0;
          if (oValue.sID_Type && oValue.sID_Type === 'Qlogic') {
            nID_FlowSlotTicket = oValue.sDate.substring(0, oValue.sDate.indexOf(":") + 3);
            if (oValue.oTicket && oValue.oTicket['receiptNum']) {
              nID_FlowSlotTicket = nID_FlowSlotTicket + ' Талон №' + oValue.oTicket['receiptNum'];
            }
          } else {
            try {
              var nAt = sValue.indexOf(":");
              var nTo = sValue.indexOf(",");
              nID_FlowSlotTicket = sValue.substring(nAt + 1, nTo);;
            } catch (_) {
              nID_FlowSlotTicket = 1;
            }
          }
          return nID_FlowSlotTicket;
        };

        $scope.sDate_FieldQueueData = function(sValue) {
          var oValue;
          try {
            oValue = angular.fromJson(sValue);
          } catch (err) {
            oValue = sValue;
          }
          var sDate = "Дата назначена!";
          if (oValue.sID_Type && oValue.sID_Type === 'Qlogic') {
            sDate = oValue.sDate.substring(0, oValue.sDate.indexOf(":") + 3);
          } else {
            try {
              var nAt = sValue.indexOf("sDate");
              var nTo = sValue.indexOf("}");
              sDate = sValue.substring(nAt + 5 + 1 + 1 + 1, nTo - 1 - 6);
            } catch (_) {
              sDate = "Дата назначена!";
            }
          }
          return sDate;
        };

        $scope.sEnumValue = function(aItem, sID) {
          var s = sID;
          _.forEach(aItem, function(oItem) {
            if (oItem.id == sID) {
              s = oItem.name;
            }
          });
          return s;
        };

        $scope.getMessageFileUrl = function(oMessage, oFile) {
          if (oMessage && oFile)
            return './api/tasks/' + $scope.nID_Process + '/getMessageFile/' + oMessage.nID + '/' + oFile.sFileName;
        };

        $scope.getCurrentUserName = function() {
          var user = Auth.getCurrentUser();
          return user.firstName + ' ' + user.lastName;
        };

        $scope.getCurrentUserLogin = function() {
          var user = Auth.getCurrentUser();
          return user.id;
        };

        $scope.isCommentAfterReject = function(item) {
          if (item.id != "comment") return false;

          var decision = $.grep($scope.taskForm, function(e) {
            return e.id == "decide";
          });

          if (decision.length == 0) {
            // no decision
          } else if (decision.length == 1) {
            if (decision[0].value == "reject") return true;
          }
          return false;
        };

        angular.forEach($scope.taskForm, function(field, key, obj) {
          var id = 'id' in field ? 'id' : 'sId';
          var tableVar = aID_FieldPhoneUA.filter(function(i) {
            return field[id].indexOf(i) === 0 && field[id].split('_')[0] === i;
          });

          if (tableVar.length > 0) {
            if (field.type)
              obj[key].type = 'tel';
            else
              obj[key].sType = 'tel';

            obj[key].sFieldType = 'tel';
          }
        });

        // change "enum" field to "string" (https://github.com/e-government-ua/i/issues/751)
        $scope.convertDisabledEnumFiedsToReadonlySimpleText = function() {
          $scope.originalTaskForm = jQuery.extend(true, {}, $scope.taskForm);
          for (var i = 0; i < taskForm.length; i++) {
            if ($scope.originalTaskForm[i].type === "enum" && isItemFormPropertyDisabled($scope.originalTaskForm[i])) {
              /*for (var j = 0; j < $scope.originalTaskForm[i].enumValues.length; j++) {
                  if ($scope.originalTaskForm[i].value === $scope.originalTaskForm[i].enumValues[j].id) {
                      $scope.taskForm[i].value = $scope.originalTaskForm[i].enumValues[j].name;
                  }
              }
              Непонятно зачем так усложнять
              Закоментил потому как только мешало https://github.com/e-government-ua/i/issues/1915
              */
              try {
                var keyCandidate = $scope.originalTaskForm.taskData.aField[i].sValue;
                var objCandidate = $scope.originalTaskForm.taskData.aField[i].mEnum;
                $scope.taskForm.taskData.aField[i].sValue = objCandidate[keyCandidate];
              } catch (e) {
                Modal.inform.error()($scope.taskForm.taskData.message)
              }
            }
          }
        };

        function rollbackReadonlyEnumFields() {
          for (var i = 0; i < taskForm.length; i++) {
            if ($scope.originalTaskForm[i].type === "enum" && isItemFormPropertyDisabled($scope.originalTaskForm[i])) {
              $scope.taskForm[i].value = $scope.originalTaskForm[i].value;
              try {
                $scope.taskForm.taskData.aField[i].sType = $scope.originalTaskForm.taskData.aField[i].sType;
                $scope.taskForm.taskData.aField[i].sValue = $scope.originalTaskForm.taskData.aField[i].sValue;
              } catch (e) {
                Modal.inform.error()($scope.taskForm.taskData.message)
              }
            }
          }
        }
        $scope.convertDisabledEnumFiedsToReadonlySimpleText();

        $scope.copyLinkToBuffer = function(file) {
          var protocol = window.location.protocol;
          var hostname = window.location.host;
          var linkToCopy = protocol + '//' + hostname + '/api/tasks/download/'
            + file.sKey + '/attachment/' + file.storageType + '/' + file.fileName;

          var dummy = document.createElement('input');
          document.body.appendChild(dummy);
          dummy.setAttribute('id', 'dummy_id');
          document.getElementById('dummy_id').value = linkToCopy;
          dummy.select();
          document.execCommand('copy');
          document.getElementById('dummy_id').style.display = 'none';
          document.body.removeChild(dummy);
        };

        $scope.isFieldVisible = function(item) {
          return fieldsService.isFieldVisible(item, $scope.taskForm);
        };

        $scope.creationDateFormatted = function(date) {
          return fieldsService.creationDateFormatted(date);
        };

        //Asignee user.
        $scope.choiceUser = function(login) {
          for (var i = 0; i < taskData.aField.length; i++) {
            if (taskData.aField[i].sID.indexOf(sLoginAsignee) >= 0) {
              taskData.aField[i].sValue = login;
              break;
            }
          }
        };

        $scope.inUnassigned = function() {
          return $location.hash() === "unassigned";
        };

        $scope.tabHistoryAppealChange = function(param) {
          $scope.tabHistoryAppeal = param;
        };

        $scope.newPrint = function(form, item) {
          runCalculation(form);
          $scope.model.printTemplate = item;
          $scope.print(form, true);
        };

        $scope.isClarify = function(name) {
          return name.indexOf('writable=false') !== -1;
        };

        var activeFieldsList = [];
        angular.forEach($scope.taskForm, function(item) {
          if ($scope.isFieldVisible(item) &&
            !$scope.isFormPropertyDisabled(item) &&
            item.type !== 'invisible' &&
            item.type !== 'label' &&
            item.type !== 'markers') {
            activeFieldsList.push(item);
          }
        });

        $scope.insertOrdersSeparator = function(sPropertyId) {
          var oLine = FieldAttributesService.insertSeparators(sPropertyId);
          var oItem = null;
          if (oLine.bShow) {
            angular.forEach($scope.taskForm, function(item) {
              if (item.id == oLine.sLinkedFieldID) oItem = item;
            });
            if (oItem) {
              oLine.bShow = oItem.value && $scope.isFormPropertyDisabled(oItem);
            } else {
              oLine.bShow = false;
            }
          }
          return oLine;
        };

        $scope.insertSeparator = function(sPropertyId) {
          return FieldAttributesService.insertSeparators(sPropertyId);
        };

        $scope.isTableAttachment = function(item) {
          if (typeof item === 'object') {
            return item.type === 'table' || item.sType === 'table';
          } else {
            if (item)
              return item.indexOf('[table]') > -1;
          }
        };

        $scope.isUnDisabledFields = function() {
          return activeFieldsList.length > 0;
        };

        $scope.openTableAttachment = function(id, taskId, isNew) {
          $scope.attachIsLoading = true;

          if (!taskId && id.indexOf('sKey') > -1) {
            var key = JSON.parse(id);
            taskId = key.sKey;
            id = key.sID_StorageType;
          }

          tasks.getTableOrFileAttachment(taskId, id, isNew).then(function(res) {
            $scope.openedAttachTable = typeof res === 'object' ? res : JSON.parse(res);
            fixFieldsForTable($scope.openedAttachTable);
            $scope.attachIsLoading = false;
          });

          $scope.tableContentShow = !$scope.tableContentShow;
        };

        // проверяем имя поля на наличие заметок
        function fixName(item) {
          var sFieldName = item.name || '';
          var aNameParts = sFieldName.split(';');
          var sFieldNotes = aNameParts[0].trim();
          item.sFieldLabel = sFieldNotes;
          sFieldNotes = null;
          if (aNameParts.length > 1) {
            sFieldNotes = aNameParts[1].trim();
            if (sFieldNotes === '') {
              sFieldNotes = null;
            }
          }
          item.sFieldNotes = sFieldNotes;
        }

        /*
         * работа с таблицами
         */

        var fixFieldsForTable = function(table) {
          var tableRow;
          fixName(table);
          if ('content' in table) {
            tableRow = table.content;
            console.log(table);
            console.log(table.content);
          } else {
            tableRow = table.aRow;
          }
          angular.forEach(tableRow, function(row) {
            angular.forEach(row.aField, function(field) {
              fixName(field);
              if (field.type === 'date' && field.value || field.type === 'date' && field.props && field.props.value) {
                var match = /^[0-3]?[0-9].[0-3]?[0-9].(?:[0-9]{2})?[0-9]{2}$/.test(field.props.value);
                if (!match) {
                  var onlyDate = field.props.value.split('T')[0];
                  var splitDate = onlyDate.split('-');
                  field.props.value = splitDate[2] + '/' + splitDate[1] + '/' + splitDate[0]
                }
              }
              if (field.type === 'enum') {
                angular.forEach(field.a, function(item) {
                  if (field.value === item.id) {
                    field.value = item.name;
                  }
                })
              }
            })
          });
        };

        function isAnyTablesInForm(form) {
          return form.filter(function (item) {
            return item.type === 'table' || item.sType === 'table';
          });
        }

        var tables = isAnyTablesInForm($scope.taskForm);
        if (tables.length > 0) {
          $rootScope.spinner = true;
          TableService.init(tables, './api/table/getTable').then(function(){
            $rootScope.spinner = false;
          });
        }

        $scope.$on('TableFieldChanged', function(event, args) { $scope.updateTemplateList(); });

        //old service where we need to check the same id from form field and attachment to load it. remove it in a future.
        var idMatchInAttach = function() {
          angular.forEach($scope.taskForm, function(item, key, obj) {
            angular.forEach($scope.taskData.aAttachment, function(attachment) {
              var reg = /(\[id=(\w+)\])/;
              if (attachment.description) {
                var match = attachment.description.match(reg);
                if (match !== null && (item.id && match[2].toLowerCase() === item.id.toLowerCase() || item.name && match[2].toLowerCase() === item.name.toLowerCase())) {
                  tasks.getTableOrFileAttachment(attachment.taskId, attachment.id).then(function(res) {
                    obj[key] = JSON.parse(res);
                    obj[key].description = attachment.description;
                  })
                }
              }
            })
          });
        };

        var newServiceExistedTableDownload = function() {
          angular.forEach($scope.taskForm, function(item, key, obj) {
            if (item.type === "table") {
              try {
                var isDBJSON = JSON.parse(item.value);
                if (isDBJSON && isDBJSON.sKey && isDBJSON.sID_StorageType) {
                  tasks.getTableOrFileAttachment($scope.taskData.oProcess.nID, item.id, true).then(function(res) {
                    if (res && res.id) {
                      for (var t = 0; t < $scope.taskData.aField.length; t++) {
                        var table = $scope.taskData.aField[t];
                        if (table.sID === res.id) {
                          res.writable = table.bWritable;
                          res.readable = table.bReadable;
                          res.required = table.bRequired;
                        }
                      }
                    }
                    obj[key] = res;
                  })
                }
              } catch (e) {}
            }

            if (item.sType === "table") {
              try {
                var isDBJSON = JSON.parse(item.oValue);
                if (isDBJSON && isDBJSON.sKey && isDBJSON.sID_StorageType) {
                  var taskId = isDBJSON.sKey;
                  var id = isDBJSON.sID_StorageType;

                  tasks.getTableOrFileAttachment(taskId, id, true).then(function(res) {
                    if (res && res.id) {
                      for (var t = 0; t < $scope.taskData.aField.length; t++) {
                        var table = $scope.taskData.aField[t];
                        if (table.sId === res.id) {
                          res.sId = res.id;
                          delete res.id;
                          res.sType = res.type;
                          delete res.type;
                          // res.writable = false;
                          // res.readable = table.bReadable;
                          // res.required = table.bRequired;
                        }
                      }
                    }
                    obj[key] = res;
                  });
                }
              } catch (e) {}
            }
          })
        };

        idMatchInAttach();
        // newServiceExistedTableDownload();

        $rootScope.printHistory = function (name){
          var printContents = document.getElementById(name).innerHTML;
          var printTitle = document.getElementById('taskNumber').innerHTML;
          var popupWin = window.open('', '_blank');
          popupWin.document.open();
          popupWin.document.write('<html><head><link rel="stylesheet" type="text/css" href="style.css"/></head><body onload="window.print()"><h3 class="display-inline-block">Історія документу<h3/>'
            + printTitle + ' ' + printContents + '</body></html>');
          popupWin.document.close();
        };

        $scope.print = function(form, isMenuItem) {

          if (isMenuItem !== true) { // Click on Button
            $scope.updateTemplateList();
          }

          if (($scope.printTemplateList.length === 0 || isMenuItem === true) && $scope.taskData && $scope.taskForm) {
            rollbackReadonlyEnumFields();
            $scope.printModalState.show = !$scope.printModalState.show;
          }
        };

        $scope.addRow = function(form, id, index) {
          ValidationService.validateByMarkers(form, $scope.markers, true, null, true);
          if (!form.$invalid) {
            $scope.tableIsInvalid = false;
            TableService.addRow(id, $scope.taskForm);
          } else {
            $scope.tableIsInvalid = true;
            $scope.invalidTableNum = index;
          }
        };

        $scope.removeRow = function(index, form, id) {
          TableService.removeRow($scope.taskForm, index, id);
          if (!form.$invalid) {
            $scope.tableIsInvalid = false;
          }
        };
        $scope.clearRow = function(index, id) {
          TableService.clearRow($scope.taskForm, index, id);
        };

        $scope.rowLengthCheckLimit = function (table) {
          return TableService.rowLengthCheckLimit(table);
        };

        $scope.isFieldWritable = function(field) {
          return TableService.isFieldWritable(field);
        };

        $scope.updateTemplateList = function() {
          $scope.printTemplateList = PrintTemplateService.getTemplates($scope.taskForm);
          if ($scope.printTemplateList.length > 0) {
            var aFileFields = $scope.taskForm.filter(function(field) {
              return field.type === 'file' && field.options.hasOwnProperty('sID_Field_Printform_ForECP');
            });
            if (aFileFields && aFileFields.length > 0) {
              angular.forEach($scope.printTemplateList, function(oTemplate) {
                angular.forEach(aFileFields, function(oFileField) {
                  if (oFileField.options['sID_Field_Printform_ForECP'] === oTemplate.id) {
                    oTemplate.printFormLinkedToFileField = oFileField.id;
                  }
                })
              })
            }
            $scope.model.printTemplate = $scope.printTemplateList[0];
          }
          return true;
        };

        $scope.tableIsLoaded = function(item) {
          return item.aRow && typeof item.aRow[0] !== 'number';
        };

        $scope.isVisible = function(field) {
          return TableService.isVisible(field);
        };

        $scope.searchingTablesForPrint = function() {
          angular.forEach($scope.taskData.aAttachment, function(attachment) {
            if (attachment.description) {
              var tableID = attachment.description.match(/(\[id=(\w+)\])/);
              if (tableID !== null && tableID.length === 3) {
                tasks.getTableOrFileAttachment(attachment.taskId, attachment.id).then(function(res) {
                  var table = JSON.parse(res);
                  fixFieldsForTable(table);
                  $scope.taskData.aTable.push(table);
                })
              }
            }
          });

          angular.forEach($scope.taskData.aNewAttachment, function(attachment) {
            if (attachment.type === 'table' && attachment.value && attachment.value.indexOf('sKey') > -1) {
              try {
                var data = JSON.parse(attachment.value);
                tasks.getTableOrFileAttachment($scope.taskData.oProcess.nID, attachment.id, true).then(function(res) {
                  if (res.type && res.type === 'table') {
                    fixFieldsForTable(res);
                    $scope.taskData.aTable.push(res);
                  }
                })
              } catch (e) {
                console.log('Помилка в таблицi ' + attachment.id + ' ' + e)
              }
            }
          });
          $scope.updateTemplateList();
        };
        $scope.searchingTablesForPrint();

        $scope.getPositionList = function (sFind) {
          Staff.getHumanPositions(sFind).then(function (res) {
            if (res && res.length && !res.code) {
              $scope.positionList = res;
            }
          });
        };

        $scope.onSelectPositionList = function(field, positionObj) {
          field.value = positionObj.sNote;

          angular.forEach($scope.taskForm, function(item) {
            if (item.id.indexOf('sName_SubjectHumanPosition') > -1)
              item.value = positionObj.sName;
          });
        };

        /*
         * работа с таблицами
         */

        $scope.showField = function(field) {
          return !isJSONinHistory(field);
        };

        function isJSONinHistory(field) {
          return $scope.sSelectedTask === 'finished' && angular.isString(field.value) && field.value.length > 0 && (
            (field.value.charAt(0) === '{' && field.value.charAt(field.value.length - 1) === '}') ||
            (field.value.charAt(0) === '[' && field.value.charAt(field.value.length - 1) === ']'));
        }

        // $scope.openUsersHierarchy = function() {
        //   $scope.attachIsLoading = true;
        //   DocumentsService.getProcessSubjectTree($scope.taskData.oProcess.nID).then(function(res) {
        //     $scope.documentFullHierarchy = res;
        //     $scope.attachIsLoading = false;
        //     eaTreeViewFactory.setItems($scope.documentFullHierarchy.aProcessSubjectTree, $scope.$id);
        //   });
        //
        //   $scope.usersHierarchyOpened = !$scope.usersHierarchyOpened;
        // };

        $scope.assignAndSubmitDocument = function(docForm, isNeedEDS, isEdit, sSignType) {
          var user;
          if (JSON.parse($cookies.get('user'))){
            user = JSON.parse($cookies.get('user'));
          } else {
            user = Auth.getCurrentUser();
          }
          $rootScope.isEdit = false;
          if (isEdit) {
            $rootScope.isEdit = isEdit;
          }
          $scope.validateForm(docForm);


          if (!docForm.$invalid) {
            $scope.isFormInvalid = false;
            $rootScope.spinner = true;

            tasks.getTasksByOrder('' + $scope.nID_Process + lunaService.getLunaValue($scope.nID_Process)).then(function(result) {
              var aIds = JSON.parse(result);
              if (angular.isArray(aIds) && aIds.includes('' + $scope.taskId)) {
                $scope.wasAssignedOnCurrentStep = true;
                var navNext = $scope.navigateInfo && $scope.navigateInfo.task ? $scope.navigateInfo.task.next : null;
                if (sSignType) {
                  $scope.submitTask(docForm, true, isNeedEDS, navNext, false, sSignType, true);
                } else {
                  $scope.submitTask(docForm, true, isNeedEDS, navNext, false, null, true);
                }
              } else {
                var navNext = $scope.navigateInfo && $scope.navigateInfo.task ? $scope.navigateInfo.task.next : null;
                $scope.submitTask(docForm, true, isNeedEDS, navNext);
              }

            }).catch(defaultErrorHandler).finally(function() {
              iGovNavbarHelper.loadTaskCounters($rootScope.tabMenu, true);

              //hardcode: fix reload after clicking "Edit" btn
              setTimeout(function(){
                if ($scope.isTaskSuccessfullySubmitted() && $rootScope.spinner)
                  $state.reload();
              },2000);
            });

          } else {
            $scope.isFormInvalid = true;
          }
        };

        function getGroupsNote(logins) { //get groups name sNote and order for showing from step sKeyStep
          if (logins) {
            var result = {};
            $scope.groupsNote = [];
            var groupsNote = []
            for (var i = 0; i < logins.length; i++) {
              if (logins[i].sKeyStep !== '_') {
                var str = logins[i].oDocumentStepType.sNote;
                var step = logins[i].sKeyStep.split('_');
                result[str + '_' + step[1]] = true;
              }
              if(logins[i].sKeyStep === '_'){
                var str = logins[i].oDocumentStepType.sNote;
                var step = "default";
                result[str + '_' + step] = true;
              }
            }

            var resultArray = Object.keys(result);
            for (var j = 0; j < resultArray.length; j++) {
              var part = resultArray[j].split('_');
              result = new getStepObj(part[0], part[1]);
              groupsNote.push(result);
            }
            $scope.groupsNote = filterNameStep(groupsNote); // get unique meaning

          }
        }
        getGroupsNote(documentLogins);

        function filterNameStep(groupsNote) {
          var group = [];

          nextName:
            for (var i = 0; i < groupsNote.length; i++) {
              var strNote = groupsNote[i].namestep;
              for (var j = 0; j < group.length; j++) {
                if (group[j].namestep == strNote && group[j]) continue nextName;
              }
              group.push(groupsNote[i]);
            }
          return group;
        }

        function getWatchStepLogins(logins) {
          var loginsFromDefaultStep = [];
          var loginsToRemove = [];
          if (logins){
            for (i = 0; i < logins.length; i++){
              if (logins[i].sKeyStep === '_' && logins[i].aUser.length !== 0) {
                loginsFromDefaultStep.push(logins[i]);
              }
            }

            for (var j = 0; j < loginsFromDefaultStep.length; j++){
              for(i = 0; i < logins.length; i++){
                angular.forEach(logins[i].aUser, function (user) {
                  angular.forEach(loginsFromDefaultStep[j].aUser, function (defaultUser) {
                    if (user && defaultUser && defaultUser.sLogin && user.sLogin === defaultUser.sLogin){
                      if (logins[i].sKeyStep !== loginsFromDefaultStep[j].sKeyStep && logins[i].bWrite !== null){
                        loginsToRemove.push(defaultUser.sLogin);
                      }
                    }
                  })
                });
              }
            }
            for (i = 0; i < loginsFromDefaultStep.length; i++){
              angular.forEach(loginsFromDefaultStep[i].aUser, function (user) {
                if (loginsToRemove.indexOf(user.sLogin) >= 0 && user.sLogin === user.sID_Group){
                  loginsFromDefaultStep.splice(i, 1);
                  i = -1;
                }
              })
            }
            $scope.watchLogins = loginsFromDefaultStep;
          }
        }
        getWatchStepLogins(documentLogins);

        function setDeleteBtnToLogins(logins, isWatchLogins) {
          var sCurrDocumentStep = taskData.mProcessVariable.sKey_Step_Document;
          var currentUser = $scope.getCurrentUserLogin();

          angular.forEach(logins, function(item) {
            if ((item.sKeyStep === sCurrDocumentStep || isWatchLogins) && item.sKey_GroupAuthor === currentUser)
              item.showDeleteBtn = true;
          });
        }
        setDeleteBtnToLogins($scope.documentLogins);
        setDeleteBtnToLogins($scope.watchLogins, true);

        $scope.removeUserFromDoc = function(user, isWatchLogins) {
          var sCurrDocumentStep = taskData.mProcessVariable.sKey_Step_Document;
          var currentUser = $scope.getCurrentUserLogin();
          var sCurrReferent = $cookies.getObject('referent');
          sCurrReferent = sCurrReferent ? sCurrReferent.id : currentUser;

          var userToRemove = user.aUser.filter(function(item) {
            return item.sLogin === item.sID_Group;
          });
          userToRemove = userToRemove.length ? userToRemove[0].sID_Group : '';

          var params = {
            snID_Process_Activiti: taskData.oProcess.nID,
            sKey_Group: userToRemove,
            sKey_GroupAuthor: currentUser,
            sKey_Step: isWatchLogins ? '_' : sCurrDocumentStep,
            sLogin: currentUser,
            sLoginReferent: sCurrReferent,
            bSubmit: true
          };

          DocumentsService.removeDocumentStepSubject(params).then(function(res) {
            if (res.snID_Task) {
              $scope.taskData.nID_Task = parseInt(res.snID_Task);
              $scope.taskId = parseInt(res.snID_Task);
              $scope.updateListOfAcceptors();
              Modal.inform.success()("Учасника документу видалено успішно");
            } else
              Modal.inform.error()("Не вдалося видалити учасника документу");
          });
        };

        function getStepObj(namestep, step) {
          this.namestep = namestep;
          this.step = step;
        }

        $scope.isDocumentNotSigned = false;

        (function isDocSignedCheck() {
          if (!documentRights) return true;
          var notSigned = $scope.documentLogins.filter(function(login) {
            return !login.sDate && login.aUser.length > 0;
          });
          var currentUser = $scope.getCurrentUserLogin();
          for (var i = 0; i < notSigned.length; i++) {
            for (var l = 0; l < notSigned[i].aUser.length; l++) {
              if (notSigned[i].aUser[l].sLogin === currentUser && notSigned[i].sKeyStep === $scope.taskData.mProcessVariable.sKey_Step_Document) {
                $scope.isDocumentNotSigned = true;
                break;
              }
            }
          }
        })();

        $scope.isDocumentEdit = function() {
          var user = JSON.parse($cookies.get('user'));
          for (var i = 0; i < $scope.taskForm.length; i++) {
            if ($scope.taskForm[i].id === 'bAuthorEdit' && (($scope.taskData.mProcessVariable.sLoginAuthor === iGovNavbarHelper.currentUser.id) || (user.id === $scope.taskData.mProcessVariable.sLoginAuthor))) {
              return true;
            }
          }
        };

        // блокировка кнопок вібора файлов на время выполнения процесса загрузки ранее выбранного файла
        $rootScope.isFileProcessUploading = {
          bState: false
        };

        $rootScope.switchProcessUploadingState = function() {
          $rootScope.isFileProcessUploading.bState = !$rootScope.isFileProcessUploading.bState;
          console.log("Switch $rootScope.isFileProcessUploading to " + $rootScope.isFileProcessUploading.bState);
        };

        $scope.viewTrustedHTMLContent = function(html) {
          return $sce.trustAsHtml(html);
        };
        $scope.getOrgData = function(code, id) {
          var fieldPostfix = id.replace('sID_SubjectOrgan_OKPO_', '');
          var keys = { activities: 'sID_SubjectActionKVED', ceo_name: 'sCEOName', database_date: 'sDateActual', full_name: 'sFullName', location: 'sLocation', short_name: 'sShortName' };

          function findAndFillOKPOFields(res) {
            angular.forEach(res.data, function(data, key) {
              if (key in keys) {
                for (var i = 0; i < $scope.taskForm.length; i++) {
                  var prop = $scope.taskForm[i].id;
                  if (prop.indexOf(keys[key]) === 0) {
                    var checkPostfix = prop.split(/_/),
                      elementPostfix = checkPostfix.length > 1 ? checkPostfix.pop() : null;
                    if (elementPostfix !== null && elementPostfix === fieldPostfix)
                      if (prop.indexOf('sID_SubjectActionKVED') > -1) {
                        var onlyKVEDNum = data.match(/\d{1,2}[\.]\d{1,2}/),
                          onlyKVEDText = data.split(onlyKVEDNum)[1].trim(),
                          pieces = prop.split('_');
                        onlyKVEDNum.length !== 0 ? $scope.taskForm[i].value = onlyKVEDNum[0] : $scope.taskForm[i].value = data;

                        pieces.splice(0, 1, 'sNote_ID');
                        var autocompleteKVED = pieces.join('_');
                        if (prop === autocompleteKVED)
                          $scope.taskForm[i].value = onlyKVEDText;
                      } else {
                        $scope.taskForm[i].value = data;
                      }
                  }
                }
              }
            })
          }

          function clearFieldsWhenError() {
            for (var i = 0; i < $scope.taskForm.length; i++) {
              var prop = $scope.taskForm[i].id;
              if ($scope.data.formData.params.hasOwnProperty(prop) && prop.indexOf('_SubjectOrgan_') > -1) {
                var checkPostfix = prop.split(/_/),
                  elementPostfix = checkPostfix.length > 1 ? checkPostfix.pop() : null;
                if (elementPostfix !== null && elementPostfix === fieldPostfix && prop.indexOf('sID_SubjectOrgan_OKPO') === -1)
                  $scope.taskForm[i].value = '';
              }
            }
          }
          if (code) {
            $scope.orgIsLoading = { status: true, field: id };
            tasks.getOrganizationData(code).then(function(res) {
              $scope.orgIsLoading = { status: false, field: id };
              if (res.data === '' || res.data.error) {
                clearFieldsWhenError();
              } else {
                findAndFillOKPOFields(res);
              }
            });
          }
        };

        $scope.isOKPOField = function(i) {
          if (i) {
            var splitID = i.split(/_/);
            if (splitID.length === 4 && splitID[1] === 'SubjectOrgan' && splitID[2] === 'OKPO') {
              return true
            }
          }
        };

        $scope.isRemoveDocumentButtonVisible = function() {
          return $scope.taskData.mProcessVariable && $scope.taskData.mProcessVariable.sLoginAuthor === iGovNavbarHelper.currentUser.id && $scope.sSelectedTask !== 'finished' && $scope.isDocument;
        };

        $scope.openAttachFile = function () {
          Modal.inform.error()('<p style="text-align:center">Неможливо переглянути цей файл у браузері.<br> Переглядати можливо наступні форматі: pdf, txt, html, jpg, jpeg, gif, png, bmp. <br> Скористуйтеся кнопкою <strong>Завантажити</strong> та перегляньте файл на компьютері.</p>' );
        };

        $scope.removeDocument = function(nID_Process) {
          Modal.confirm.delete(function(event) {
            $scope.taskForm.isInProcess = true;
            DocumentsService.removeDocumentSteps(nID_Process)
              .then(function() {
                $scope.taskForm.isInProcess = false;
                if ($scope.filteredTasks && $scope.filteredTasks.length > 0) {
                  for (var taskIndex = 0; taskIndex < $scope.filteredTasks.length; taskIndex++) {
                    if ($scope.filteredTasks[taskIndex].processInstanceId === nID_Process) {
                      $scope.filteredTasks.splice(taskIndex, 1);
                      iGovNavbarHelper.loadTaskCounters($rootScope.tabMenu, true);
                      break;
                    }
                  }
                }
                $state.go('tasks.typeof', {tab: $rootScope.tabMenu, type: $scope.sSelectedTask}, {reload: true});
                iGovNavbarHelper.loadTaskCounters($rootScope.tabMenu, true);
              }, function() {
                $scope.lightweightRefreshAfterSubmit();
              });
          })('документ');
        };

        $scope.getBpAndFieldID = function(field) {
          if ($scope.taskData && $scope.taskData.oProcess && $scope.taskData.oProcess.sBP) {
            return $scope.taskData.oProcess.sBP.split(':')[0] + "_--_" + field.id;
          } else {
            return field.id;
          }
        };

        $scope.getScope = function() {
          console.log($scope);
        };

        $scope.getFullCellId = function(field, column, row) {
          if ($scope.taskData && $scope.taskData.oProcess && $scope.taskData.oProcess.sBP) {
            return $scope.taskData.oProcess.sBP.split(':')[0] + "_--_" + field.id + "_--_" + "COL_" + field.aRow[0].aField[column].id + "_--_" + "ROW_" + row;
          } else {
            return field.id + "_--_" + "COL_" + field.aRow[0].aField[column].id + "_--_" + "ROW_" + row;
          }
        };

        $scope.switchDelegateMenu = function() {
          $rootScope.delegateSelectMenu = !$rootScope.delegateSelectMenu;
        };

        $scope.switchAcceptMenu = function() {
          $rootScope.acceptSelectMenu = !$rootScope.acceptSelectMenu;
        };

        $scope.switchVisorMenu = function() {
          $rootScope.visorSelectMenu = !$rootScope.visorSelectMenu;
        };

        $scope.switchViewerMenu = function() {
          $rootScope.viewerSelectMenu = !$rootScope.viewerSelectMenu;
        };

        $scope.getBpAndFieldID = function(field) {
          if ($scope.taskData && $scope.taskData.oProcess && $scope.taskData.oProcess.sBP) {
            return $scope.taskData.oProcess.sBP.split(':')[0] + "_--_" + field.id;
          } else {
            return field.id;
          }
        };

        $scope.getFullCellId = function(field, column, row) {
          if ($scope.taskData && $scope.taskData.oProcess && $scope.taskData.oProcess.sBP) {
            return $scope.taskData.oProcess.sBP.split(':')[0] + "_--_" + field.id + "_--_" + "COL_" + field.aRow[0].aField[column].id + "_--_" + "ROW_" + row;
          } else {
            return field.id + "_--_" + "COL_" + field.aRow[0].aField[column].id + "_--_" + "ROW_" + row;
          }
        };

        function toggleMenu(status) {
          if (typeof status === 'boolean') {
            if (status) {
              $scope.isMenuOpened = true;
              snapRemote.open('left');
            } else {
              $scope.isMenuOpened = false;
              snapRemote.close();
            }
            sessionStorage.setItem('menu-status', JSON.stringify(status));
          }
        }

        var menuStatus = sessionStorage.getItem('menu-status');
        if (menuStatus) {
          var status = JSON.parse(menuStatus);
          toggleMenu(status);
        } else {
          $scope.isMenuOpened = false;
          snapRemote.close();
        }

        $rootScope.tabMenu = Tab.getCurrentTab($location.url(), $stateParams, $location.hash());

        $rootScope.$broadcast("update-search-counter");

        snapRemote.getSnapper().then(function(snapper) {
          snapper.on('animated', function() {
            if (snapper.state().state === 'closed') {
              $scope.isMenuOpened = false;
              $scope.$apply();
            } else if (snapper.state().state === 'left') {
              $scope.isMenuOpened = true;
              $scope.$apply();
            }
          });
        });

        $scope.historyBack = function() {
          $state.go('tasks.typeof', { tab: 'tasks', type: $stateParams.type || $location.hash() });
        };

        Tab.navigateTask(
          $rootScope.tasksOrders,
          $stateParams.sID_Order,
          $rootScope.tabMenu,
          $scope.sSelectedTask).then(function (res) {
          if (res && 'nextPage' in res) {
            $rootScope.tasksOrders.order = $rootScope.tasksOrders.order.concat(res.nextPage);
            $scope.navigateInfo = res.data ? res.data: res;
            _.defer(function(){$scope.$apply();});
          } else {
            $scope.navigateInfo = res && res.data ? res.data: res;
          }
        });

        $scope.nextOrPrevTask = function (direction) {
          $rootScope.spinner = true;
          var nf = $scope.navigateInfo;
          if (nf && direction in nf.task && nf.task[direction] !== null)
            $state.go('tasks.typeof.view', {
              tab: nf.tab,
              type: nf.type,
              sID_Order: direction === 'next' ? nf.task.next : nf.task.previous,
              '#': nf.type
            });
          else
            $state.go('tasks.typeof', { tab: $rootScope.tabMenu, type: $scope.sSelectedTask }, {reload: true});
        };

        $scope.isNeedECP = function() {
          var documentStep = taskData.mProcessVariable.sKey_Step_Document;
          for (var key in documentLogins) {
            if (documentLogins[key].aUser.length > 0 && documentLogins.hasOwnProperty(key)) {
              if (documentLogins[key].aUser[0].sID_Group === $scope.getCurrentUserLogin() && documentLogins[key].sKeyStep === documentStep) {
                if ($scope.sSelectedTask === 'ecp')
                  return true;
                else
                  return documentLogins[key].bNeedECP && $rootScope.checkboxForAutoECP && !$rootScope.checkboxForAutoECP.status;
              }
            }
          }
          return false;
        };

        $scope.getLinkToDocument = function(sID_Order) {
          $rootScope.spinner = true;
          searchService.searchTabByOrder(sID_Order).then(function (tabData) {
            var tabName = iGovNavbarHelper.tabName[tabData.oTab.sDocumentStatus];
            $state.go('tasks.typeof.view', { tab: 'documents',
              type: tabName,
              sID_Order: sID_Order,
              '#': tabName
            });
          });
        };

        $scope.execCtrlModals = {
          bExecTransferDate: false,
          bNotExecuted: false,
          bReport: false,
          bDelegate: false,
          bCtrlTransferDate: true,
          bRejected: false,
          bAskMessage: false,
          bAskMessageAnswer: false,
          bEditMessage: false,
          bSignInfo: false,
          bDelegates: false,
          bAddViewer: false,
          bAddVisor: false,
          bAddAcceptor: false
        };

        $scope.isModalByButtonIsOpened = false;

        $scope.openModalByButton = function(action, nID_Message_Parent, sBody, sKeyGroup, form) {
          for (var modal in $scope.execCtrlModals) {
            if ($scope.execCtrlModals.hasOwnProperty(modal) && modal === action) {
              angular.forEach($scope.execCtrlModals, function(value, key, obj) {
                obj[key] = false;
              });
              $scope.execCtrlModals[modal] = $scope.isModalByButtonIsOpened = true;
            }
          }


          if(action=='bAskMessage'){
             setTimeout(function() {
              //alert("123");
              $(".custom-ng-modal-overlay").hide();
             }, 300);
           }

          if ($scope.chatMessages && action.indexOf('bAskMessageAnswer') > -1)
            angular.forEach($scope.chatMessages.aProcessChat, function(item) {
              angular.forEach(item.aProcessChatMessage, function (message) {
                if (message.nID === nID_Message_Parent) {
                  $scope.sAuthor = message.sFIO_Author;
                  $scope.sReferent = message.sFIO_Referent;
                }
              });
            });

          $scope.nID_Message_Parent = nID_Message_Parent;
          $scope.sBody = sBody;
          $scope.form = form;
          $scope.sKeyGroup = sKeyGroup;
        };

        $scope.toggleIssueEdit = function() {
          $scope.isIssueEdit = !$scope.isIssueEdit;
        };

        $scope.editAndSubmit = function(form) {
          if ($scope.taskData.sLoginAssigned === null) {
            $scope.wasAssignedOnCurrentStep = true;
            $scope.submitTask(form, false, false, null, false, null, true);
          } else {
            $scope.submitTask(form);
          }
        };

        $scope.closeModalByButton = function() {
          $scope.isModalByButtonIsOpened = false;
        };

        if ($scope.taskData.aProcessSubjectTask && $scope.taskData.aProcessSubjectTask.length !== 0) {
          $scope.currentUserRole = ExecAndCtrlService.getCurrentRole($scope.taskData.aProcessSubjectTask[0].aProcessSubject, $scope.sSelectedTask);
        }

        setTimeout(function() {
          Issue.isIssue($scope.taskForm).then(function(answ) {
            $scope.issue = answ;
            if ($scope.issue && (!$scope.taskData.mProcessVariable || $scope.taskData.mProcessVariable && !$scope.taskData.mProcessVariable.aProcessSubjectTask)) {
              Issue.clearIssues();
            }
          });
          $scope.addIssue = function() {
            //$scope.isIssueEdit = true;
            $scope.disablePastDays = { minDate: new Date() };
            $scope.disablePastDays.minDate.setHours(0,0,0,0);

            $scope.isIssueAdd = true;
            $scope.issueValid = Issue.addIssue();
          };
        }, 1000);

        if ($scope.taskData.mProcessVariable && 'aProcessSubjectTask' in $scope.taskData.mProcessVariable) {
          if (angular.isArray($scope.taskData.mProcessVariable.aProcessSubjectTask) && $scope.taskData.mProcessVariable.aProcessSubjectTask.length > 0) {
            Issue.clearIssues();
            angular.forEach($scope.taskData.mProcessVariable.aProcessSubjectTask, function(issue) {
              Issue.pushIssue(issue);
            });
            IsSavedIssueEdit();
          }

          for (var i = 0; i < $scope.taskForm.length; i++) {
            if ($scope.taskForm[i].id && $scope.taskForm[i].id.indexOf('oProcessSubject_Executor') === 0 && $scope.taskForm[i].value.indexOf('sKey') > -1) {
              Issue.isIssue($scope.taskForm, $scope.taskForm[i]).then(function(answ) {
                $scope.issue = answ;
                IsSavedIssueEdit();
                if (taskData.mProcessVariable && taskData.mProcessVariable.sKey_Step_Document && taskData.mProcessVariable.sKey_Step_Document === "step_1"){
                  $scope.isIssueAdd = true;
                }
              });
            }
          }
        }

        var getDocRightsLength = function() {
          var length = 0;
          angular.forEach(docRights, function(value, key) {
            if (key !== 'CreateTask' && key !== 'EditTask' && value === true) {
              length = length + 1;
            }
          });
          return length;
        };

        var docRights = {
          Delegate: false,
          AddAcceptor: false,
          AddVisor: false,
          AddViewer: false,
          DeleteDocument: false,
          CreateTask: false,
          EditTask: false,
          CancelDocumentSubmit: false,
          RemoveDocumentStepSubject: false,
          AskMessage: false,
          Refuse: false,
          Seen: false,
          SetUrgent: false,
          Edit: false
        };

        var checkDocumentSubjectRightPermition = function() {
          if (documentRights && documentRights.aDocumentSubjectRightPermition) {
            for (var i = 0; i < documentRights.aDocumentSubjectRightPermition.length; i++) {
                if (documentRights.aDocumentSubjectRightPermition[i].permitionType in docRights) {
                docRights[documentRights.aDocumentSubjectRightPermition[i].permitionType] = true;
                  if (documentRights.aDocumentSubjectRightPermition[i].sID_Group_Activiti) {
                    docRights[documentRights.aDocumentSubjectRightPermition[i].permitionType + "_Group_Activiti"] = documentRights.aDocumentSubjectRightPermition[i].sID_Group_Activiti;
                  }
                  if (documentRights.aDocumentSubjectRightPermition[i].soValue) {
                    docRights[documentRights.aDocumentSubjectRightPermition[i].permitionType + "_soValue"] = JSON.parse(documentRights.aDocumentSubjectRightPermition[i].soValue);
                  }
              }
            }
            $rootScope.docRights = docRights;
          }
          $scope.getDocRightsLength = getDocRightsLength();
        };
        checkDocumentSubjectRightPermition();

        $scope.cancelDocumentSubmit = function() {
          var params = {
            snID_Process_Activiti: $scope.nID_Process,
            sKey_Step: taskData.mProcessVariable.sKey_Step_Document,
            sKey_Group: Auth.getCurrentUser().id,
            nID_Task: $scope.taskId
          };
          DocumentsService.cancelDocumentSubmit(params).then(function(response) {
            $rootScope.spinner = true;
            Modal.inform.signed(function() {})('Підпис знято');
          })
            .finally(function() {
              $rootScope.spinner = false;
            });
        };

        $scope.removeDocumentStepSubject = function() {
          var params = {
            snID_Process_Activiti: $scope.nID_Process,
            sKey_Step: taskData.mProcessVariable.sKey_Step_Document,
            sKey_Group: Auth.getCurrentUser().id,
            nID_Task: $scope.taskId
          };
          DocumentsService.removeDocumentStepSubject(params).then(function(response) {
            Modal.inform.signed(function() {})('Підпис не потрібен');
          });
        };

        $scope.updateListOfAcceptors = function(updatedLogins) {
          getGroupsNote(documentLogins);
          
          if (updatedLogins && updatedLogins.length) {
            $scope.documentLogins = updatedLogins;
            getWatchStepLogins($scope.documentLogins);

            setDeleteBtnToLogins($scope.documentLogins);
            setDeleteBtnToLogins($scope.watchLogins, true);
          } else
            tasks.getDocumentStepLogins(taskData.oProcess.nID, $scope.sSelectedTask).then(function(response) {
              $scope.documentLogins = response;
              getWatchStepLogins($scope.documentLogins);

              setDeleteBtnToLogins($scope.documentLogins);
              setDeleteBtnToLogins($scope.watchLogins, true);
            });
        };

        $scope.deleteProcessChatMessage = function(nID_ProcessChatMessage, sKeyGroup_Author) {
          var taskServer = CurrentServer.getServer();

          var sCurrReferent = $cookies.getObject('referent');
          sCurrReferent = sCurrReferent ? sCurrReferent.id : '';

          Modal.confirm.delete(function(event) {
            var request = {
              method: 'DELETE',
              url: '/api/chat/deleteProcessChatMessage',
              data: {},
              params: {
                nID_ProcessChatMessage: nID_ProcessChatMessage,
                sLogin: sKeyGroup_Author,
                taskServer: taskServer.another ? taskServer.name : null,
                sLoginReferent: sCurrReferent ? sCurrReferent : sLogin
              }
            };
            $http(request).success(function() {
              $scope.updateChat();
            });
          })('коментар');
        };

        $scope.updateChat = function() {
          DocumentsService.getProcessChat(taskData.oProcess.nID).then(function(response) {
            $scope.chatMessages = response;
          })
        };

        $scope.setUrgentStatus = function(sLogin, sKeyStep, bWholeStep, bUrgent){
          $rootScope.spinner = true;
          var request = {
            method: 'GET',
            url: '/api/documents/setDocumentUrgent',
            params: {
              snID_Process_Activiti: taskData.oProcess.nID,
              sKey_Group_Editor: Auth.getCurrentUser().id,
              sKey_Step: sKeyStep,
              sKey_Group_Urgent: sLogin,
            }
          }
          if (bWholeStep) {
            if ($scope.checkStepIsUrgent(sKeyStep) === false){
              request.params.bUrgent = true;
            }
          } else {
            if (bUrgent === true){
              request.params.bUrgent = true;
            }
          }
          $http(request).success(function (data){
            $state.reload().then(function () {
              $rootScope.spinner = false;
              if (request.params.bUrgent){
                Modal.inform.success()("Екстреність успішно додана")
              } else {
                Modal.inform.success()("Екстреність успішно знята")
              }
            });
          });
        }

        $scope.checkStepIsUrgent = function (currentStep) {
          var tempArray = [];
          for (i = 0; i < documentLogins.length; i++){
            if (documentLogins[i].sKeyStep === currentStep) {
              tempArray.push(documentLogins[i].bUrgent);
            }
          }
          return tempArray.every(function (element, index, array) {
            return element === true;
          });
        };

        $scope.getShortName = function (name) {
          var nameArray = name.split(' ');
          for (var i = 1; i < nameArray.length; i++) {
            nameArray[i] = nameArray[i].slice(0, 1).concat(".");
          }
          return nameArray.join(" ");
        };
      }
    ])
})();
