(function() {
  'use strict';

  angular
    .module('dashboardJsApp')
    .config(tasksConfig);

  tasksConfig.$inject = ['$stateProvider'];
  function tasksConfig($stateProvider) {
    $stateProvider
      .state('tasks', {
        abstract: true,
        url: '',
        controller: 'TasksBaseCtrl',
        access: {
          requiresLogin: true
        },
        resolve: {
          tasksStateModel: function () {
            return {};
          },
          stateModel: function () {
            return {
              printTemplate: null,
              taskDefinition: null,
              strictTaskDefinition: null,
              userProcess: null
            }
          },
          processesList: [
            'processes',
            function (processes) {
              return processes.list();
            }
          ]
        }
      })
      .state('tasks.typeof', {
        url: '/:tab?:type',
        access: {
          requiresLogin: true
        },
        views: {
          '@': {
            templateUrl: 'app/tasks/tasks.html',
            controller: 'TasksCtrl'
          }
        }
      })
      .state('tasks.typeof.create', {
        url: '^/:tab/create:typeTab',
        templateUrl: 'app/tasks/createView.html',
        controller: 'createView',
        access: {
          requiresLogin: true
        }
      })
      .state('tasks.typeof.newtask', {
        url: '^/new/:id',
        templateUrl: 'app/tasks/createTask.html',
        controller: 'createTask',
        access: {
          requiresLogin: true
        }
      })
      .state('tasks.typeof.view', {
        url: '^/:tab/sID_Order=:sID_Order',
        templateUrl: 'app/tasks/taskView.html',
        controller: 'TaskViewCtrl',
        access: {
          requiresLogin: true
        },
        resolve: {
          currentTaskServer: [
            '$stateParams',
            'CurrentServer',
            '$q',
            function ($stateParams, CurrentServer, $q) {
              var deferred = $q.defer();

              CurrentServer.setServer($stateParams.sID_Order).then(function (response) {
                deferred.resolve(response);
              });

              return deferred.promise;
          }],
          isSubTabForRedirect: [
            'searchService',
            '$stateParams',
            '$q',
            '$location',
            '$state',
            'currentTaskServer',
            function (searchService, $stateParams, $q, $location, $state, currentTaskServer) {
            var defer = $q.defer();
            searchService.searchTabByOrder($stateParams['sID_Order']).then(function (res) {
              if (res && res['oTab'] && res['oTab']['sSubTab']) {
                var type = $stateParams['type'] ? $stateParams['type'] : $location.hash();
                var lowerCaseType = res['oTab']['sSubTab'].toLowerCase();
                if (type !== lowerCaseType) {
                  $state.go('tasks.typeof.view', {
                    'tab': $stateParams.tab,
                    'sID_Order': $stateParams.sID_Order,
                    'type': lowerCaseType,
                    '#': lowerCaseType
                  })
                } else {
                  defer.resolve(null);
                }
              } else {
                defer.resolve(null);
              }
            });

            return defer.promise;
          }],
          taskData: [
            'tasks',
            '$stateParams',
            '$location',
            'tasksStateModel',
            'CurrentServer',
            'currentTaskServer',
            'isSubTabForRedirect',
            function(tasks, $stateParams, $location, tasksStateModel, CurrentServer, currentTaskServer, isSubTabForRedirect) {
              var params = {
                  sID_Order: $stateParams.sID_Order.indexOf('#') > -1 ? $stateParams.sID_Order.split('#')[0] : $stateParams.sID_Order,
                  bIncludeGroups: true,
                  bIncludeStartForm: true,
                  bIncludeAttachments: true,
                  bIncludeProcessVariables: true,
                  bIncludeMessages: true
                },
                type = $stateParams.type ? $stateParams.type : $location.hash();

              tasksStateModel.sID_Order = params.sID_Order;

              if (type == 'finished' || type == 'docHistory'){
                params.isHistory = true;
              }
              return tasks.getTaskData(params, false, currentTaskServer)
            }
          ],
          checkSubFolderStatus: ['iGovNavbarHelper', function (iGovNavbarHelper) {
            return iGovNavbarHelper.getSubFolders();
          }],
          taskForm: [
            'tasks',
            '$q',
            'taskData',
            '$state',
            '$stateParams',
            'Modal',
            '$location',
            'iGovNavbarHelper',
            'Tab',
            'SubFolder',
            'checkSubFolderStatus',
            function (tasks, $q, taskData, $state, $stateParams, Modal, $location, iGovNavbarHelper, Tab, SubFolder, checkSubFolderStatus) {
              var reject = function (err) {
                console.error(angular.toJson(err));
                var type = $stateParams.type ? $stateParams.type : $location.hash();
                $state.go('tasks.typeof', {tab: $stateParams.tab, type: type});
                Modal.inform.error(function (result) {})(angular.toJson(err));
                defer.reject();
              };

              var defer = $q.defer(),
                type = $stateParams.type ? $stateParams.type : $location.hash(),
                oTab = taskData.oTab && taskData.oTab.sDocumentStatus ? taskData.oTab.sDocumentStatus : null,
                hasSubFolder = oTab ? SubFolder.hasSubFolder(iGovNavbarHelper.tabName[oTab]) : false,
                step = taskData.mProcessVariable && taskData.mProcessVariable['sKey_Step_Document'] ? taskData.mProcessVariable['sKey_Step_Document'] : null,
                subFolder = hasSubFolder ? Tab.checkSubFolder(taskData.aDocumentStepLogin, step, iGovNavbarHelper.tabName[oTab]) : null;

              if (subFolder && !subFolder.isVisible) hasSubFolder = false;

              if (oTab && iGovNavbarHelper.tabName[oTab] !== type && !hasSubFolder || hasSubFolder && subFolder && subFolder.name !== type) {
                var currentUrl = $location.url();
                var redirectUrl = '/' + $stateParams.tab + '/sID_Order=' + $stateParams.sID_Order + '#' + (subFolder && subFolder.isVisible ? subFolder.name : iGovNavbarHelper.tabName[oTab]);

                if (currentUrl !== redirectUrl) {
                  $state.go('tasks.typeof.view', {
                    'tab': $stateParams.tab,
                    'sID_Order': $stateParams.sID_Order,
                    'type': subFolder && subFolder.isVisible ? subFolder.name : iGovNavbarHelper.tabName[oTab],
                    '#': subFolder && subFolder.isVisible ? subFolder.name : iGovNavbarHelper.tabName[oTab]
                  })
                } else if (currentUrl === redirectUrl && type === $location.hash()) {
                  $state.reload();
                }
              }

              if (taskData.sDateEnd) {
                defer.resolve(taskData.aField);
              } else if(taskData.nID_Task) {
                defer.resolve(Tab.taskFormStructureConvert(taskData.aField))
              } else {
                if(taskData['code'] === 'SYSTEM_ERR' && taskData['message']){
                  reject(taskData['message']);
                } else {
                  var order = $stateParams.sID_Order.indexOf('#') > -1 ? $stateParams.sID_Order.split('#')[0] : $stateParams.sID_Order;
                  reject('Tasks id is incorrect' + order ? ' (' + order + ').' : '.');
                }
              }
              return defer.promise;
            }
          ],
          documentRights: [
            'taskData',
            function (taskData) {
              return taskData.aDocumentStepRight ? taskData.aDocumentStepRight : null;
            }
          ],
          documentLogins: [
            'taskData',
            function (taskData) {
              return taskData.aDocumentStepLogin ? taskData.aDocumentStepLogin : null;
            }
          ],
          chatMessages: [
            'taskData',
            function (taskData) {
              return taskData.aListOfChat ? taskData.aListOfChat : null;
            }
          ]
        }
      })
      .state('tasks.typeof.history', {
        url: '^/:tab/sID_Order=:sID_Order/history',
        templateUrl: 'app/tasks/taskFormHistoryNew.html',
        controller: 'historyCtrl',
        access: {
          requiresLogin: true
        },
        resolve: {
          taskData: [
            'tasks',
            '$stateParams',
            '$location',
            function(tasks, $stateParams, $location) {
              var params = {
                  sID_Order: $stateParams.sID_Order,
                  bIncludeGroups: true,
                  bIncludeStartForm: true,
                  bIncludeAttachments: true,
                  bIncludeProcessVariables: true,
                  bIncludeMessages: true
                },
                type = $stateParams.type ? $stateParams.type : $location.hash();

              if (type == 'finished' || type == 'docHistory'){
                params.isHistory = true;
              }

              return tasks.getTaskData(params, false)
            }
          ]
        }
      })
      .state('tasks.typeof.fulfillment', {
        url: '/:id/fulfillment',
        templateUrl: 'app/tasks/fulfillment.html',
        controller: 'fulfillmentCtrl',
        access: {
          requiresLogin: true
        },
        resolve: {
          oTask: [
            'tasks',
            '$stateParams',
            'tasksStateModel',
            '$q',
            '$location',
            function (tasks, $stateParams, tasksStateModel, $q, $location) {
              tasksStateModel.taskId = $stateParams.id;
              if ($location.hash() == 'finished'){
                var defer = $q.defer();
                tasks.taskFormFromHistory($stateParams.id).then(function(response){
                  defer.resolve(JSON.parse(response).data[0]);
                }, defer.reject);
                return defer.promise;
              }
              else {
                return tasks.getTask($stateParams.id);
              }
            }
          ],
          taskData: [
            'tasks',
            '$stateParams',
            '$location',
            function(tasks, $stateParams, $location) {
              var params = {
                sID_Order: $stateParams.id,
                bIncludeGroups: true,
                bIncludeStartForm: true,
                bIncludeAttachments: true,
                bIncludeProcessVariables: true,
                bIncludeMessages: true
              };
              return tasks.getTaskData(params, false)
            }
          ]
        }
      })
  }
})();
