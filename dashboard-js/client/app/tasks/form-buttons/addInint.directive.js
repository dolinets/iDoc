'use strict';

angular.module('dashboardJsApp').directive('visorSelect', ['$http', 'tasks', 'Auth', '$rootScope',
  '$timeout', 'DocumentsService', 'Modal', 'CurrentServer', '$cookies',
  function($http, tasks, Auth, $rootScope, $timeout, DocumentsService, Modal, CurrentServer, $cookies) {
    return {
      restrict: 'EA',
      templateUrl: 'app/tasks/form-buttons/addInint.html',
      link: function (scope, element, attrs, ngModel) {
        scope.visors = {
          selected: null
        };
        scope.sKey_Group = null;

        var queryParams = {params: {}};
        var login = $cookies.getObject('user');
        var taskServer = CurrentServer.getServer();
        var sCurrReferent = $cookies.getObject('referent');
        sCurrReferent = sCurrReferent ? sCurrReferent.id : login.id;

        var subjectUserFilter = function (arr) {
          var allUsers = [], filteredUsers = [], logins = [];

          (function loop(arr) {
            angular.forEach(arr, function (item) {
              if (item.aUser) {
                angular.forEach(item.aUser, function (user) {
                  if (user.sLogin === item.sID_Group_Activiti){
                    allUsers.push(user);
                  }
                })
              }
              if(allUsers[allUsers.length-1] !== undefined){
                if(item.oSubjectHumanPositionCustom && item.oSubjectHumanPositionCustom.sNote && item.oSubjectHumanPositionCustom.sNote.length > 0){
                    allUsers[allUsers.length-1]['sPosition'] = item.oSubjectHumanPositionCustom.sNote;
                }
                if((typeof item.sName_SubjectGroupCompany === 'string') && item.sName_SubjectGroupCompany.length > 0){
                    allUsers[allUsers.length-1]['sCompany'] = item.sName_SubjectGroupCompany;
                }
              }
              if (item.aSubjectGroupChilds && item.aSubjectGroupChilds.length > 0) {
                loop(item.aSubjectGroupChilds)
              }
            })
          })(arr);

          for (var i = 0; i < allUsers.length; i++) {
            if (logins.indexOf(allUsers[i].sLogin) === -1) {
              filteredUsers.push(allUsers[i]);
              logins.push(allUsers[i].sLogin);
            }
          }
          return filteredUsers;
        };

        scope.refreshUserData = function (sFind) {
          for (var j = 0; j < scope.documentLogins.length; j++) {
            var users = scope.documentLogins[j].aUser;
            for (var l = 0; l < users.length; l++) {
              if (users[l].sLogin === login.id) {
                queryParams.params.sID_Group_Activiti = $rootScope.docRights.AddVisor_Group_Activiti;
                queryParams.params.sSubjectType = 'Human';
                queryParams.params.nDeepLevel = 0;
                scope.sKey_Group = users[l].sID_Group;
                break;
              }
            }
          }

          queryParams.params.sFind = sFind;

          $http.get('./api/subject-role', queryParams).then(function (res) {
            if (typeof res.data === 'object') {
              var response = subjectUserFilter(res.data.aSubjectGroupTree);
              angular.forEach(response, function (user) {
                user.sName = user.sFirstName + " " + user.sLastName;
                user.sCompany = user.sCompany;
                user.sPosition = user.sPosition;
              });
            }
            scope.visorUsersList = response;
          });
        }

        scope.onSelectUser = function (user) {
          $rootScope.visorSelectMenu = false;

          var params = {
            snID_Process_Activiti: scope.taskData.oProcess.nID,
            sKey_Group: scope.sKey_Group,
            sKey_Group_Delegate: user.sLogin,
            sKey_Step: scope.sKey_Step,
            nID_Task: scope.taskForm.taskData.nID_Task
          };

          if (taskServer.another)
            params.taskServer = taskServer.name;

          for (var i = 0; i < scope.taskForm.length; i++) {
            if (scope.taskForm[i].id.indexOf('sKey_Step') === 0) {
              params.sKey_Step = scope.taskForm[i].value;
              break;
            }
          }
          scope.params = params;

        };

        scope.safeVisor = function () {
          scope.modalSpinner = true;
          if (scope.visors.selected) {
            scope.params.sLogin = login.id;
            scope.params.sLoginReferent = sCurrReferent;
            scope.params.bHistory = scope.sSelectedTask === 'docHistory';

            DocumentsService.addVisorToDoc(scope.params).then(function (res) {
              $timeout(function () {
                scope.$apply();
                Modal.inform.success()('Успішно додано для ознайомлення');
                scope.updateListOfAcceptors(res);
              });
              scope.visors.selected = null;
            }).finally(function () {
              scope.modalSpinner = false;
            });
          }
        };
      }
    };
  }]);
