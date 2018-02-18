(function () {
  'use strict';
  angular
    .module('dashboardJsApp')
    .config([
      '$stateProvider',
      function ($stateProvider) {
        $stateProvider
          .state('baTask', {
            url: '/task',
            access: {
              requiresLogin: true
            },
            resolve: {
              taskData: [
                'tasks',
                '$location',
                function (tasks, $location) {
                  var params = angular.copy($location.search());
                  if (params.nID_Order) {
                    params.nID_Process = params.nID_Order.substring(params.nID_Order.length - 1);
                  }
                  return tasks.getTaskData(params, true);
                }
              ],
              stateModel: [
                function () {
                  return {
                    printTemplate: null,
                    taskDefinition: null,
                    strictTaskDefinition: null,
                    userProcess: null
                  }
                }
              ]
            },
            views: {
              '@': {
                controller: 'baTask',
                templateUrl: 'app/task/baTask.html'
              },
              'task-view@baTask': {
                templateUrl: 'app/tasks/taskForm.html',
                controller: 'TaskViewCtrl',
                resolve: {
                  taskData: [
                    'tasks',
                    '$stateParams',
                    function(tasks, $stateParams) {
                      var params = {
                        nID_Task: $stateParams.id,
                        bIncludeGroups: true,
                        bIncludeStartForm: true,
                        bIncludeAttachments: true,
                        bIncludeProcessVariables: true,
                        bIncludeMessages: true
                      };
                      if ($stateParams.type == 'finished' || $stateParams.type == 'docHistory'){
                        params.isHistory = true;
                      }
                      return tasks.getTaskData(params, false)
                    }
                  ],
                  taskForm: [
                    'taskData',
                    'tasks',
                    '$q',
                    function (taskData, tasks, $q) {
                      var defer = $q.defer();
                      if (taskData.sDateEnd) {
                        tasks.taskFormFromHistory(taskData.nID_Task).then(function (result) {
                          defer.resolve(JSON.parse(result).data[0].variables)
                        }, defer.reject)
                      } else {
                        tasks.taskForm(taskData.nID_Task).then(function (result) {
                          defer.resolve(result.formProperties);
                        }, defer.reject);
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
                  ]
                }
              },
              'task-view-history@baTask': {
                templateUrl: 'app/tasks/taskFormHistory.html'
              }
            }
          })
      }
    ]);
})();
