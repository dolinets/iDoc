angular.module('dashboardJsApp').directive('issueBlock', ['Issue', '$http', 'Auth', '$sce', '$cookies','CurrentServer', function (Issue, $http, Auth, $sce, $cookies, CurrentServer) {
  return {
    restrict: 'EA',
    templateUrl: 'components/issueBlock/issue.template.html',
    link: function (scope) {
      scope.html = function (text) {
        return $sce.trustAsHtml(text);
      };
      scope.isDisabled = true;

      scope.taskServer = CurrentServer.getServer();

      if (scope.taskData && (scope.taskData.mProcessVariable && scope.taskData.mProcessVariable.sID_Order_Document || scope.taskData.oProcess.sBP && scope.taskData.oProcess.sBP.indexOf('_doc_') === 0)){
        scope.createdByDocumentEdit = true;
      } else {
        scope.createdByDocumentEdit = false;
      }

      scope.issues = Issue.getIssues();

      scope.selectExe = {params: {sSubjectType: 'Human', sID_Group_Activiti: scope.issue.selectExecutors.activiti, nDeepLevel: scope.issue.selectExecutors.deep}};
      scope.selectCtrl = {params: {sSubjectType: 'Human', sID_Group_Activiti: scope.issue.controllerSelect.activiti, nDeepLevel: scope.issue.controllerSelect.deep}};
      scope.valid = true;
      scope.isCreatedFromDoc = !(scope.creatingTask && scope.creatingTask.aFormProperty);

      scope.remove = function (i) {
        Issue.removeIssue(i);
        scope.taskData.aProcessSubjectTask.splice(i, 1);
      };

      scope.onTaskTermChange = function(index) {
        if (scope.issues.length > 0) {
          var issue = scope.issues[index];
          if (issue.taskTerm.property === 'calendar')
            scope.disablePastDays = {minDate: new Date(issue.taskTerm.value)};
        }
        issue.taskTerm.value = '';
      };

      if (!scope.isCreatedFromDoc) {
        var user = JSON.parse($cookies.get('user'));
          Issue.addIssue({
            sEmail: user.email,
            sFirstName: user.firstName,
            sLastName: user.lastName,
            sLogin: user.id,
            sName: user.firstName + ' ' + user.lastName,
            sPicture: user.pictureUrl
          });

        scope.issues = Issue.getIssues();
      }
      var subjectUserFilter = function (arr) {
        var allUsers = [], filteredUsers = [], logins = [];

        (function loop(arr) {
          angular.forEach(arr, function(item) {
            if (!item.oSubject.oSubjectStatus || (item.oSubject.oSubjectStatus && item.oSubject.oSubjectStatus.sName !== 'Dismissed') ) {
              if(item.aUser) {
                angular.forEach(item.aUser, function(user) {
                  if (user.sLogin === item.sID_Group_Activiti){
                    allUsers.push(user);
                  }
                });
              }
              if(allUsers[allUsers.length-1] !== undefined){
                if(item.oSubjectHumanPositionCustom && item.oSubjectHumanPositionCustom.sNote && item.oSubjectHumanPositionCustom.sNote.length > 0){
                    allUsers[allUsers.length-1]['sPosition'] = item.oSubjectHumanPositionCustom.sNote;
                }
                if((typeof item.sName_SubjectGroupCompany === 'string') && item.sName_SubjectGroupCompany.length > 0){
                    allUsers[allUsers.length-1]['sCompany'] = item.sName_SubjectGroupCompany;
                }
              }
              if(item.aSubjectGroupChilds && item.aSubjectGroupChilds.length > 0){
                loop(item.aSubjectGroupChilds)
              }
            }
          })
        })(arr);

        for(var i=0; i<allUsers.length; i++) {
          if(logins.indexOf(allUsers[i].sLogin) === -1) {
            filteredUsers.push(allUsers[i]);
            logins.push(allUsers[i].sLogin);
          }
        }
        return filteredUsers;
      };

      scope.loadExecutors = function (sFind) {
        scope.selectExe.params.sFind = sFind;
        if (scope.selectExe.params.sID_Group_Activiti.indexOf('$') > -1) {
          var user = $cookies.getObject('user');
          scope.selectExe.params.sID_Group_Activiti = user.id;
        }
        
        $http.get('./api/subject-role', scope.selectExe).then(function (res) {
          if(typeof res.data === 'object') {
            var response = subjectUserFilter(res.data.aSubjectGroupTree);
            angular.forEach(response, function (user) {
              user.sName = user.sFirstName + " " + user.sLastName;
              user.sCompany = user.sCompany;
              user.sPosition = user.sPosition;
            });
          }
          scope.executors = response;
        });
      }

      scope.loadCtrls = function (sFind) {
        scope.selectCtrl.params.sFind = sFind;
        if (scope.selectCtrl.params.sID_Group_Activiti.indexOf('$') > -1) {
          var user = $cookies.getObject('user');
          scope.selectCtrl.params.sID_Group_Activiti = user.id;
        }

        $http.get('./api/subject-role', scope.selectCtrl).then(function (res) {
          if(typeof res.data === 'object') {
            var response = subjectUserFilter(res.data.aSubjectGroupTree);
            angular.forEach(response, function (user) {
              user.sName = user.sFirstName + " " + user.sLastName;
              user.sCompany = user.sCompany;
              user.sPosition = user.sPosition;
            })
          }
          scope.ctrl = response;
        });
      }

      function loadSelects() {

      }loadSelects();

      scope.addNewExecutor = function (issue) {
        scope.valid = Issue.addExecutor(issue);
      };

      scope.removeExecutor = function (issue, index) {
        Issue.removeExecutor(issue, index);
      };

      scope.onSelectUser = function () {
        Issue.validate();
      };

      scope.updateExecutorsSelect = function(contact, index) {
        _.each(scope.issues[index].taskExecutor, function (x) {
          x.isMain = (x.value.sLogin === contact.value.sLogin);
        });
      };
    }
  }
}]);
