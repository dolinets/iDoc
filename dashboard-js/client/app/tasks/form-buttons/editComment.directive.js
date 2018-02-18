angular.module('dashboardJsApp').directive('editComment', ['$state', '$templateCache', '$rootScope', '$http', 'Modal', 'CurrentServer', '$cookies',
  function ($state, $templateCache, $rootScope, $http, Modal, CurrentServer, $cookies) {
    return {
      restrict: 'EA',
      templateUrl: 'app/tasks/form-buttons/editComment.html',
      link: function (scope) {
        scope.data = {message: scope.sBody};
        $rootScope.spinner = false;

        var taskServer = CurrentServer.getServer();

        var sCurrReferent = $cookies.getObject('referent');
        sCurrReferent = sCurrReferent ? sCurrReferent.id : '';

        scope.sendEditMessage = function () {
          scope.modalSpinner = true;
          var options = {
            nID_Process_Activiti: scope.taskData.oProcess.nID,
            sLogin: scope.getCurrentUserLogin(),
            sBody: scope.data.message,
            sKeyGroup: scope.sKeyGroup,
            nID_ProcessChatMessage: scope.nID_Message_Parent,
            sLoginReferent: sCurrReferent ? sCurrReferent : sLogin,
            headers: {
              'Content-Type': 'application/json;charset=UTF-8'
            }
          };

          if (taskServer.another)
            options.taskServer = taskServer.name;

          $http.put('api/chat/updateProcessChatMessage', options).success(function () {
            scope.modalSpinner = false;
            Modal.inform.success()("Зауваження відредаговано");
            scope.closeModalByButton();
            scope.updateChat();
          });
        };
      }
    };
  }]);