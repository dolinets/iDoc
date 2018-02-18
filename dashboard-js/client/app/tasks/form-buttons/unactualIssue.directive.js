angular.module('dashboardJsApp').directive('unactualIssue', ['DocumentsService', '$state', '$templateCache', '$rootScope',
  function (DocumentsService, $state, $templateCache, $rootScope) {
  return {
    restrict: 'EA',
    template: '<button type="button" ' +
              'ng-if="currentUserRole.isController" ' +
              'class="btn btn-link" ' +
              'ng-click="unactual()">Неактуально</button>',
    link: function (scope) {
      $rootScope.spinner = false;

      scope.unactual = function () {
        $rootScope.spinner = true;
        DocumentsService.getLoginExecutorOrController(scope.taskData.aProcessSubjectTask[0].aProcessSubject, scope.taskId, 'controller')
          .then(function (res) {
            var params = {
              sID_ProcessSubjectStatus: 'unactual',
              snID_Task_Activiti: res.snID_Task_Activiti,
              sLoginController: res.sLoginController
            };

            DocumentsService.setProcessStatus(params).then(function () {
              //todo когда будет изменен роутинг - нужно будет подправить путь на сервере к темплейту (путь будет tasks, а в роутинге tasks тоже используем)
              // var currentPageTemplate = $state.current.templateUrl;
              // $templateCache.remove(currentPageTemplate);
              $state.reload();
            });
        });
      };
    }
  }
}]);
