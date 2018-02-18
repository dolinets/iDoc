angular.module('dashboardJsApp').directive('reportExecutor', ['DocumentsService', 'ExecAndCtrlService', 
  '$rootScope', '$state', '$templateCache', 'Auth', '$cookies',
  function (DocumentsService, ExecAndCtrlService, $rootScope, $state, $templateCache, Auth, $cookies) {
  return {
    restrict: 'EA',
    templateUrl: 'app/tasks/form-buttons/reportExecutor.template.html',
    link: function (scope) {

      scope.data = { content: null, status: '' };
      scope.item = { value: null, writable: true, id: 'reportFile', name: 'reportFile; ;bNew=true' };
      scope.reportType = ExecAndCtrlService.reportType(scope.taskData.aProcessSubjectTask);
      $rootScope.spinner = false;

      scope.upload = function (files, propertyID) {
        var content = {
          fieldId: propertyID,
          files: files
        };

        $rootScope.switchProcessUploadingState();
        ExecAndCtrlService.uploadFileReport(content.files, scope.taskData.oProcess.nID, content.fieldId )
          .then(function (result) {
            $rootScope.switchProcessUploadingState();
            scope.data.content = JSON.stringify(result.response);
          });
      };

      scope.clearModel = function () {
        scope.data.content = "";
      };

      scope.getFileName = function (content) {
        try {
          content = JSON.parse(content);

          return content.sFileNameAndExt;
        } catch (e) {
         console.error(e);
        }
      };

      scope.sendReport = function (type, form) {
        if (form) {
          scope.validateForm(form);

          var interval = setInterval(waitForValidity, 1); // huck for async validator
          function waitForValidity() {
            if (form.$invalid !== undefined) {
              clearInterval(interval);
              if (form.$valid)
                completeSubmition();
              else
                return;
            }
          }

          if (form.$invalid) return;
        } else 
          completeSubmition();
          
        function completeSubmition() {
          $rootScope.spinner = true;
          DocumentsService.getLoginExecutorOrController(scope.taskData.aProcessSubjectTask[0].aProcessSubject, scope.taskId, 'executor')
            .then(function (res) {
              var currentUser = Auth.getCurrentUser().id,
                  cookieUser = $cookies.getObject('user') ? $cookies.getObject('user').id : '';

              var params = {
                sID_ProcessSubjectStatus: type,
                snID_Task_Activiti: res.snID_Task_Activiti,
                sLoginExecutor: currentUser !== cookieUser ? cookieUser : res.sLoginExecutor,
                sText: scope.data.content
              };

              DocumentsService.setProcessStatus(params).then(function () {
                scope.execCtrlModals.bReport = false;
                scope.closeModalByButton();

                //todo когда будет изменен роутинг - нужно будет подправить путь на сервере к темплейту (путь будет tasks, а в роутинге tasks тоже используем)
                // var currentPageTemplate = $state.current.templateUrl;
                // $templateCache.remove(currentPageTemplate);
                $state.reload();
              });
            });
          }
      };
    }
  }
}]);
