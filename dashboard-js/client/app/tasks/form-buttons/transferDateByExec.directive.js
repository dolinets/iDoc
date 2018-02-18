angular.module('dashboardJsApp').directive('transferDateByExec', ['DocumentsService', 'ExecAndCtrlService', '$state', '$templateCache', '$rootScope',
  function (DocumentsService, ExecAndCtrlService, $state, $templateCache, $rootScope) {
  return {
    restrict: 'EA',
    templateUrl: 'app/tasks/form-buttons/transferDateByExec.template.html',
    link: function (scope) {
      scope.data = { date: null, text: "" };
      $rootScope.spinner = false;

      scope.execTransferDate = function () {
        $rootScope.spinner = true;
        DocumentsService.getLoginExecutorOrController(scope.taskData.aProcessSubjectTask[0].aProcessSubject, scope.taskId, 'executor')
          .then(function (res) {
            var params = {
              sID_ProcessSubjectStatus: 'requestTransfered',
              snID_Task_Activiti: res.snID_Task_Activiti,
              sLoginExecutor: res.sLoginExecutor,
              sText: scope.data.text,
              sDatePlaneNew: ExecAndCtrlService.prepareDateFormat(scope.data.date)
            };

            DocumentsService.setProcessStatus(params).then(function () {
              scope.execCtrlModals.bExecTransferDate = false;
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
