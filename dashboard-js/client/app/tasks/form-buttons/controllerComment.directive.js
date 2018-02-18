angular.module('dashboardJsApp').directive('controllerComment', ['DocumentsService', 'ExecAndCtrlService', '$state', '$templateCache', '$rootScope',
  function (DocumentsService, ExecAndCtrlService, $state, $templateCache, $rootScope) {
  return {
    restrict: 'EA',
    templateUrl: 'app/tasks/form-buttons/controllerComment.template.html',
    link: function (scope) {
      var currentAction = ExecAndCtrlService.getCurrentAction(scope.execCtrlModals);
      scope.data = { text: "" };
      $rootScope.spinner = false;

      scope.leaveComment = function () {
        $rootScope.spinner = true;
        DocumentsService.getLoginExecutorOrController(scope.taskData.aProcessSubjectTask[0].aProcessSubject, scope.taskId, 'controller')
          .then(function (res) {
            var params = {
              sID_ProcessSubjectStatus: currentAction.action,
              snID_Task_Activiti: res.snID_Task_Activiti,
              snID_ProcessSubjectTask: scope.taskData.aProcessSubjectTask[0].nID,
              sLoginController: res.sLoginController,
              sText: scope.data.text
            };

            DocumentsService.setProcessStatus(params).then(function () {
              scope.execCtrlModals[currentAction.name] = false;
              scope.closeModalByButton();

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
