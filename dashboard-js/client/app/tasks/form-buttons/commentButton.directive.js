angular.module('dashboardJsApp').directive('commentButton', ['$state', '$templateCache', '$rootScope', '$http', 'Modal', 'CurrentServer','$cookies',
  function ($state, $templateCache, $rootScope, $http, Modal, CurrentServer, $cookies) {
    return {
      restrict: 'EA',
      templateUrl: 'app/tasks/form-buttons/commentButton.html',
      link: function (scope) {
        scope.data = {message: ""};
        $rootScope.spinner = false;

        var taskServer = CurrentServer.getServer();

        var sCurrReferent = $cookies.getObject('referent');
        sCurrReferent = sCurrReferent ? sCurrReferent.id : '';

        scope.sendAskMessage = function () {
          scope.modalSpinner = true;
          var options = {
            nID_Process_Activiti: scope.taskData.oProcess.nID,
            sLogin: scope.getCurrentUserLogin(),
            sBody: scope.data.message,
            sKeyGroup: scope.getCurrentUserLogin(),
            sLoginReferent: sCurrReferent ? sCurrReferent : sLogin
          };

          if (taskServer.another)
            options.taskServer = taskServer.name;

          $http.post('api/chat/setProcessChatMessage', options).success(function () {
            scope.modalSpinner = false;
            //Modal.inform.success()("Зауваження додано");
            scope.closeModalByButton();
            scope.updateChat();
          });
        };

        scope.sendAnswerMessage = function () {
          scope.modalSpinner = true;
          var options = {
            nID_Process_Activiti: scope.taskData.oProcess.nID,
            sLogin: scope.getCurrentUserLogin(),
            sBody: scope.data.message,
            nID_ProcessChatMessage_Parent: scope.nID_Message_Parent,
            sKeyGroup: scope.sKeyGroup,
            sLoginReferent: sCurrReferent ? sCurrReferent : sLogin
          };

          if (taskServer.another)
            options.taskServer = taskServer.name;

          $http.post('api/chat/setProcessChatMessage', options).success(function () {
            scope.modalSpinner = false;
            //Modal.inform.success()("Відповідь додана");
            scope.closeModalByButton();
            scope.updateChat();
          });
        };

        correctHeight(document.getElementById('askMessage'));

        function correctHeight(elem) {
          var stopPos;

          elem.onmousedown = mouseDown;

          function mouseDown() {
            stopPos = document.getElementsByClassName('button-block-pull-right')[0].getBoundingClientRect().top;

            document.onmouseup = closeDragElement;
            document.onmousemove = elementDrag;
          }

          function elementDrag() {
            var bottom = elem.getBoundingClientRect().bottom;
            if (bottom >= stopPos) {
              elem.style.height = (parseInt(elem.style.height, 10)-5) + 'px';
              elem.style.resize = 'none';
            }
          }

          function closeDragElement() {
            document.onmouseup = null;
            document.onmousemove = null;

            elem.style.resize = 'vertical';
          }
        }
      }
    };
  }]);
