angular.module('dashboardJsApp').directive('delegateSelect', ['Auth', '$http', 'ExecAndCtrlService', 'lunaService', 'tasks', 'moment', 'Modal',
  function (Auth, $http, ExecAndCtrlService, lunaService, tasks, moment, Modal) {
  return {
    restrict: 'EA',
    templateUrl: 'app/tasks/form-buttons/delegateSelect.template.html',
    link: function (scope) {
      var today = moment().format('DD/MM/YYYY'),
          selectExe = {},
          sID_BP,
          issueArr = [];

      scope.executors = null;
      scope.data = {executors: []};
      scope.delegate = {exec: null, date: null, soExec: 'no'};
      scope.hasDelegates = false;

      angular.element(document.getElementById('execDate')).data("DateTimePicker").minDate(new Date()); //huck for setting date to datepicker
      
      var subjectUserFilter = function (arr) {
        var allUsers = [], filteredUsers = [], logins = [];

        (function loop(arr) {
          angular.forEach(arr, function(item) {
            if(item.aUser) {
              angular.forEach(item.aUser, function(user) {
                allUsers.push(user);
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
            if(item.aSubjectGroupChilds && item.aSubjectGroupChilds.length > 0){
              loop(item.aSubjectGroupChilds)
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

      scope.refreshUserData = function (sFind) {
        for ( var i=0; i<scope.taskForm.length; i++ ) {
          if ( scope.taskForm[i].type === 'table' && scope.taskForm[i].id.indexOf('oProcessSubject_Executor') === 0 ) {
            selectExe.params = ExecAndCtrlService.getDelegateSelectProperties(scope.taskForm, scope.taskForm[i].value);
            sID_BP = selectExe.params.sID_BP;
            delete selectExe.params.sID_BP;
            selectExe.params.sSubjectType = 'Human';
            selectExe.params.sFind = sFind;
            console.log(selectExe);

            $http.get('./api/subject-role', selectExe).then(function (res) {
              if ( typeof res.data === 'object' ) {
                var response = subjectUserFilter(res.data.aSubjectGroupTree);
                angular.forEach(response, function (user) {
                  user.sName = user.sFirstName + " " + user.sLastName;
                })
              }
              scope.executors = response;
            });
          }
        }
      };

      function fillDateToExecutors() {
        angular.forEach(scope.data.executors, function (executor) {
          executor.date = scope.delegate.date;
        });

        scope.taskData.aProcessSubjectTask[0].aProcessSubject[0].sDatePlan = ExecAndCtrlService.prepareDateFormat(scope.delegate.date);
      }

      scope.delegateExec = function () {
        fillDateToExecutors();
        var executors = ExecAndCtrlService.rightExecStructure(
          scope.data.executors,
          scope.taskData.aProcessSubjectTask[0].aProcessSubject,
          'Executor',
          scope.delegate.date,
          scope.delegate.soExec
        );

        if (scope.delegate.soExec && scope.delegate.soExec === 'yes') {
          issueArr.push(
            ExecAndCtrlService.createIssueObject(
              scope.taskData.aProcessSubjectTask,
              scope.taskData,
              sID_BP,
              executors,
              'delegate',
              scope.delegate.soExec
            )
          );
        } else {
          issueArr = ExecAndCtrlService.createDelegateIssuesWithoutSoExec(
            scope.taskData.aProcessSubjectTask,
            scope.taskData,
            sID_BP,
            executors,
            'delegate'
          )
        }

        scope.submitTask(scope.form, false, false, 'stay', issueArr);
        issueArr = [];
        scope.execCtrlModals.bDelegate = false;
        scope.closeModalByButton();
      };

      scope.onSelectDelegate = function () {
        scope.addFlag = true;
        for (var i in scope.data.executors){
          if (scope.data.executors[i].exec.sName === scope.delegate.exec.sName){
            scope.addFlag = false;
          }
        }
        if (scope.delegate.date && scope.delegate.exec) {
          if (scope.addFlag){
            scope.data.executors.push({exec: scope.delegate.exec, date: scope.delegate.date});
            scope.delegate.exec = null;
          }
        }
      };

      scope.getInitials = function (name) {
        if (name) {
          var nameAndMiddleName = name.split(' ');
          if (nameAndMiddleName.length === 2) {
            return nameAndMiddleName[0][0] + '.' + nameAndMiddleName[1][0] + '.';
          }
        } else {
          return name;
        }
      };

      scope.removeDelegator = function (position) {
        scope.data.executors.splice(position, 1);
      };

      scope.isFormInvalid = function () {
        for ( var i=0; i<scope.data.executors.length; i++ ) {
          if ( !scope.data.executors[i].exec || !scope.data.executors[i].date ) {
            return true;
          }
        }
      };
    }
  }
}]);
