angular.module('dashboardJsApp').directive('transferDateByCtrl', ['DocumentsService', 'ExecAndCtrlService', '$state', '$templateCache', '$rootScope',
  function (DocumentsService, ExecAndCtrlService, $state, $templateCache, $rootScope) {
  return {
    restrict: 'EA',
    templateUrl: 'app/tasks/form-buttons/transferDateByCtrl.template.html',
    link: function (scope) {
      $rootScope.spinner = false;
      scope.data = { date: null };

      scope.transferDate = function () {
        $rootScope.spinner = true;
        DocumentsService.getLoginExecutorOrController(scope.taskData.aProcessSubjectTask[0].aProcessSubject, scope.taskId, 'controller')
          .then(function (res) {
            var params = {
              sID_ProcessSubjectStatus: 'transfered',
              snID_Task_Activiti: res.snID_Task_Activiti,
              sLoginController: res.sLoginController,
              snID_ProcessSubjectTask: scope.taskData.aProcessSubjectTask[0].nID,
              sDatePlaneNew: ExecAndCtrlService.prepareDateFormat(scope.data.date)
            };

            DocumentsService.setProcessStatus(params).then(function () {
              scope.execCtrlModals.bCtrlTransferDate = false;
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
