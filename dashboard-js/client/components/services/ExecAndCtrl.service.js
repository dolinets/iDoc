angular.module('dashboardJsApp').service('ExecAndCtrlService', ['Auth', 'tasks', '$q', '$cookies', '$rootScope', function (Auth, tasks, $q, $cookies, $rootScope) {
  var actionList = {
    bExecTransferDate: 'requestTransfered',
    bNotExecuted: 'notExecuted',
    bCtrlTransferDate: 'transfered',
    bRejected: 'rejected'
  };

  var actionNames = {
    bExecTransferDate: 'Перенести завдання',
    bReport: 'Звiт',
    bDelegate: 'Делегувати завдання',
    bCtrlTransferDate: 'Перенести завдання',
    bNotExecuted: 'Не прийняти завдання',
    bRejected: 'Вiдхилити завдання',
    bAskMessage: 'Додати зауваження',
    bAskMessageAnswer: 'Відповісти на зауваження',
    bEditMessage: 'Редагувати зауваження',
    bSignInfo: 'Коментар до підпису',
    bDelegates: 'Делегувати',
    bAddAcceptor: 'Додати підписанта',
    bAddViewer: 'Додати на перегляд',
    bAddVisor: 'Додати на ознайомлення'
  };

  function isActiveIssue(person) {
    return (['executed', 'notExecuted', 'unactual']).indexOf(person.oProcessSubjectStatus.sID) > -1;
  }

  return {
    getCurrentRole: function (obj, current) {

      var user;
      if (JSON.parse($cookies.get('user'))){
        user = JSON.parse($cookies.get('user'));
      } else {
        user = Auth.getCurrentUser();
      }

      var result = {isController: false, isExecutor: false, isComplete: false};

      for ( var i=0; i<obj.length; i++ ) {
        if ( obj[i].sLoginRole === 'Controller' ) {
          result.isComplete = isActiveIssue(obj[i]);
        }

        if ( obj[i].sLogin === user.id) {
          if ( obj[i].sLoginRole === 'Controller' ) {
            result.isController = true;
          } else {
            result.nDeep = obj[i].nDeep ? obj[i].nDeep : 0;
            result.isExecutor = true;
          }
        }
      }

/*      if (result.isController && result.isExecutor) {
        if (current === 'Контроль') {
          result.isExecutor = false;
          result.isController = true;
        } else if (current === 'Исполнитель') {
          result.isExecutor = true;
          result.isController = false;
        }
      }*/
      if (result.isController && result.isExecutor) {
        if (current === 'control') {
          result.isExecutor = false;
          result.isController = true;
        } else if (current === 'unassigned') {
          result.isExecutor = true;
          result.isController = false;
        }
      }

      return result;
    },

    getIssueName: function (n) {
      for (var name in actionNames) {
        if (actionNames.hasOwnProperty(name) && name === n) {
          return actionNames[name];
        }
      }
    },

    prepareDateFormat: function (date, back) {
      var splitDate;

      if (!back) {
        splitDate = date.split('/');
        return splitDate[2] + '-' + splitDate[1] + '-' + splitDate[0];
      } else {
        splitDate = date.split('-');
        return splitDate[2] + '/' + splitDate[1] + '/' + splitDate[0];
      }
    },

    convertDay: function(day) {
      var splitDay = day.split(/[A-zА-я]/g);
      var result = Math.floor(splitDay);
      return result;
    },

    getCurrentAction: function (actions) {
      for ( var action in actions ) {
        if ( actions.hasOwnProperty(action) && actions[action] ) {
          return {name: action, action: actionList[action]};
        }
      }
    },

    reportType: function (item) {
      var user;
      if (JSON.parse($cookies.get('user'))){
        user = JSON.parse($cookies.get('user'));
      } else {
        user = Auth.getCurrentUser();
      }
      for ( var i=0; i<item[0].aProcessSubject.length; i++ ) {
        if ( user.id === item[0].aProcessSubject[i].sLogin ) {
          return item[0].aProcessSubject[i].sTextType;
        }
      }
    },

    uploadFileReport: function (files, taskID, fieldID) {
      var deferred = $q.defer();
      $rootScope.spinner = true;
      tasks.upload(files, taskID, fieldID, true).then(function (result) {
        deferred.resolve(result);
      }).finally(function () {
          $rootScope.spinner = false;
      });

      return deferred.promise;
    },

    getDelegateSelectProperties: function (form, table) {
      var arr = JSON.parse(table), select, result = { sID_Group_Activiti: null, nDeepLevel: null };

      for ( var o=0; o<arr.aField.length; o++ ) {
        if ( arr.aField[o].type === 'select' && arr.aField[o].name.indexOf('sID_BP') > -1 ) {
          select = arr.aField[o];
          break;
        }
      }

      var objName = select.name.split(';')[2],
          options = objName.split(','),
          params = { sID_Group_Activiti: null, nDeepLevel: null };

      angular.forEach(options, function (opt) {
        if ( opt.indexOf('sID_Group_Activiti') > -1 ) {
          params.sID_Group_Activiti = opt.split('=')[1];
        } else if ( opt.indexOf('nDeepLevel') > -1 ) {
          params.nDeepLevel = opt.split('=')[1];
        } else if ( opt.indexOf('sID_BP') > -1) {
          result.sID_BP = opt.split('=')[1];
        }
      });

      for ( var i=0; i<form.length; i++ ) {
        if ( form[i].id === params.sID_Group_Activiti )
          result.sID_Group_Activiti = form[i].value;
        else if ( form[i].id === params.nDeepLevel )
          result.nDeepLevel = form[i].value;

        if ( result.sID_Group_Activiti && result.nDeepLevel )
          break;
      }

      return result;
    },

    rightExecStructure: function (arr, issueSub, role, newDate, soExec) {
      var result = [], self = this, user = Auth.getCurrentUser();

      // if (soExec && soExec === 'yes') {
      //   for (var i=0; i<issueSub.length; i++) {
      //     if (issueSub[i].sLogin === user.id && issueSub[i].aProcessSubjectChild.length > 0) {
      //       angular.forEach(issueSub[i].aProcessSubjectChild, function (child) {
      //         if (child.sLoginRole !== 'Controller') {
      //           result.push({
      //             sLogin: child.sLogin,
      //             sLoginRole: child.sLoginRole,
      //             sDatePlan: self.prepareDateFormat(newDate)
      //           });
      //         }
      //       });
      //       break;
      //     }
      //   }
      // }

      angular.forEach(arr, function (user) {
        var day = $("input[name='taskDay0']").val(), dayTask = $("input[name='taskDay1']").val();
        if (day != undefined || (day != undefined && dayTask != undefined) || dayTask != undefined) {
            result.push({
                sLogin: user.exec.sLogin,
                sLoginRole: role,
                nDayPlan: self.convertDay(user.day)
            });
        } else {
            result.push({
                sLogin: user.exec.sLogin,
                sLoginRole: role,
                sDatePlan: self.prepareDateFormat(user.date)
            });
        }
      });

      return result;
    },

    createIssueObject: function (issue, taskData, idBP, execArr, action, soExec) {
      var day = $("input[name='taskDay0']").val(), dayTask = $("input[name='taskDay1']").val();
      var user = Auth.getCurrentUser(),
          snID_Process_Activiti_Root;

      if (taskData.mProcessVariable && taskData.mProcessVariable.sID_Order_Document)
        snID_Process_Activiti_Root = issue[0].snID_Process_Activiti_Root;
      else if (taskData.oProcess.sBP.indexOf('_doc_') === 0)
        snID_Process_Activiti_Root = issue[0].snID_Process_Activiti_Root;
      else
        snID_Process_Activiti_Root = null;

      if (day != undefined || (day != undefined && dayTask != undefined) || dayTask != undefined) {
          var obj = {
              sID_BP: idBP,
              snID_ProcessSubjectTask: issue[0].nID.toString(),
              snID_Process_Activiti_Root: snID_Process_Activiti_Root,
              sHead: issue[0].sHead,
              sActionType: action,
              sBody: issue[0].sBody,
              sReportType: issue[0].aProcessSubject[0].sTextType,
              aProcessSubject: [{
                  sLogin: user.id,
                  sLoginRole: 'Controller',
                  nDayPlan: issue[0].aProcessSubject[0].nDayPlan.split(' ')[0]
              }]
          };
      } else {
          var obj = {
              sID_BP: idBP,
              snID_ProcessSubjectTask: issue[0].nID.toString(),
              snID_Process_Activiti_Root: snID_Process_Activiti_Root,
              sHead: issue[0].sHead,
              sActionType: action,
              sBody: issue[0].sBody,
              sReportType: issue[0].aProcessSubject[0].sTextType,
              aProcessSubject: [{
                  sLogin: user.id,
                  sLoginRole: 'Controller',
                  sDatePlan: issue[0].aProcessSubject[0].sDatePlan.split(' ')[0]
              }]
          };
      }
      obj.aProcessSubject = obj.aProcessSubject.concat(execArr);

      if (soExec && soExec === 'yes')
        obj.soExecutor = true;

      return obj;
    },

    createDelegateIssuesWithoutSoExec: function (issue, taskData, idBP, execArr, action) {
      var user = Auth.getCurrentUser(), issues = [],
          snID_Process_Activiti_Root;

      if (taskData.mProcessVariable && taskData.mProcessVariable.sID_Order_Document)
        snID_Process_Activiti_Root = issue[0].snID_Process_Activiti_Root;
      else if (taskData.oProcess.sBP.indexOf('_doc_') === 0)
        snID_Process_Activiti_Root = issue[0].snID_Process_Activiti_Root;
      else
        snID_Process_Activiti_Root = null;

      angular.forEach(execArr, function (executor) {
        var day = $("input[name='taskDay0']").val(), dayTask = $("input[name='taskDay1']").val();
        if (day != undefined || (day != undefined && dayTask != undefined) || dayTask != undefined) {
              var obj = {
                  sID_BP: idBP,
                  snID_ProcessSubjectTask: issue[0].nID.toString(),
                  snID_Process_Activiti_Root: snID_Process_Activiti_Root,
                  sHead: issue[0].sHead,
                  sActionType: action,
                  sBody: issue[0].sBody,
                  sReportType: issue[0].aProcessSubject[0].sTextType,
                  aProcessSubject: [{
                      sLogin: user.id,
                      sLoginRole: 'Controller',
                      nDayPlan: issue[0].aProcessSubject[0].nDayPlan.split(' ')[0]
                  }, {
                      sLogin: executor.sLogin,
                      sLoginRole: executor.sLoginRole,
                      nDayPlan: executor.nDayPlan
                  }]
              };
          } else {
              var obj = {
                  sID_BP: idBP,
                  snID_ProcessSubjectTask: issue[0].nID.toString(),
                  snID_Process_Activiti_Root: snID_Process_Activiti_Root,
                  sHead: issue[0].sHead,
                  sActionType: action,
                  sBody: issue[0].sBody,
                  sReportType: issue[0].aProcessSubject[0].sTextType,
                  aProcessSubject: [{
                      sLogin: user.id,
                      sLoginRole: 'Controller',
                      sDatePlan: issue[0].aProcessSubject[0].sDatePlan.split(' ')[0]
                  }, {
                      sLogin: executor.sLogin,
                      sLoginRole: executor.sLoginRole,
                      sDatePlan: executor.sDatePlan
                  }]
              };
          }

        issues.push(obj);
      });

      return issues;
    },

    isIssueIsCompleted: function (arr) {
      for (var i=0; i<arr.length; i++) {
        if (arr[i].sLoginRole === 'Controller') {

        } else if (arr[i].sLoginRole === 'Controller') {

        }
      }
    }
  }
}]);
