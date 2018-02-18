angular.module('dashboardJsApp').directive('acceptIssue', ['DocumentsService', '$state', '$templateCache', '$rootScope',
  function (DocumentsService, $state, $templateCache, $rootScope) {
  return {
    restrict: 'EA',
    template: '<button type="button" ' +
              'ng-if="currentUserRole.isController" ' +
              'class="btn btn-link" ' +
              'ng-click="accept()">Прийняти завдання</button>',
    link: function (scope) {
      $rootScope.spinner = false;

      scope.accept = function () {
        $rootScope.spinner = true;
        DocumentsService.getLoginExecutorOrController(scope.taskData.aProcessSubjectTask[0].aProcessSubject, scope.taskId, 'controller')
          .then(function (res) {
            var params = {
              sID_ProcessSubjectStatus: 'executed',
              snID_Task_Activiti: res.snID_Task_Activiti,
              sLoginController: res.sLoginController
            };

            DocumentsService.setProcessStatus(params).then(function () {
              $state.reload();
            });
        });
      };
    }
  }
}]);
