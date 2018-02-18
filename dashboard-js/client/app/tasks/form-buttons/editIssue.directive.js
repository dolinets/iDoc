angular.module('dashboardJsApp').directive('editIssue', ['DocumentsService', 'ExecAndCtrlService', 'Issue',
  function (DocumentsService, ExecAndCtrlService, Issue) {
    return {
      restrict: 'EA',
      template: '<button type="button" ' +
      'ng-if="currentUserRole.isController || (docRights.EditTask && taskData.aProcessSubjectTask.length > 0)" ' +
      'ng-disabled="isIssueEdit"' +
      'class="btn btn-link" ' +
      'ng-click="edit()">Редагувати завдання</button>',
      link: function (scope) {
        scope.edit = function () {
          scope.toggleIssueEdit();
          var createdByDocument;
          if (scope.taskData) {
            if (scope.taskData.mProcessVariable && scope.taskData.mProcessVariable.sID_Order_Document)
              createdByDocument = true;
            if (scope.taskData.oProcess && scope.taskData.oProcess.sBP && scope.taskData.oProcess.sBP.indexOf('_doc_') === 0)
              createdByDocument = true;
            else
              createdByDocument = false;
          }
          Issue.fillIssueForEdit(scope.taskData.aProcessSubjectTask, createdByDocument);
        };
      }
    };
}]);
